/** Match the desktop glyph palette while leaving alpha and animation untouched. */
export function glyphColourFilter(level: number): string {
    return level === 1 ? "saturate(0.55)" : level === 3 ? "saturate(1.55)" : "none";
}
