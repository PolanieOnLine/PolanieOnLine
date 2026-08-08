from pathlib import Path
import re

BRANCH_FILES = {
    'data/conf/creatures/stendhal/dragon.xml': {
        'jeździec chaosu na zielonym smoku': 'medium',
        'jeździec chaosu na czerwonym smoku': 'medium',
        'błękitny smok': 'medium',
        'czarny smok': 'heavy',
        'złoty smok': 'medium',
        'dwugłowy niebieski smok': 'medium',
        'purpurowy smok': 'medium',
    },
    'data/conf/creatures/pol/dragon.xml': {
        'dwugłowy czarny smok': 'heavy',
        'dwugłowy lodowy smok': 'heavy',
        'dwugłowy czerwony smok': 'heavy',
        'dwugłowy złoty smok': 'heavy',
        'dwugłowy zielony smok': 'heavy',
        'lodowy smok': 'heavy',
        'pustynny smok': 'heavy',
        'smok arktyczny': 'heavy',
        'zielone smoczysko': 'heavy',
        'niebieskie smoczysko': 'heavy',
        'czerwone smoczysko': 'heavy',
        'czarne smoczysko': 'heavy',
        'latający czarny smok': 'heavy',
        'latający złoty smok': 'heavy',
        'Smok Wawelski': 'heavy',
    },
    'data/conf/creatures/stendhal/angel.xml': {
        'anioł ciemności': 'medium',
        'archanioł ciemności': 'heavy',
        'upadły anioł': 'medium',
        'anioł': 'medium',
        'archanioł': 'heavy',
    },
    'data/conf/creatures/pol/angel.xml': {
        'serafin': 'heavy',
        'azazel': 'heavy',
        'cherubin': 'heavy',
    },
}


def creature_block(text, name):
    pattern = re.compile(
        r'<creature name="' + re.escape(name) + r'">.*?</creature>',
        re.DOTALL,
    )
    match = pattern.search(text)
    if not match:
        raise SystemExit(f'Missing creature {name!r}')
    return match.group(0)


def add_armor(path, mapping):
    file_path = Path(path)
    text = file_path.read_text(encoding='utf-8')

    for name, armor in mapping.items():
        block = creature_block(text, name)
        if '<armor ' in block:
            raise SystemExit(f'{name!r} already has armor in {path}')

        def_pattern = re.compile(r'(<def value="[^"]+"/>)')
        updated_block, count = def_pattern.subn(
            r'\1\n\t\t\t<armor value="' + armor + r'"/>',
            block,
            count=1,
        )
        if count != 1:
            raise SystemExit(f'Expected exactly one def for {name!r} in {path}, got {count}')
        text = text.replace(block, updated_block, 1)

    file_path.write_text(text, encoding='utf-8')


# Existing regular dragons are the baseline for the family rule and must stay medium.
stendhal_dragon = Path('data/conf/creatures/stendhal/dragon.xml').read_text(encoding='utf-8')
for baseline_name in ('zielony smok', 'czerwony smok'):
    baseline = creature_block(stendhal_dragon, baseline_name)
    if '<armor value="medium"/>' not in baseline:
        raise SystemExit(f'Expected {baseline_name!r} to remain medium')

for path, mapping in BRANCH_FILES.items():
    add_armor(path, mapping)

print('Classified', sum(len(mapping) for mapping in BRANCH_FILES.values()), 'dragons/celestials')
