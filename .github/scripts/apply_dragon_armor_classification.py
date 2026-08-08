from pathlib import Path
import re

path = Path('data/conf/creatures/stendhal/dragon.xml')
text = path.read_text(encoding='utf-8')

armor_by_name = {
    'zielony smok': 'medium',
    'czerwony smok': 'medium',
}

for name, armor in armor_by_name.items():
    pattern = re.compile(
        r'(<creature name="' + re.escape(name) + r'">.*?<attributes>.*?<def value="[^"]+"/>)(?!\s*<armor )',
        re.DOTALL,
    )
    replacement = r'\1\n\t\t\t<armor value="' + armor + r'"/>'
    text, count = pattern.subn(replacement, text, count=1)
    if count != 1:
        raise SystemExit(f'Expected exactly one unclassified creature named {name!r}, got {count}')

path.write_text(text, encoding='utf-8')
