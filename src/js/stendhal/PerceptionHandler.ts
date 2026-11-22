/* Copyright (C) 2026 - PolanieOnLine; AGPL version 3 or later. */
import { marauroa, RPObject } from "marauroa";

interface ObjectChanges {
	a?: Record<string, unknown>;
	s?: Record<string, ObjectChanges[]>;
	m?: Record<string, { a?: Record<string, unknown> }>;
}

// Marauroa exposes its handler instance but does not export the handler class.
const BasePerceptionHandler = marauroa.perceptionHandler.constructor as new () => typeof marauroa.perceptionHandler;

/** Distinguish carried-object removal from partial scalar, map and nested-slot deletions. */
export class StendhalPerceptionHandler extends BasePerceptionHandler {
	override deleteChanges(object: RPObject, diff?: ObjectChanges): void {
		if (!object || !diff) return;
		super.deleteChanges(object, { a: diff.a });
		for (const [name, children] of Object.entries(diff.s || {})) {
			if (children.length === 0) {
				object.unset(name);
				continue;
			}
			const slot = object[name];
			if (!slot) continue;
			for (const child of children) {
				const id = child.a?.id;
				if (this.attributeKeys(child.a).length === 0
						&& Object.keys(child.s || {}).length === 0 && Object.keys(child.m || {}).length === 0) {
					slot.del(id);
				} else {
					this.deleteChanges(slot.get(id), child);
				}
			}
		}
		for (const [name, map] of Object.entries(diff.m || {})) {
			const keys = this.attributeKeys(map.a);
			if (keys.length === 0) object.unset(name);
			else if (object[name]) for (const key of keys) object.unsetMapEntry(name, key);
		}
	}

	private attributeKeys(attributes?: Record<string, unknown>): string[] {
		// Map payloads also carry these identity fields; RPObject forbids them as map keys.
		return Object.keys(attributes || {}).filter(key => key !== "id" && key !== "zoneid");
	}
}
