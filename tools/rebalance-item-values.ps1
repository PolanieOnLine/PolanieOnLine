# Read-only calibration/audit tool. -Patch prints an apply_patch patch, never edits files.
param([switch]$Patch)
$ErrorActionPreference = 'Stop'
$repo = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$definitions = @()
[xml]$manifest = [IO.File]::ReadAllText((Join-Path $repo 'data/conf/items.xml'))
foreach ($group in $manifest.SelectNodes('//*[local-name()="group"]')) {
    $file = Get-Item (Join-Path $repo ('data/conf/' + $group.GetAttribute('uri')))
    $text = [IO.File]::ReadAllText($file.FullName)
    [xml]$xml = $text
    foreach ($node in $xml.SelectNodes('//*[local-name()="item"]')) {
        $valueNode = $node.SelectSingleNode('*[local-name()="value"]')
        $stats = @('atk', 'ratk', 'def') | ForEach-Object {
            $statNode = $node.SelectSingleNode('*[local-name()="attributes"]/*[local-name()="' + $_ + '"]')
            if ($statNode) { [double]$statNode.GetAttribute('value') } else { 0 }
        }
        $rateNode = $node.SelectSingleNode('*[local-name()="attributes"]/*[local-name()="rate"]')
        $levelNode = $node.SelectSingleNode('*[local-name()="attributes"]/*[local-name()="min_level"]')
        $rate = if ($rateNode) { [Math]::Max(1, [double]$rateNode.GetAttribute('value')) } else { 1 }
        $weapons = @('sword', 'axe', 'club', 'dagger', 'ranged', 'wand', 'whip', 'missile', 'ammunition', 'magia')
        $score = if ($node.type.GetAttribute('class') -in $weapons) {
            [Math]::Max($stats[0], $stats[1]) / $rate
        } else { $stats[2] }
        $definitions += [pscustomobject]@{
            Name = $node.GetAttribute('name'); Class = $node.type.GetAttribute('class')
            Old = $(if ($valueNode) { [int]$valueNode.GetAttribute('value') } else { 0 })
            Score = $score
            Level = $(if ($levelNode) { [double]$levelNode.GetAttribute('value') } else { 0 })
            File = $file; Text = $text; ValueNode = $valueNode
        }
    }
}

