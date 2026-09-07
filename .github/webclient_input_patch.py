from pathlib import Path


def replace_once(path, old, new):
    p = Path(path)
    text = p.read_text()
    count = text.count(old)
    if count != 1:
        raise SystemExit(f"{path}: expected one match, found {count}: {old[:100]!r}")
    p.write_text(text.replace(old, new, 1))


# Use the module singleton instead of an absent global window.stendhal.
p = Path("src/js/stendhal/ui/HTMLManager.ts")
text = p.read_text()
marker = "/**\n * HTML code manipulation.\n */"
if text.count(marker) != 1:
    raise SystemExit("HTMLManager.ts: class comment marker mismatch")
text = text.replace(marker, 'import { stendhal } from "../stendhal";\n\n' + marker, 1)
old = 'const gamewindow = (globalThis as any)?.stendhal?.ui?.gamewindow;'
if text.count(old) != 1:
    raise SystemExit("HTMLManager.ts: global stendhal lookup mismatch")
p.write_text(text.replace(old, "const gamewindow = stendhal.ui?.gamewindow;", 1))


# Let the viewport own touch gestures.
replace_once(
    "src/js/stendhal/Client.ts",
    'gamewindow.addEventListener("touchstart", stendhal.ui.gamewindow.onMouseDown, { passive: true });',
    'gamewindow.addEventListener("touchstart", stendhal.ui.gamewindow.onMouseDown, { passive: false });',
)

p = Path("src/js/stendhal/ui/ViewPort.ts")
text = p.read_text()
old = '\t\tconst element = this.getElement() as HTMLCanvasElement;\n\t\tthis.ctx = element.getContext("2d")!;'
new = '\t\tconst element = this.getElement() as HTMLCanvasElement;\n\t\telement.style.touchAction = "none";\n\t\tthis.ctx = element.getContext("2d")!;'
if text.count(old) != 1:
    raise SystemExit("ViewPort.ts: constructor marker mismatch")
text = text.replace(old, new, 1)

old = '\t\t\tif (stendhal.ui.touch.isTouchEvent(e)) {\n\t\t\t\tif (stendhal.ui.touch.holding()) {'
new = '\t\t\tif (stendhal.ui.touch.isTouchEvent(e)) {\n\t\t\t\tif (e.cancelable) {\n\t\t\t\t\te.preventDefault();\n\t\t\t\t}\n\t\t\t\tif (stendhal.ui.touch.holding()) {'
if text.count(old) != 1:
    raise SystemExit("ViewPort.ts: touch start marker mismatch")
text = text.replace(old, new, 1)

old = '''\t\tmHandle.onDrag = function(e: MouseEvent) {
\t\t\tif (stendhal.ui.touch.isTouchEvent(e)) {
\t\t\t\tstendhal.ui.gamewindow.onDragStart(e);
\t\t\t}

\t\t\tvar pos = stendhal.ui.html.extractPosition(e);
\t\t\tvar xDiff = startX - pos.canvasRelativeX;
\t\t\tvar yDiff = startY - pos.canvasRelativeY;
\t\t\t// It's not really a click if the mouse has moved too much.
\t\t\tif (xDiff * xDiff + yDiff * yDiff > 5) {
\t\t\t\tmHandle.cleanUp(e);
\t\t\t}
\t\t}'''
new = '''\t\tmHandle.onDrag = function(e: MouseEvent | TouchEvent) {
\t\t\tconst isTouch = stendhal.ui.touch.isTouchEvent(e);
\t\t\tif (isTouch) {
\t\t\t\tstendhal.ui.gamewindow.onDragStart(e);
\t\t\t}

\t\t\tvar pos = stendhal.ui.html.extractPosition(e);
\t\t\tvar xDiff = startX - pos.canvasRelativeX;
\t\t\tvar yDiff = startY - pos.canvasRelativeY;
\t\t\t// Touch input jitters by a few pixels even during a normal tap.
\t\t\tconst dragThresholdSquared = isTouch ? 32 * 32 : 5;
\t\t\tif (xDiff * xDiff + yDiff * yDiff > dragThresholdSquared) {
\t\t\t\tmHandle.cleanUp(e);
\t\t\t}
\t\t}'''
if text.count(old) != 1:
    raise SystemExit("ViewPort.ts: drag block mismatch")
text = text.replace(old, new, 1)

old = "stendhal.ui.html.extractTarget(event).parentElement!"
if text.count(old) != 1:
    raise SystemExit("ViewPort.ts: stale event lookup mismatch")
text = text.replace(old, "stendhal.ui.html.extractTarget(e).parentElement!", 1)

old = '\tonTouchEnd(e: TouchEvent) {\n\t\tstendhal.ui.touch.onTouchEnd();\n\t\tstendhal.ui.gamewindow.onDrop(e);\n\t\tif (stendhal.ui.touch.holding()) {'
new = '\tonTouchEnd(e: TouchEvent) {\n\t\tstendhal.ui.touch.onTouchEnd();\n\t\tif (stendhal.ui.heldObject) {\n\t\t\tstendhal.ui.gamewindow.onDrop(e);\n\t\t}\n\t\tif (stendhal.ui.touch.holding()) {'
if text.count(old) != 1:
    raise SystemExit("ViewPort.ts: touchend block mismatch")
