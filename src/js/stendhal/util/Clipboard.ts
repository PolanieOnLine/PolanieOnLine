/** Copy directly from a user gesture, including WebViews without the Clipboard API. */
export async function copyTextToClipboard(text: string): Promise<boolean> {
	if (!text) { return false; }
	try {
		if (typeof navigator.clipboard?.writeText === "function") {
			await navigator.clipboard.writeText(text);
			return true;
		}
	} catch {
		// Some mobile WebViews expose this API but deny clipboard permission.
	}
	const previousFocus = document.activeElement as HTMLElement|null;
	const field = document.createElement("textarea");
	field.value = text;
	field.readOnly = true;
	field.style.position = "fixed";
	field.style.left = "0";
	field.style.top = "0";
	field.style.opacity = "0";
	field.style.pointerEvents = "none";
	field.setAttribute("aria-hidden", "true");
	document.body.appendChild(field);
	try {
		field.focus({ preventScroll: true });
		field.select();
		field.setSelectionRange(0, text.length);
		return typeof document.execCommand === "function" && document.execCommand("copy");
	} catch {
		return false;
	} finally {
		field.remove();
		previousFocus?.focus({ preventScroll: true });
	}
}
