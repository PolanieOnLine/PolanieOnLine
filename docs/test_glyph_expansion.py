"""Independent regression contract for shipped glyph definitions and Zoryk recipes."""
import collections
import json
from pathlib import Path
import re
import struct
import unittest
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]
NS = {'s': 'stendhal'}
CONTRACT = json.loads((ROOT / 'docs/glyph-expansion-contract.json').read_text(encoding='utf-8'))

class GlyphExpansionTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.items = {i.attrib['name']: i for i in ET.parse(ROOT / 'data/conf/items/glyphs.xml').getroot()}
        cls.npc = (ROOT / 'src/games/stendhal/server/maps/tarnow/city/RuneMasterNPC.java').read_text(encoding='utf-8')
        cls.recipes = (ROOT / 'src/games/stendhal/server/core/rule/glyph/GlyphCraftingService.java').read_text(encoding='utf-8')

    def test_three_glyphs_per_slot(self):
        counts = collections.Counter()
        for item in self.items.values():
            for slot in item.findall('s:equipable/s:slot', NS):
                if slot.attrib['name'].endswith('_rune'):
                    counts[slot.attrib['name']] += 1
        self.assertEqual(len(self.items), 23)  # fragment, 21 combat glyphs and knowledge
        self.assertEqual(counts.pop('utility_rune'), 4)
        self.assertEqual(set(counts.values()), {3})
        self.assertEqual(len(counts), 6)

    def test_attributes_slots_and_sprites(self):
        for expected in CONTRACT:
            with self.subTest(glyph=expected['name']):
                item = self.items[expected['name']]
                attrs = {a.tag.split('}')[-1]: a.attrib['value'] for a in item.find('s:attributes', NS)}
                self.assertEqual(attrs, expected['attributes'])
                self.assertEqual({s.attrib['type']: s.attrib['value'] for s in item.findall('s:susceptibility', NS)}, expected['susceptibilities'])
                self.assertIn(expected['slot'], [s.attrib['name'] for s in item.findall('s:equipable/s:slot', NS)])
                self.assertEqual(item.find('s:type', NS).attrib['subclass'], expected['sprite'])
                png = (ROOT / 'data/sprites/items/glyph' / (expected['sprite'] + '.png')).read_bytes()
                self.assertEqual(png[:8], b'\x89PNG\r\n\x1a\n')
                self.assertEqual(struct.unpack('>II', png[16:24]), (32, 32))

    def test_exact_recipes_and_existing_resource_names(self):
        known = set()
        for path in (ROOT / 'data/conf/items').glob('*.xml'):
            known.update(i.attrib['name'] for i in ET.parse(path).getroot() if 'name' in i.attrib)
        recipes = dict(re.findall(r'glyphConfig\.put\("([^"]+)", createResourceMap\((.*?)\)\);', self.recipes, re.S))
        self.assertEqual(len(recipes), 21)
        for expected in CONTRACT:
            with self.subTest(glyph=expected['name']):
                actual = dict((name, int(amount)) for name, amount in re.findall(r'"([^"]+)",\s*(\d+)', recipes[expected['name']]))
                self.assertEqual(actual, expected['recipe'])
                self.assertTrue(set(actual).issubset(known), set(actual) - known)
        for name in ['glif wichru', 'glif wytrwałości']:
            self.assertIn('"glif Strzyboga", 1', recipes[name])
        self.assertIn('selectedGlyph.consumeResources(player)', self.npc)
        self.assertIn('player.equipOrPutOnGround(selectedGlyph.getItem());', self.npc)

if __name__ == '__main__':
    unittest.main()
