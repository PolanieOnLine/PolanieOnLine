from pathlib import Path
import re

branch_test = Path('tests/games/stendhal/server/entity/RPEntityTest.java')
text = branch_test.read_text(encoding='utf-8')
old = '''\t\tfinal Item item = SingletonRepository.getEntityManager().getItem("sztylecik");
\t\tentity.getSlot("lhand").add(item);
\t\tassertThat(entity.getItemAtk(), is((float) item.getAttack()));
\t\tentity.getSlot("rhand").add(item);
\t\tassertThat(entity.getItemAtk(), is((float) item.getAttack()));
\t\tentity.getSlot("lhand").remove(item.getID());
\t\tassertThat(entity.getItemAtk(), is((float) item.getAttack()));'''
new = '''\t\tfinal Item item = SingletonRepository.getEntityManager().getItem("sztylecik");
\t\tfinal float stableWeaponDamage = item.getAverageDamage();
\t\tentity.getSlot("lhand").add(item);
\t\tassertThat(entity.getItemAtk(), is(stableWeaponDamage));
\t\tentity.getSlot("rhand").add(item);
\t\tassertThat(entity.getItemAtk(), is(stableWeaponDamage));
\t\tentity.getSlot("lhand").remove(item.getID());
\t\tassertThat(entity.getItemAtk(), is(stableWeaponDamage));'''
if old not in text:
    raise SystemExit('Expected simple weapon test block not found')
branch_test.write_text(text.replace(old, new, 1), encoding='utf-8')

creature_path = Path('data/conf/creatures/stendhal/human_mithrilbourgh.xml')
text = creature_path.read_text(encoding='utf-8')
pattern = re.compile(r'(<creature name="rycerz mithrilbourgh">.*?<attributes>.*?<def value="[^"]+"/>)(?!\s*<armor )', re.DOTALL)
text, count = pattern.subn(r'\1\n\t\t\t<armor value="heavy"/>', text, count=1)
if count != 1:
    raise SystemExit(f'Expected exactly one unclassified mithrilbourgh knight, got {count}')
creature_path.write_text(text, encoding='utf-8')