p.write_text(text.replace(old, new, 1))


# Make inventory touch dragging own the gesture and let taps behave like clicks.
p = Path("src/js/stendhal/ui/component/ItemContainerImplementation.ts")
text = p.read_text()
old = '\t\t\te.addEventListener("touchmove", (event: TouchEvent) => {\n\t\t\t\tthis.onTouchMove(event);\n\t\t\t}, {passive: true});'
new = '\t\t\te.addEventListener("touchmove", (event: TouchEvent) => {\n\t\t\t\tthis.onTouchMove(event);\n\t\t\t}, {passive: false});'
if text.count(old) != 1:
    raise SystemExit("ItemContainerImplementation.ts: touchmove listener mismatch")
text = text.replace(old, new, 1)

old = '\t\t\tlet e = this.parentElement.querySelector("#" + this.slot + this.suffix + i) as HTMLElement;\n\t\t\te.setAttribute("draggable", "true");'
new = '\t\t\tlet e = this.parentElement.querySelector("#" + this.slot + this.suffix + i) as HTMLElement;\n\t\t\te.setAttribute("draggable", "true");\n\t\t\te.style.touchAction = "none";'
if text.count(old) != 1:
    raise SystemExit("ItemContainerImplementation.ts: slot init marker mismatch")
text = text.replace(old, new, 1)

old = '\tprivate onTouchMove(event: TouchEvent) {\n\t\tif (stendhal.ui.heldObject) {\n\t\t\treturn;\n\t\t}\n\t\tthis.onDragStart(event);\n\t}'
new = '\tprivate onTouchMove(event: TouchEvent) {\n\t\tif (event.cancelable) {\n\t\t\tevent.preventDefault();\n\t\t}\n\t\tif (stendhal.ui.heldObject) {\n\t\t\treturn;\n\t\t}\n\t\tthis.onDragStart(event);\n\t}'
if text.count(old) != 1:
    raise SystemExit("ItemContainerImplementation.ts: onTouchMove mismatch")
text = text.replace(old, new, 1)

old = '''\tprivate onTouchEnd(evt: TouchEvent) {
\t\tstendhal.ui.touch.onTouchEnd();
\t\tif (stendhal.ui.touch.isLongTouch(evt) && !stendhal.ui.touch.holding()) {
\t\t\tthis.onMouseUp(evt);
\t\t} else if (stendhal.ui.touch.holding()) {
\t\t\tevt.preventDefault();

\t\t\tthis.onDrop(evt);
\t\t\tstendhal.ui.touch.setHolding(false);
\t\t}
\t\t// clean up touch handler
\t\tstendhal.ui.touch.unsetOrigin();
\t}'''
new = '''\tprivate onTouchEnd(evt: TouchEvent) {
\t\tstendhal.ui.touch.onTouchEnd();
\t\tif (stendhal.ui.touch.holding()) {
\t\t\tif (evt.cancelable) {
\t\t\t\tevt.preventDefault();
\t\t\t}
\t\t\tthis.onDrop(evt);
\t\t\tstendhal.ui.touch.setHolding(false);
\t\t} else {
\t\t\t// Treat a tap like a normal click and a long press like a context click.
\t\t\tthis.onMouseUp(evt);
\t\t}
\t\t// clean up touch handler
\t\tstendhal.ui.touch.unsetOrigin();
\t}'''
if text.count(old) != 1:
    raise SystemExit("ItemContainerImplementation.ts: onTouchEnd mismatch")
p.write_text(text.replace(old, new, 1))


# The held-item preview is visual only and must never become a drop target.
replace_once(
    "src/js/stendhal/ui/HeldObject.ts",
    '\tprivate constructor() {\n\t\tthis.image = document.getElementById("held-object")! as HTMLImageElement;\n\t}',
    '\tprivate constructor() {\n\t\tthis.image = document.getElementById("held-object")! as HTMLImageElement;\n\t\tthis.image.style.pointerEvents = "none";\n\t\tthis.image.classList.add("notarget");\n\t}',
)

# Keep browser gestures from stealing minimap taps.
replace_once(
    "src/js/stendhal/ui/component/MiniMapComponent.ts",
    '\tconstructor() {\n\t\tsuper("minimap");\n\t\tthis.map = TileMap.get();',
    '\tconstructor() {\n\t\tsuper("minimap");\n\t\tthis.map = TileMap.get();\n\t\tthis.componentElement.style.touchAction = "none";',
)

# The current web asset tree contains no legacy *b.png dress variants.
p = Path("src/js/stendhal/data/OutfitStore.ts")
text = p.read_text()
old = '''\tprivate busty_dress: number[] = [
\t\t  1,   4,   6,   7,  10,  11,  13,  16,
\t\t 29,  37,  40,  53,  54,  56,  61,  64,
\t\t967, 968, 977, 980, 989, 990, 999
\t];'''
new = '''\t// Legacy *b.png variants are not present in the web sprite set.
\t// Use the regular dress sprite instead of requesting a missing file.
\tprivate busty_dress: number[] = [];'''
if text.count(old) != 1:
    raise SystemExit("OutfitStore.ts: legacy dress list mismatch")
p.write_text(text.replace(old, new, 1))