function Read-Prices($relativePath) {
    [xml]$xml = [IO.File]::ReadAllText((Join-Path $repo $relativePath))
    $result = @{}
    foreach ($shop in $xml.SelectNodes('//*[local-name()="shop"]')) {
        $merchants = @($shop.SelectNodes('*[local-name()="merchant"]'))
        $factors = @(if ($merchants.Count) {
            foreach ($merchant in $merchants) {
                if ($merchant.HasAttribute('factor')) {
                    [double]::Parse($merchant.GetAttribute('factor'), [Globalization.CultureInfo]::InvariantCulture)
                } else { 1.0 }
            }
        } else { 1.0 })
        foreach ($item in $shop.SelectNodes('*[local-name()="item"]')) {
            $name = $item.GetAttribute('name')
            if (!$result.ContainsKey($name)) { $result[$name] = @() }
            foreach ($factor in $factors) {
                $price = [int][Math]::Floor([int]$item.GetAttribute('price') * $factor)
                if ($price -gt 0) { $result[$name] += $price }
            }
        }
    }
    return $result
}
$buy = Read-Prices 'data/conf/shops/buy_items.xml'
$sell = Read-Prices 'data/conf/shops/sell_items.xml'
$means = @{}
foreach ($name in $buy.Keys) {
    if ($buy[$name].Count) { $means[$name] = [int][Math]::Floor(($buy[$name] | Measure-Object -Average).Average + 0.5) }
}
$anchors = @($definitions | Where-Object { $_.Score -gt 0 -and $means.ContainsKey($_.Name) })
$ratios = @{}
foreach ($group in ($definitions | Group-Object Class)) {
    $samples = @($group.Group | Where-Object { $_.Old -gt 0 -and $means.ContainsKey($_.Name) } |
        ForEach-Object { $means[$_.Name] / [double]$_.Old } | Sort-Object)
    if ($samples.Count) {
        $ratios[$group.Name] = [Math]::Min(1.0, $samples[[int][Math]::Floor(($samples.Count - 1) / 2)])
    }
}
$rows = @()
foreach ($item in $definitions) {
    $value = $item.Old
    $method = 'retained'
    if ($item.Class -eq 'ring' -or $item.File.Name -eq 'rings.xml' -or $item.Class -eq 'money') {
        $method = 'excluded (rings/currency)'
    } else {
        if ($means.ContainsKey($item.Name)) {
            $value = $means[$item.Name]; $method = 'mean NPC buy price'
        } elseif ($item.Score -gt 0) {
            $peers = @($anchors | Where-Object Class -eq $item.Class)
            if (!$peers.Count) { $peers = @($anchors | Where-Object Class -eq 'armor') }
            $nearest = $peers | Sort-Object @{Expression={
                [Math]::Abs($_.Score - $item.Score) / [Math]::Max(1, $item.Score) +
                [Math]::Abs($_.Level - $item.Level) / [Math]::Max(1, $item.Level)
            }},
                @{Expression='Score'; Descending=$true}, Name | Select-Object -First 1
            if ($nearest) {
                $sameScore = @($peers | Where-Object { $_.Score -eq $nearest.Score -and $_.Level -eq $nearest.Level })
                $reference = ($sameScore | ForEach-Object { $means[$_.Name] } | Measure-Object -Average).Average
                $value = [int][Math]::Min([int]::MaxValue, [Math]::Max(1, [Math]::Floor(
                    $reference * [Math]::Pow($item.Score / $nearest.Score, 2) + 0.5)))
                $method = 'estimated from same-class NPC-priced combat items'
            }
        } elseif ($ratios.ContainsKey($item.Class) -and $value -gt 0) {
            $value = [int][Math]::Max(1, [Math]::Floor($value * $ratios[$item.Class] + 0.5))
            $method = 'class median buy/value ratio'
        }
        if ($sell.ContainsKey($item.Name) -and $sell[$item.Name].Count) {
            $ceiling = [Math]::Max(0, ($sell[$item.Name] | Measure-Object -Minimum).Minimum - 1)
            if ($value -gt $ceiling) { $value = [int]$ceiling; $method += ' + anti-arbitrage cap' }
        }
    }
    $rows += [pscustomobject]@{ Name=$item.Name; Class=$item.Class; Old=$item.Old; Value=$value; Method=$method; File=$item.File.Name }
}
if (!$Patch) { $rows | ConvertTo-Json -Depth 3; exit }

$patchLines = [Collections.Generic.List[string]]::new()
$patchLines.Add('*** Begin Patch')
foreach ($fileGroup in ($definitions | Group-Object { $_.File.FullName })) {
    $oldText = $fileGroup.Group[0].Text
    $filePatch = [Collections.Generic.List[string]]::new()
    foreach ($match in [regex]::Matches($oldText, '(?s)<item\b[^>]*>.*?</item>')) {
        [xml]$blockXml = $match.Value
        $name = $blockXml.DocumentElement.GetAttribute('name')
        $row = $rows | Where-Object Name -eq $name | Select-Object -First 1
        if (!$row -or $row.Value -eq $row.Old) { continue }
        $filePatch.Add('@@ ' + ($match.Value -split '\r?\n')[0])
        if ($blockXml.DocumentElement.SelectSingleNode('*[local-name()="value"]')) {
            $oldLine = [regex]::Match($match.Value, '(?m)^[ \t]*<value\s+value="\d+"\s*/>[^\n]*').Value.TrimEnd("`r")
            if (!$oldLine) { throw "Unsupported value formatting for $name" }
            $filePatch.Add('-' + $oldLine)
            $filePatch.Add('+' + [regex]::Replace($oldLine, 'value="\d+"', 'value="' + $row.Value + '"'))
        } else {
            $filePatch.Add('+        <value value="' + $row.Value + '"/>')
            $equipableLine = [regex]::Match($match.Value, '(?m)^[ \t]*<equipable>[^\n]*').Value.TrimEnd("`r")
            if (!$equipableLine) { throw "No equipable element for $name" }
            $filePatch.Add(' ' + $equipableLine)
        }
    }
    if (!$filePatch.Count) { continue }
    $relative = [IO.Path]::GetRelativePath($repo, $fileGroup.Name).Replace('\', '/')
    $patchLines.Add('*** Update File: ' + $relative)
    $patchLines.AddRange($filePatch)
}
$patchLines.Add('*** End Patch')
$patchLines -join "`n"
