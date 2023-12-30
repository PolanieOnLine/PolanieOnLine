/*

**************************************************************************
                   (C) Copyright 2003-2023 - Stendhal                    *
**************************************************************************
                                                                         *
   This program is free software; you can redistribute it and/or modify  *
   it under the terms of the GNU Affero General Public License as        *
   published by the Free Software Foundation; either version 3 of the    *
   License, or (at your option) any later version.                       *
                                                                         *
**************************************************************************





 Have a look at https://stendhalgame.org/development/introduction.html to get the source code.
 And perhaps, contribute a feature or bugfix.





















































 Compiled and optimized JavaScript follows. Don't let that scare you off, have a look at the source instead.
*/
var $JSCompiler_prototypeAlias$$;
function $$jscomp$arrayIteratorImpl$$($array$jscomp$6$$) {
  var $index$jscomp$73$$ = 0;
  return function() {
    return $index$jscomp$73$$ < $array$jscomp$6$$.length ? {done:!1, value:$array$jscomp$6$$[$index$jscomp$73$$++],} : {done:!0};
  };
}
function $$jscomp$makeIterator$$($iterable$jscomp$4$$) {
  var $iteratorFunction$$ = "undefined" != typeof Symbol && Symbol.iterator && $iterable$jscomp$4$$[Symbol.iterator];
  return $iteratorFunction$$ ? $iteratorFunction$$.call($iterable$jscomp$4$$) : {next:$$jscomp$arrayIteratorImpl$$($iterable$jscomp$4$$)};
}
function $$jscomp$arrayFromIterable$$($JSCompiler_temp$jscomp$139_iterable$jscomp$5_iterator$jscomp$inline_210$$) {
  if (!($JSCompiler_temp$jscomp$139_iterable$jscomp$5_iterator$jscomp$inline_210$$ instanceof Array)) {
    $JSCompiler_temp$jscomp$139_iterable$jscomp$5_iterator$jscomp$inline_210$$ = $$jscomp$makeIterator$$($JSCompiler_temp$jscomp$139_iterable$jscomp$5_iterator$jscomp$inline_210$$);
    for (var $i$jscomp$inline_211$$, $arr$jscomp$inline_212$$ = []; !($i$jscomp$inline_211$$ = $JSCompiler_temp$jscomp$139_iterable$jscomp$5_iterator$jscomp$inline_210$$.next()).done;) {
      $arr$jscomp$inline_212$$.push($i$jscomp$inline_211$$.value);
    }
    $JSCompiler_temp$jscomp$139_iterable$jscomp$5_iterator$jscomp$inline_210$$ = $arr$jscomp$inline_212$$;
  }
  return $JSCompiler_temp$jscomp$139_iterable$jscomp$5_iterator$jscomp$inline_210$$;
}
var $$jscomp$objectCreate$$ = "function" == typeof Object.create ? Object.create : function($prototype$$) {
  function $ctor$$() {
  }
  $ctor$$.prototype = $prototype$$;
  return new $ctor$$();
}, $$jscomp$defineProperty$$ = "function" == typeof Object.defineProperties ? Object.defineProperty : function($target$jscomp$92$$, $property$jscomp$5$$, $descriptor$jscomp$1$$) {
  if ($target$jscomp$92$$ == Array.prototype || $target$jscomp$92$$ == Object.prototype) {
    return $target$jscomp$92$$;
  }
  $target$jscomp$92$$[$property$jscomp$5$$] = $descriptor$jscomp$1$$.value;
  return $target$jscomp$92$$;
};
function $$jscomp$getGlobal$$($passedInThis_possibleGlobals$$) {
  $passedInThis_possibleGlobals$$ = ["object" == typeof globalThis && globalThis, $passedInThis_possibleGlobals$$, "object" == typeof window && window, "object" == typeof self && self, "object" == typeof global && global,];
  for (var $i$jscomp$4$$ = 0; $i$jscomp$4$$ < $passedInThis_possibleGlobals$$.length; ++$i$jscomp$4$$) {
    var $maybeGlobal$$ = $passedInThis_possibleGlobals$$[$i$jscomp$4$$];
    if ($maybeGlobal$$ && $maybeGlobal$$.Math == Math) {
      return $maybeGlobal$$;
    }
  }
  throw Error("Cannot find global object");
}
var $$jscomp$global$$ = $$jscomp$getGlobal$$(this);
function $$jscomp$polyfill$$($property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$, $impl$jscomp$inline_223_polyfill$jscomp$1$$) {
  if ($impl$jscomp$inline_223_polyfill$jscomp$1$$) {
    a: {
      var $obj$jscomp$inline_217$$ = $$jscomp$global$$;
      $property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$ = $property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$.split(".");
      for (var $i$jscomp$inline_219_orig$jscomp$inline_222$$ = 0; $i$jscomp$inline_219_orig$jscomp$inline_222$$ < $property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$.length - 1; $i$jscomp$inline_219_orig$jscomp$inline_222$$++) {
        var $key$jscomp$inline_220$$ = $property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$[$i$jscomp$inline_219_orig$jscomp$inline_222$$];
        if (!($key$jscomp$inline_220$$ in $obj$jscomp$inline_217$$)) {
          break a;
        }
        $obj$jscomp$inline_217$$ = $obj$jscomp$inline_217$$[$key$jscomp$inline_220$$];
      }
      $property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$ = $property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$[$property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$.length - 1];
      $i$jscomp$inline_219_orig$jscomp$inline_222$$ = $obj$jscomp$inline_217$$[$property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$];
      $impl$jscomp$inline_223_polyfill$jscomp$1$$ = $impl$jscomp$inline_223_polyfill$jscomp$1$$($i$jscomp$inline_219_orig$jscomp$inline_222$$);
      $impl$jscomp$inline_223_polyfill$jscomp$1$$ != $i$jscomp$inline_219_orig$jscomp$inline_222$$ && null != $impl$jscomp$inline_223_polyfill$jscomp$1$$ && $$jscomp$defineProperty$$($obj$jscomp$inline_217$$, $property$jscomp$inline_221_split$jscomp$inline_218_target$jscomp$94$$, {configurable:!0, writable:!0, value:$impl$jscomp$inline_223_polyfill$jscomp$1$$});
    }
  }
}
var $$jscomp$construct$$ = function() {
  function $reflectConstructWorks$$() {
    function $Base$$() {
    }
    new $Base$$();
    Reflect.construct($Base$$, [], function() {
    });
    return new $Base$$() instanceof $Base$$;
  }
  if ("undefined" != typeof Reflect && Reflect.construct) {
    if ($reflectConstructWorks$$()) {
      return Reflect.construct;
    }
    var $brokenConstruct$$ = Reflect.construct;
    return function($out_target$jscomp$97$$, $argList$jscomp$2$$, $opt_newTarget$$) {
      $out_target$jscomp$97$$ = $brokenConstruct$$($out_target$jscomp$97$$, $argList$jscomp$2$$);
      $opt_newTarget$$ && Reflect.setPrototypeOf($out_target$jscomp$97$$, $opt_newTarget$$.prototype);
      return $out_target$jscomp$97$$;
    };
  }
  return function($target$jscomp$98$$, $argList$jscomp$3$$, $obj$jscomp$27_opt_newTarget$jscomp$1$$) {
    void 0 === $obj$jscomp$27_opt_newTarget$jscomp$1$$ && ($obj$jscomp$27_opt_newTarget$jscomp$1$$ = $target$jscomp$98$$);
    $obj$jscomp$27_opt_newTarget$jscomp$1$$ = $$jscomp$objectCreate$$($obj$jscomp$27_opt_newTarget$jscomp$1$$.prototype || Object.prototype);
    return Function.prototype.apply.call($target$jscomp$98$$, $obj$jscomp$27_opt_newTarget$jscomp$1$$, $argList$jscomp$3$$) || $obj$jscomp$27_opt_newTarget$jscomp$1$$;
  };
}(), $JSCompiler_temp$jscomp$142$$;
if ("function" == typeof Object.setPrototypeOf) {
  $JSCompiler_temp$jscomp$142$$ = Object.setPrototypeOf;
} else {
  var $JSCompiler_inline_result$jscomp$143$$;
  a: {
    var $x$jscomp$inline_225$$ = {a:!0}, $y$jscomp$inline_226$$ = {};
    try {
      $y$jscomp$inline_226$$.__proto__ = $x$jscomp$inline_225$$;
      $JSCompiler_inline_result$jscomp$143$$ = $y$jscomp$inline_226$$.a;
      break a;
    } catch ($e$jscomp$inline_227$$) {
    }
    $JSCompiler_inline_result$jscomp$143$$ = !1;
  }
  $JSCompiler_temp$jscomp$142$$ = $JSCompiler_inline_result$jscomp$143$$ ? function($target$jscomp$99$$, $proto$jscomp$4$$) {
    $target$jscomp$99$$.__proto__ = $proto$jscomp$4$$;
    if ($target$jscomp$99$$.__proto__ !== $proto$jscomp$4$$) {
      throw new TypeError($target$jscomp$99$$ + " is not extensible");
    }
    return $target$jscomp$99$$;
  } : null;
}
var $$jscomp$setPrototypeOf$$ = $JSCompiler_temp$jscomp$142$$;
function $$jscomp$inherits$$($childCtor$$, $parentCtor$$) {
  $childCtor$$.prototype = $$jscomp$objectCreate$$($parentCtor$$.prototype);
  $childCtor$$.prototype.constructor = $childCtor$$;
  if ($$jscomp$setPrototypeOf$$) {
    $$jscomp$setPrototypeOf$$($childCtor$$, $parentCtor$$);
  } else {
    for (var $p$$ in $parentCtor$$) {
      if ("prototype" != $p$$) {
        if (Object.defineProperties) {
          var $descriptor$jscomp$2$$ = Object.getOwnPropertyDescriptor($parentCtor$$, $p$$);
          $descriptor$jscomp$2$$ && Object.defineProperty($childCtor$$, $p$$, $descriptor$jscomp$2$$);
        } else {
          $childCtor$$[$p$$] = $parentCtor$$[$p$$];
        }
      }
    }
  }
  $childCtor$$.$superClass_$ = $parentCtor$$.prototype;
}
function $$jscomp$getRestArguments$$() {
  for (var $startIndex$$ = Number(this), $restArgs$$ = [], $i$jscomp$7$$ = $startIndex$$; $i$jscomp$7$$ < arguments.length; $i$jscomp$7$$++) {
    $restArgs$$[$i$jscomp$7$$ - $startIndex$$] = arguments[$i$jscomp$7$$];
  }
  return $restArgs$$;
}
$$jscomp$polyfill$$("Reflect", function($orig$jscomp$1$$) {
  return $orig$jscomp$1$$ ? $orig$jscomp$1$$ : {};
});
$$jscomp$polyfill$$("Reflect.construct", function() {
  return $$jscomp$construct$$;
});
$$jscomp$polyfill$$("Reflect.setPrototypeOf", function($orig$jscomp$3$$) {
  return $orig$jscomp$3$$ ? $orig$jscomp$3$$ : $$jscomp$setPrototypeOf$$ ? function($target$jscomp$100$$, $proto$jscomp$5$$) {
    try {
      return $$jscomp$setPrototypeOf$$($target$jscomp$100$$, $proto$jscomp$5$$), !0;
    } catch ($e$jscomp$8$$) {
      return !1;
    }
  } : null;
});
$$jscomp$polyfill$$("Symbol", function($orig$jscomp$4$$) {
  function $symbolPolyfill$$($opt_description$jscomp$2$$) {
    if (this instanceof $symbolPolyfill$$) {
      throw new TypeError("Symbol is not a constructor");
    }
    return new $SymbolClass$$($SYMBOL_PREFIX$$ + ($opt_description$jscomp$2$$ || "") + "_" + $counter$$++, $opt_description$jscomp$2$$);
  }
  function $SymbolClass$$($id$jscomp$5$$, $opt_description$jscomp$1$$) {
    this.$v$ = $id$jscomp$5$$;
    $$jscomp$defineProperty$$(this, "description", {configurable:!0, writable:!0, value:$opt_description$jscomp$1$$});
  }
  if ($orig$jscomp$4$$) {
    return $orig$jscomp$4$$;
  }
  $SymbolClass$$.prototype.toString = function() {
    return this.$v$;
  };
  var $SYMBOL_PREFIX$$ = "jscomp_symbol_" + (1e9 * Math.random() >>> 0) + "_", $counter$$ = 0;
  return $symbolPolyfill$$;
});
$$jscomp$polyfill$$("Symbol.iterator", function($orig$jscomp$5_symbolIterator$$) {
  if ($orig$jscomp$5_symbolIterator$$) {
    return $orig$jscomp$5_symbolIterator$$;
  }
  $orig$jscomp$5_symbolIterator$$ = Symbol("Symbol.iterator");
  for (var $arrayLikes$$ = "Array Int8Array Uint8Array Uint8ClampedArray Int16Array Uint16Array Int32Array Uint32Array Float32Array Float64Array".split(" "), $i$jscomp$8$$ = 0; $i$jscomp$8$$ < $arrayLikes$$.length; $i$jscomp$8$$++) {
    var $ArrayLikeCtor$$ = $$jscomp$global$$[$arrayLikes$$[$i$jscomp$8$$]];
    "function" === typeof $ArrayLikeCtor$$ && "function" != typeof $ArrayLikeCtor$$.prototype[$orig$jscomp$5_symbolIterator$$] && $$jscomp$defineProperty$$($ArrayLikeCtor$$.prototype, $orig$jscomp$5_symbolIterator$$, {configurable:!0, writable:!0, value:function() {
      return $$jscomp$iteratorPrototype$$($$jscomp$arrayIteratorImpl$$(this));
    }});
  }
  return $orig$jscomp$5_symbolIterator$$;
});
function $$jscomp$iteratorPrototype$$($iterator$jscomp$7_next$$) {
  $iterator$jscomp$7_next$$ = {next:$iterator$jscomp$7_next$$};
  $iterator$jscomp$7_next$$[Symbol.iterator] = function() {
    return this;
  };
  return $iterator$jscomp$7_next$$;
}
function $$jscomp$owns$$($obj$jscomp$28$$, $prop$jscomp$2$$) {
  return Object.prototype.hasOwnProperty.call($obj$jscomp$28$$, $prop$jscomp$2$$);
}
$$jscomp$polyfill$$("WeakMap", function($NativeWeakMap$$) {
  function $PolyfillWeakMap$$($iter$jscomp$1_opt_iterable$jscomp$4$$) {
    this.$v$ = ($index$jscomp$74$$ += Math.random() + 1).toString();
    if ($iter$jscomp$1_opt_iterable$jscomp$4$$) {
      $iter$jscomp$1_opt_iterable$jscomp$4$$ = $$jscomp$makeIterator$$($iter$jscomp$1_opt_iterable$jscomp$4$$);
      for (var $entry_item$jscomp$1$$; !($entry_item$jscomp$1$$ = $iter$jscomp$1_opt_iterable$jscomp$4$$.next()).done;) {
        $entry_item$jscomp$1$$ = $entry_item$jscomp$1$$.value, this.set($entry_item$jscomp$1$$[0], $entry_item$jscomp$1$$[1]);
      }
    }
  }
  function $WeakMapMembership$$() {
  }
  function $isValidKey$$($key$jscomp$40$$) {
    var $type$jscomp$153$$ = typeof $key$jscomp$40$$;
    return "object" === $type$jscomp$153$$ && null !== $key$jscomp$40$$ || "function" === $type$jscomp$153$$;
  }
  function $insert$$($target$jscomp$102$$) {
    if (!$$jscomp$owns$$($target$jscomp$102$$, $prop$jscomp$3$$)) {
      var $obj$jscomp$29$$ = new $WeakMapMembership$$();
      $$jscomp$defineProperty$$($target$jscomp$102$$, $prop$jscomp$3$$, {value:$obj$jscomp$29$$});
    }
  }
  function $patch$$($name$jscomp$73$$) {
    var $prev$$ = Object[$name$jscomp$73$$];
    $prev$$ && (Object[$name$jscomp$73$$] = function($target$jscomp$103$$) {
      if ($target$jscomp$103$$ instanceof $WeakMapMembership$$) {
        return $target$jscomp$103$$;
      }
      Object.isExtensible($target$jscomp$103$$) && $insert$$($target$jscomp$103$$);
      return $prev$$($target$jscomp$103$$);
    });
  }
  if (function() {
    if (!$NativeWeakMap$$ || !Object.seal) {
      return !1;
    }
    try {
      var $x$jscomp$89$$ = Object.seal({}), $y$jscomp$74$$ = Object.seal({}), $map$$ = new $NativeWeakMap$$([[$x$jscomp$89$$, 2], [$y$jscomp$74$$, 3]]);
      if (2 != $map$$.get($x$jscomp$89$$) || 3 != $map$$.get($y$jscomp$74$$)) {
        return !1;
      }
      $map$$.delete($x$jscomp$89$$);
      $map$$.set($y$jscomp$74$$, 4);
      return !$map$$.has($x$jscomp$89$$) && 4 == $map$$.get($y$jscomp$74$$);
    } catch ($err$jscomp$4$$) {
      return !1;
    }
  }()) {
    return $NativeWeakMap$$;
  }
  var $prop$jscomp$3$$ = "$jscomp_hidden_" + Math.random();
  $patch$$("freeze");
  $patch$$("preventExtensions");
  $patch$$("seal");
  var $index$jscomp$74$$ = 0;
  $PolyfillWeakMap$$.prototype.set = function($key$jscomp$41$$, $value$jscomp$91$$) {
    if (!$isValidKey$$($key$jscomp$41$$)) {
      throw Error("Invalid WeakMap key");
    }
    $insert$$($key$jscomp$41$$);
    if (!$$jscomp$owns$$($key$jscomp$41$$, $prop$jscomp$3$$)) {
      throw Error("WeakMap key fail: " + $key$jscomp$41$$);
    }
    $key$jscomp$41$$[$prop$jscomp$3$$][this.$v$] = $value$jscomp$91$$;
    return this;
  };
  $PolyfillWeakMap$$.prototype.get = function($key$jscomp$42$$) {
    return $isValidKey$$($key$jscomp$42$$) && $$jscomp$owns$$($key$jscomp$42$$, $prop$jscomp$3$$) ? $key$jscomp$42$$[$prop$jscomp$3$$][this.$v$] : void 0;
  };
  $PolyfillWeakMap$$.prototype.has = function($key$jscomp$43$$) {
    return $isValidKey$$($key$jscomp$43$$) && $$jscomp$owns$$($key$jscomp$43$$, $prop$jscomp$3$$) && $$jscomp$owns$$($key$jscomp$43$$[$prop$jscomp$3$$], this.$v$);
  };
  $PolyfillWeakMap$$.prototype.delete = function($key$jscomp$44$$) {
    return $isValidKey$$($key$jscomp$44$$) && $$jscomp$owns$$($key$jscomp$44$$, $prop$jscomp$3$$) && $$jscomp$owns$$($key$jscomp$44$$[$prop$jscomp$3$$], this.$v$) ? delete $key$jscomp$44$$[$prop$jscomp$3$$][this.$v$] : !1;
  };
  return $PolyfillWeakMap$$;
});
$$jscomp$polyfill$$("Map", function($NativeMap$$) {
  function $createHead$$() {
    var $head$$ = {};
    return $head$$.$previous$ = $head$$.next = $head$$.head = $head$$;
  }
  function $makeIterator$$($map$jscomp$3$$, $func$jscomp$3$$) {
    var $entry$jscomp$8$$ = $map$jscomp$3$$.$v$;
    return $$jscomp$iteratorPrototype$$(function() {
      if ($entry$jscomp$8$$) {
        for (; $entry$jscomp$8$$.head != $map$jscomp$3$$.$v$;) {
          $entry$jscomp$8$$ = $entry$jscomp$8$$.$previous$;
        }
        for (; $entry$jscomp$8$$.next != $entry$jscomp$8$$.head;) {
          return $entry$jscomp$8$$ = $entry$jscomp$8$$.next, {done:!1, value:$func$jscomp$3$$($entry$jscomp$8$$)};
        }
        $entry$jscomp$8$$ = null;
      }
      return {done:!0, value:void 0};
    });
  }
  function $maybeGetEntry$$($index$jscomp$75_map$jscomp$2$$, $key$jscomp$50$$) {
    var $id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$ = $key$jscomp$50$$ && typeof $key$jscomp$50$$;
    "object" == $id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$ || "function" == $id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$ ? $idMap$$.has($key$jscomp$50$$) ? $id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$ = $idMap$$.get($key$jscomp$50$$) : ($id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$ = "" + ++$mapIndex$$, $idMap$$.set($key$jscomp$50$$, $id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$)) : $id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$ = 
    "p_" + $key$jscomp$50$$;
    var $list$$ = $index$jscomp$75_map$jscomp$2$$.$A$[$id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$];
    if ($list$$ && $$jscomp$owns$$($index$jscomp$75_map$jscomp$2$$.$A$, $id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$)) {
      for ($index$jscomp$75_map$jscomp$2$$ = 0; $index$jscomp$75_map$jscomp$2$$ < $list$$.length; $index$jscomp$75_map$jscomp$2$$++) {
        var $entry$jscomp$7$$ = $list$$[$index$jscomp$75_map$jscomp$2$$];
        if ($key$jscomp$50$$ !== $key$jscomp$50$$ && $entry$jscomp$7$$.key !== $entry$jscomp$7$$.key || $key$jscomp$50$$ === $entry$jscomp$7$$.key) {
          return {id:$id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$, list:$list$$, index:$index$jscomp$75_map$jscomp$2$$, $entry$:$entry$jscomp$7$$};
        }
      }
    }
    return {id:$id$jscomp$6_id$jscomp$inline_231_type$jscomp$inline_230$$, list:$list$$, index:-1, $entry$:void 0};
  }
  function $PolyfillMap$$($iter$jscomp$3_opt_iterable$jscomp$5$$) {
    this.$A$ = {};
    this.$v$ = $createHead$$();
    this.size = 0;
    if ($iter$jscomp$3_opt_iterable$jscomp$5$$) {
      $iter$jscomp$3_opt_iterable$jscomp$5$$ = $$jscomp$makeIterator$$($iter$jscomp$3_opt_iterable$jscomp$5$$);
      for (var $entry$jscomp$1_item$jscomp$3$$; !($entry$jscomp$1_item$jscomp$3$$ = $iter$jscomp$3_opt_iterable$jscomp$5$$.next()).done;) {
        $entry$jscomp$1_item$jscomp$3$$ = $entry$jscomp$1_item$jscomp$3$$.value, this.set($entry$jscomp$1_item$jscomp$3$$[0], $entry$jscomp$1_item$jscomp$3$$[1]);
      }
    }
  }
  if (function() {
    if (!$NativeMap$$ || "function" != typeof $NativeMap$$ || !$NativeMap$$.prototype.entries || "function" != typeof Object.seal) {
      return !1;
    }
    try {
      var $key$jscomp$45$$ = Object.seal({x:4}), $map$jscomp$1$$ = new $NativeMap$$($$jscomp$makeIterator$$([[$key$jscomp$45$$, "s"]]));
      if ("s" != $map$jscomp$1$$.get($key$jscomp$45$$) || 1 != $map$jscomp$1$$.size || $map$jscomp$1$$.get({x:4}) || $map$jscomp$1$$.set({x:4}, "t") != $map$jscomp$1$$ || 2 != $map$jscomp$1$$.size) {
        return !1;
      }
      var $iter$jscomp$2$$ = $map$jscomp$1$$.entries(), $item$jscomp$2$$ = $iter$jscomp$2$$.next();
      if ($item$jscomp$2$$.done || $item$jscomp$2$$.value[0] != $key$jscomp$45$$ || "s" != $item$jscomp$2$$.value[1]) {
        return !1;
      }
      $item$jscomp$2$$ = $iter$jscomp$2$$.next();
      return $item$jscomp$2$$.done || 4 != $item$jscomp$2$$.value[0].x || "t" != $item$jscomp$2$$.value[1] || !$iter$jscomp$2$$.next().done ? !1 : !0;
    } catch ($err$jscomp$5$$) {
      return !1;
    }
  }()) {
    return $NativeMap$$;
  }
  var $idMap$$ = new WeakMap();
  $PolyfillMap$$.prototype.set = function($key$jscomp$46$$, $value$jscomp$92$$) {
    $key$jscomp$46$$ = 0 === $key$jscomp$46$$ ? 0 : $key$jscomp$46$$;
    var $r$jscomp$1$$ = $maybeGetEntry$$(this, $key$jscomp$46$$);
    $r$jscomp$1$$.list || ($r$jscomp$1$$.list = this.$A$[$r$jscomp$1$$.id] = []);
    $r$jscomp$1$$.$entry$ ? $r$jscomp$1$$.$entry$.value = $value$jscomp$92$$ : ($r$jscomp$1$$.$entry$ = {next:this.$v$, $previous$:this.$v$.$previous$, head:this.$v$, key:$key$jscomp$46$$, value:$value$jscomp$92$$,}, $r$jscomp$1$$.list.push($r$jscomp$1$$.$entry$), this.$v$.$previous$.next = $r$jscomp$1$$.$entry$, this.$v$.$previous$ = $r$jscomp$1$$.$entry$, this.size++);
    return this;
  };
  $PolyfillMap$$.prototype.delete = function($key$jscomp$47_r$jscomp$2$$) {
    $key$jscomp$47_r$jscomp$2$$ = $maybeGetEntry$$(this, $key$jscomp$47_r$jscomp$2$$);
    return $key$jscomp$47_r$jscomp$2$$.$entry$ && $key$jscomp$47_r$jscomp$2$$.list ? ($key$jscomp$47_r$jscomp$2$$.list.splice($key$jscomp$47_r$jscomp$2$$.index, 1), $key$jscomp$47_r$jscomp$2$$.list.length || delete this.$A$[$key$jscomp$47_r$jscomp$2$$.id], $key$jscomp$47_r$jscomp$2$$.$entry$.$previous$.next = $key$jscomp$47_r$jscomp$2$$.$entry$.next, $key$jscomp$47_r$jscomp$2$$.$entry$.next.$previous$ = $key$jscomp$47_r$jscomp$2$$.$entry$.$previous$, $key$jscomp$47_r$jscomp$2$$.$entry$.head = null, 
    this.size--, !0) : !1;
  };
  $PolyfillMap$$.prototype.clear = function() {
    this.$A$ = {};
    this.$v$ = this.$v$.$previous$ = $createHead$$();
    this.size = 0;
  };
  $PolyfillMap$$.prototype.has = function($key$jscomp$48$$) {
    return !!$maybeGetEntry$$(this, $key$jscomp$48$$).$entry$;
  };
  $PolyfillMap$$.prototype.get = function($entry$jscomp$2_key$jscomp$49$$) {
    return ($entry$jscomp$2_key$jscomp$49$$ = $maybeGetEntry$$(this, $entry$jscomp$2_key$jscomp$49$$).$entry$) && $entry$jscomp$2_key$jscomp$49$$.value;
  };
  $PolyfillMap$$.prototype.entries = function() {
    return $makeIterator$$(this, function($entry$jscomp$3$$) {
      return [$entry$jscomp$3$$.key, $entry$jscomp$3$$.value];
    });
  };
  $PolyfillMap$$.prototype.keys = function() {
    return $makeIterator$$(this, function($entry$jscomp$4$$) {
      return $entry$jscomp$4$$.key;
    });
  };
  $PolyfillMap$$.prototype.values = function() {
    return $makeIterator$$(this, function($entry$jscomp$5$$) {
      return $entry$jscomp$5$$.value;
    });
  };
  $PolyfillMap$$.prototype.forEach = function($callback$jscomp$46$$, $opt_thisArg$jscomp$9$$) {
    for (var $iter$jscomp$4$$ = this.entries(), $entry$jscomp$6_item$jscomp$4$$; !($entry$jscomp$6_item$jscomp$4$$ = $iter$jscomp$4$$.next()).done;) {
      $entry$jscomp$6_item$jscomp$4$$ = $entry$jscomp$6_item$jscomp$4$$.value, $callback$jscomp$46$$.call($opt_thisArg$jscomp$9$$, $entry$jscomp$6_item$jscomp$4$$[1], $entry$jscomp$6_item$jscomp$4$$[0], this);
    }
  };
  $PolyfillMap$$.prototype[Symbol.iterator] = $PolyfillMap$$.prototype.entries;
  var $mapIndex$$ = 0;
  return $PolyfillMap$$;
});
function $$jscomp$iteratorFromArray$$($array$jscomp$8$$, $transform$jscomp$1$$) {
  $array$jscomp$8$$ instanceof String && ($array$jscomp$8$$ += "");
  var $i$jscomp$9$$ = 0, $done$$ = !1, $iter$jscomp$5$$ = {next:function() {
    if (!$done$$ && $i$jscomp$9$$ < $array$jscomp$8$$.length) {
      var $index$jscomp$76$$ = $i$jscomp$9$$++;
      return {value:$transform$jscomp$1$$($index$jscomp$76$$, $array$jscomp$8$$[$index$jscomp$76$$]), done:!1};
    }
    $done$$ = !0;
    return {done:!0, value:void 0};
  }};
  $iter$jscomp$5$$[Symbol.iterator] = function() {
    return $iter$jscomp$5$$;
  };
  return $iter$jscomp$5$$;
}
$$jscomp$polyfill$$("Array.prototype.entries", function($orig$jscomp$7$$) {
  return $orig$jscomp$7$$ ? $orig$jscomp$7$$ : function() {
    return $$jscomp$iteratorFromArray$$(this, function($i$jscomp$10$$, $v$$) {
      return [$i$jscomp$10$$, $v$$];
    });
  };
});
$$jscomp$polyfill$$("Object.is", function($orig$jscomp$8$$) {
  return $orig$jscomp$8$$ ? $orig$jscomp$8$$ : function($left$jscomp$2$$, $right$jscomp$2$$) {
    return $left$jscomp$2$$ === $right$jscomp$2$$ ? 0 !== $left$jscomp$2$$ || 1 / $left$jscomp$2$$ === 1 / $right$jscomp$2$$ : $left$jscomp$2$$ !== $left$jscomp$2$$ && $right$jscomp$2$$ !== $right$jscomp$2$$;
  };
});
$$jscomp$polyfill$$("Array.prototype.includes", function($orig$jscomp$9$$) {
  return $orig$jscomp$9$$ ? $orig$jscomp$9$$ : function($searchElement$jscomp$4$$, $i$jscomp$11_opt_fromIndex$jscomp$8$$) {
    var $array$jscomp$9$$ = this;
    $array$jscomp$9$$ instanceof String && ($array$jscomp$9$$ = String($array$jscomp$9$$));
    var $len$$ = $array$jscomp$9$$.length;
    $i$jscomp$11_opt_fromIndex$jscomp$8$$ = $i$jscomp$11_opt_fromIndex$jscomp$8$$ || 0;
    for (0 > $i$jscomp$11_opt_fromIndex$jscomp$8$$ && ($i$jscomp$11_opt_fromIndex$jscomp$8$$ = Math.max($i$jscomp$11_opt_fromIndex$jscomp$8$$ + $len$$, 0)); $i$jscomp$11_opt_fromIndex$jscomp$8$$ < $len$$; $i$jscomp$11_opt_fromIndex$jscomp$8$$++) {
      var $element$jscomp$8$$ = $array$jscomp$9$$[$i$jscomp$11_opt_fromIndex$jscomp$8$$];
      if ($element$jscomp$8$$ === $searchElement$jscomp$4$$ || Object.is($element$jscomp$8$$, $searchElement$jscomp$4$$)) {
        return !0;
      }
    }
    return !1;
  };
});
function $$jscomp$checkStringArgs$$($thisArg$jscomp$4$$, $arg$jscomp$8$$, $func$jscomp$4$$) {
  if (null == $thisArg$jscomp$4$$) {
    throw new TypeError("The 'this' value for String.prototype." + $func$jscomp$4$$ + " must not be null or undefined");
  }
  if ($arg$jscomp$8$$ instanceof RegExp) {
    throw new TypeError("First argument to String.prototype." + $func$jscomp$4$$ + " must not be a regular expression");
  }
  return $thisArg$jscomp$4$$ + "";
}
$$jscomp$polyfill$$("String.prototype.includes", function($orig$jscomp$10$$) {
  return $orig$jscomp$10$$ ? $orig$jscomp$10$$ : function($searchString$jscomp$3$$, $opt_position$jscomp$3$$) {
    return -1 !== $$jscomp$checkStringArgs$$(this, $searchString$jscomp$3$$, "includes").indexOf($searchString$jscomp$3$$, $opt_position$jscomp$3$$ || 0);
  };
});
$$jscomp$polyfill$$("String.prototype.startsWith", function($orig$jscomp$11$$) {
  return $orig$jscomp$11$$ ? $orig$jscomp$11$$ : function($searchString$jscomp$4$$, $i$jscomp$12_opt_position$jscomp$4$$) {
    var $string$jscomp$4$$ = $$jscomp$checkStringArgs$$(this, $searchString$jscomp$4$$, "startsWith"), $strLen$$ = $string$jscomp$4$$.length, $searchLen$$ = $searchString$jscomp$4$$.length;
    $i$jscomp$12_opt_position$jscomp$4$$ = Math.max(0, Math.min($i$jscomp$12_opt_position$jscomp$4$$ | 0, $string$jscomp$4$$.length));
    for (var $j$$ = 0; $j$$ < $searchLen$$ && $i$jscomp$12_opt_position$jscomp$4$$ < $strLen$$;) {
      if ($string$jscomp$4$$[$i$jscomp$12_opt_position$jscomp$4$$++] != $searchString$jscomp$4$$[$j$$++]) {
        return !1;
      }
    }
    return $j$$ >= $searchLen$$;
  };
});
$$jscomp$polyfill$$("Array.prototype.keys", function($orig$jscomp$12$$) {
  return $orig$jscomp$12$$ ? $orig$jscomp$12$$ : function() {
    return $$jscomp$iteratorFromArray$$(this, function($i$jscomp$13$$) {
      return $i$jscomp$13$$;
    });
  };
});
$$jscomp$polyfill$$("Number.isNaN", function($orig$jscomp$13$$) {
  return $orig$jscomp$13$$ ? $orig$jscomp$13$$ : function($x$jscomp$90$$) {
    return "number" === typeof $x$jscomp$90$$ && isNaN($x$jscomp$90$$);
  };
});
$$jscomp$polyfill$$("Array.from", function($orig$jscomp$14$$) {
  return $orig$jscomp$14$$ ? $orig$jscomp$14$$ : function($arrayLike$jscomp$1$$, $opt_mapFn$jscomp$10$$, $opt_thisArg$jscomp$10$$) {
    $opt_mapFn$jscomp$10$$ = null != $opt_mapFn$jscomp$10$$ ? $opt_mapFn$jscomp$10$$ : function($x$jscomp$91$$) {
      return $x$jscomp$91$$;
    };
    var $result$jscomp$1$$ = [], $iteratorFunction$jscomp$1_len$jscomp$1_next$jscomp$1$$ = "undefined" != typeof Symbol && Symbol.iterator && $arrayLike$jscomp$1$$[Symbol.iterator];
    if ("function" == typeof $iteratorFunction$jscomp$1_len$jscomp$1_next$jscomp$1$$) {
      $arrayLike$jscomp$1$$ = $iteratorFunction$jscomp$1_len$jscomp$1_next$jscomp$1$$.call($arrayLike$jscomp$1$$);
      for (var $i$jscomp$14_k$$ = 0; !($iteratorFunction$jscomp$1_len$jscomp$1_next$jscomp$1$$ = $arrayLike$jscomp$1$$.next()).done;) {
        $result$jscomp$1$$.push($opt_mapFn$jscomp$10$$.call($opt_thisArg$jscomp$10$$, $iteratorFunction$jscomp$1_len$jscomp$1_next$jscomp$1$$.value, $i$jscomp$14_k$$++));
      }
    } else {
      for ($iteratorFunction$jscomp$1_len$jscomp$1_next$jscomp$1$$ = $arrayLike$jscomp$1$$.length, $i$jscomp$14_k$$ = 0; $i$jscomp$14_k$$ < $iteratorFunction$jscomp$1_len$jscomp$1_next$jscomp$1$$; $i$jscomp$14_k$$++) {
        $result$jscomp$1$$.push($opt_mapFn$jscomp$10$$.call($opt_thisArg$jscomp$10$$, $arrayLike$jscomp$1$$[$i$jscomp$14_k$$], $i$jscomp$14_k$$));
      }
    }
    return $result$jscomp$1$$;
  };
});
$$jscomp$polyfill$$("Math.trunc", function($orig$jscomp$15$$) {
  return $orig$jscomp$15$$ ? $orig$jscomp$15$$ : function($x$jscomp$92$$) {
    $x$jscomp$92$$ = Number($x$jscomp$92$$);
    if (isNaN($x$jscomp$92$$) || Infinity === $x$jscomp$92$$ || -Infinity === $x$jscomp$92$$ || 0 === $x$jscomp$92$$) {
      return $x$jscomp$92$$;
    }
    var $y$jscomp$75$$ = Math.floor(Math.abs($x$jscomp$92$$));
    return 0 > $x$jscomp$92$$ ? -$y$jscomp$75$$ : $y$jscomp$75$$;
  };
});
$$jscomp$polyfill$$("String.prototype.endsWith", function($orig$jscomp$16$$) {
  return $orig$jscomp$16$$ ? $orig$jscomp$16$$ : function($searchString$jscomp$5$$, $i$jscomp$15_opt_position$jscomp$5$$) {
    var $string$jscomp$5$$ = $$jscomp$checkStringArgs$$(this, $searchString$jscomp$5$$, "endsWith");
    void 0 === $i$jscomp$15_opt_position$jscomp$5$$ && ($i$jscomp$15_opt_position$jscomp$5$$ = $string$jscomp$5$$.length);
    $i$jscomp$15_opt_position$jscomp$5$$ = Math.max(0, Math.min($i$jscomp$15_opt_position$jscomp$5$$ | 0, $string$jscomp$5$$.length));
    for (var $j$jscomp$1$$ = $searchString$jscomp$5$$.length; 0 < $j$jscomp$1$$ && 0 < $i$jscomp$15_opt_position$jscomp$5$$;) {
      if ($string$jscomp$5$$[--$i$jscomp$15_opt_position$jscomp$5$$] != $searchString$jscomp$5$$[--$j$jscomp$1$$]) {
        return !1;
      }
    }
    return 0 >= $j$jscomp$1$$;
  };
});
$$jscomp$polyfill$$("Set", function($NativeSet$$) {
  function $PolyfillSet$$($iter$jscomp$7_opt_iterable$jscomp$6$$) {
    this.$v$ = new Map();
    if ($iter$jscomp$7_opt_iterable$jscomp$6$$) {
      $iter$jscomp$7_opt_iterable$jscomp$6$$ = $$jscomp$makeIterator$$($iter$jscomp$7_opt_iterable$jscomp$6$$);
      for (var $entry$jscomp$9$$; !($entry$jscomp$9$$ = $iter$jscomp$7_opt_iterable$jscomp$6$$.next()).done;) {
        this.add($entry$jscomp$9$$.value);
      }
    }
    this.size = this.$v$.size;
  }
  if (function() {
    if (!$NativeSet$$ || "function" != typeof $NativeSet$$ || !$NativeSet$$.prototype.entries || "function" != typeof Object.seal) {
      return !1;
    }
    try {
      var $value$jscomp$93$$ = Object.seal({x:4}), $set$$ = new $NativeSet$$($$jscomp$makeIterator$$([$value$jscomp$93$$]));
      if (!$set$$.has($value$jscomp$93$$) || 1 != $set$$.size || $set$$.add($value$jscomp$93$$) != $set$$ || 1 != $set$$.size || $set$$.add({x:4}) != $set$$ || 2 != $set$$.size) {
        return !1;
      }
      var $iter$jscomp$6$$ = $set$$.entries(), $item$jscomp$5$$ = $iter$jscomp$6$$.next();
      if ($item$jscomp$5$$.done || $item$jscomp$5$$.value[0] != $value$jscomp$93$$ || $item$jscomp$5$$.value[1] != $value$jscomp$93$$) {
        return !1;
      }
      $item$jscomp$5$$ = $iter$jscomp$6$$.next();
      return $item$jscomp$5$$.done || $item$jscomp$5$$.value[0] == $value$jscomp$93$$ || 4 != $item$jscomp$5$$.value[0].x || $item$jscomp$5$$.value[1] != $item$jscomp$5$$.value[0] ? !1 : $iter$jscomp$6$$.next().done;
    } catch ($err$jscomp$6$$) {
      return !1;
    }
  }()) {
    return $NativeSet$$;
  }
  $PolyfillSet$$.prototype.add = function($value$jscomp$94$$) {
    $value$jscomp$94$$ = 0 === $value$jscomp$94$$ ? 0 : $value$jscomp$94$$;
    this.$v$.set($value$jscomp$94$$, $value$jscomp$94$$);
    this.size = this.$v$.size;
    return this;
  };
  $PolyfillSet$$.prototype.delete = function($result$jscomp$2_value$jscomp$95$$) {
    $result$jscomp$2_value$jscomp$95$$ = this.$v$.delete($result$jscomp$2_value$jscomp$95$$);
    this.size = this.$v$.size;
    return $result$jscomp$2_value$jscomp$95$$;
  };
  $PolyfillSet$$.prototype.clear = function() {
    this.$v$.clear();
    this.size = 0;
  };
  $PolyfillSet$$.prototype.has = function($value$jscomp$96$$) {
    return this.$v$.has($value$jscomp$96$$);
  };
  $PolyfillSet$$.prototype.entries = function() {
    return this.$v$.entries();
  };
  $PolyfillSet$$.prototype.values = function() {
    return this.$v$.values();
  };
  $PolyfillSet$$.prototype.keys = $PolyfillSet$$.prototype.values;
  $PolyfillSet$$.prototype[Symbol.iterator] = $PolyfillSet$$.prototype.values;
  $PolyfillSet$$.prototype.forEach = function($callback$jscomp$47$$, $opt_thisArg$jscomp$11$$) {
    var $set$jscomp$1$$ = this;
    this.$v$.forEach(function($value$jscomp$97$$) {
      return $callback$jscomp$47$$.call($opt_thisArg$jscomp$11$$, $value$jscomp$97$$, $value$jscomp$97$$, $set$jscomp$1$$);
    });
  };
  return $PolyfillSet$$;
});
$$jscomp$polyfill$$("Promise", function($NativePromise$$) {
  function $PolyfillPromise$$($executor$$) {
    this.$A$ = 0;
    this.$B$ = void 0;
    this.$v$ = [];
    this.$G$ = !1;
    var $resolveAndReject$$ = this.$C$();
    try {
      $executor$$($resolveAndReject$$.resolve, $resolveAndReject$$.reject);
    } catch ($e$jscomp$9$$) {
      $resolveAndReject$$.reject($e$jscomp$9$$);
    }
  }
  function $AsyncExecutor$$() {
    this.$v$ = null;
  }
  function $resolvingPromise$$($opt_value$jscomp$10$$) {
    return $opt_value$jscomp$10$$ instanceof $PolyfillPromise$$ ? $opt_value$jscomp$10$$ : new $PolyfillPromise$$(function($resolve$jscomp$1$$) {
      $resolve$jscomp$1$$($opt_value$jscomp$10$$);
    });
  }
  if ($NativePromise$$) {
    return $NativePromise$$;
  }
  $AsyncExecutor$$.prototype.$A$ = function($f$jscomp$1$$) {
    if (null == this.$v$) {
      this.$v$ = [];
      var $self$jscomp$1$$ = this;
      this.$B$(function() {
        $self$jscomp$1$$.$D$();
      });
    }
    this.$v$.push($f$jscomp$1$$);
  };
  var $nativeSetTimeout$$ = $$jscomp$global$$.setTimeout;
  $AsyncExecutor$$.prototype.$B$ = function($f$jscomp$2$$) {
    $nativeSetTimeout$$($f$jscomp$2$$, 0);
  };
  $AsyncExecutor$$.prototype.$D$ = function() {
    for (; this.$v$ && this.$v$.length;) {
      var $executingBatch$$ = this.$v$;
      this.$v$ = [];
      for (var $i$jscomp$16$$ = 0; $i$jscomp$16$$ < $executingBatch$$.length; ++$i$jscomp$16$$) {
        var $f$jscomp$3$$ = $executingBatch$$[$i$jscomp$16$$];
        $executingBatch$$[$i$jscomp$16$$] = null;
        try {
          $f$jscomp$3$$();
        } catch ($error$jscomp$2$$) {
          this.$C$($error$jscomp$2$$);
        }
      }
    }
    this.$v$ = null;
  };
  $AsyncExecutor$$.prototype.$C$ = function($exception$jscomp$2$$) {
    this.$B$(function() {
      throw $exception$jscomp$2$$;
    });
  };
  $PolyfillPromise$$.prototype.$C$ = function() {
    function $firstCallWins$$($method$jscomp$1$$) {
      return function($x$jscomp$93$$) {
        $alreadyCalled$$ || ($alreadyCalled$$ = !0, $method$jscomp$1$$.call($thisPromise$$, $x$jscomp$93$$));
      };
    }
    var $thisPromise$$ = this, $alreadyCalled$$ = !1;
    return {resolve:$firstCallWins$$(this.$L$), reject:$firstCallWins$$(this.$D$)};
  };
  $PolyfillPromise$$.prototype.$L$ = function($value$jscomp$98$$) {
    if ($value$jscomp$98$$ === this) {
      this.$D$(new TypeError("A Promise cannot resolve to itself"));
    } else {
      if ($value$jscomp$98$$ instanceof $PolyfillPromise$$) {
        this.$N$($value$jscomp$98$$);
      } else {
        a: {
          switch(typeof $value$jscomp$98$$) {
            case "object":
              var $JSCompiler_inline_result$jscomp$146$$ = null != $value$jscomp$98$$;
              break a;
            case "function":
              $JSCompiler_inline_result$jscomp$146$$ = !0;
              break a;
            default:
              $JSCompiler_inline_result$jscomp$146$$ = !1;
          }
        }
        $JSCompiler_inline_result$jscomp$146$$ ? this.$K$($value$jscomp$98$$) : this.$F$($value$jscomp$98$$);
      }
    }
  };
  $PolyfillPromise$$.prototype.$K$ = function($obj$jscomp$31$$) {
    var $thenMethod$$ = void 0;
    try {
      $thenMethod$$ = $obj$jscomp$31$$.then;
    } catch ($error$jscomp$3$$) {
      this.$D$($error$jscomp$3$$);
      return;
    }
    "function" == typeof $thenMethod$$ ? this.$O$($thenMethod$$, $obj$jscomp$31$$) : this.$F$($obj$jscomp$31$$);
  };
  $PolyfillPromise$$.prototype.$D$ = function($reason$jscomp$6$$) {
    this.$H$(2, $reason$jscomp$6$$);
  };
  $PolyfillPromise$$.prototype.$F$ = function($value$jscomp$100$$) {
    this.$H$(1, $value$jscomp$100$$);
  };
  $PolyfillPromise$$.prototype.$H$ = function($settledState$$, $valueOrReason$$) {
    if (0 != this.$A$) {
      throw Error("Cannot settle(" + $settledState$$ + ", " + $valueOrReason$$ + "): Promise already settled in state" + this.$A$);
    }
    this.$A$ = $settledState$$;
    this.$B$ = $valueOrReason$$;
    2 === this.$A$ && this.$M$();
    this.$I$();
  };
  $PolyfillPromise$$.prototype.$M$ = function() {
    var $self$jscomp$2$$ = this;
    $nativeSetTimeout$$(function() {
      if ($self$jscomp$2$$.$J$()) {
        var $nativeConsole$$ = $$jscomp$global$$.console;
        "undefined" !== typeof $nativeConsole$$ && $nativeConsole$$.error($self$jscomp$2$$.$B$);
      }
    }, 1);
  };
  $PolyfillPromise$$.prototype.$J$ = function() {
    if (this.$G$) {
      return !1;
    }
    var $NativeCustomEvent_event$jscomp$4$$ = $$jscomp$global$$.CustomEvent, $NativeEvent$$ = $$jscomp$global$$.Event, $nativeDispatchEvent$$ = $$jscomp$global$$.dispatchEvent;
    if ("undefined" === typeof $nativeDispatchEvent$$) {
      return !0;
    }
    "function" === typeof $NativeCustomEvent_event$jscomp$4$$ ? $NativeCustomEvent_event$jscomp$4$$ = new $NativeCustomEvent_event$jscomp$4$$("unhandledrejection", {cancelable:!0}) : "function" === typeof $NativeEvent$$ ? $NativeCustomEvent_event$jscomp$4$$ = new $NativeEvent$$("unhandledrejection", {cancelable:!0}) : ($NativeCustomEvent_event$jscomp$4$$ = $$jscomp$global$$.document.createEvent("CustomEvent"), $NativeCustomEvent_event$jscomp$4$$.initCustomEvent("unhandledrejection", !1, !0, $NativeCustomEvent_event$jscomp$4$$));
    $NativeCustomEvent_event$jscomp$4$$.promise = this;
    $NativeCustomEvent_event$jscomp$4$$.reason = this.$B$;
    return $nativeDispatchEvent$$($NativeCustomEvent_event$jscomp$4$$);
  };
  $PolyfillPromise$$.prototype.$I$ = function() {
    if (null != this.$v$) {
      for (var $i$jscomp$17$$ = 0; $i$jscomp$17$$ < this.$v$.length; ++$i$jscomp$17$$) {
        $asyncExecutor$$.$A$(this.$v$[$i$jscomp$17$$]);
      }
      this.$v$ = null;
    }
  };
  var $asyncExecutor$$ = new $AsyncExecutor$$();
  $PolyfillPromise$$.prototype.$N$ = function($promise$$) {
    var $methods$jscomp$1$$ = this.$C$();
    $promise$$.$callWhenSettled_$($methods$jscomp$1$$.resolve, $methods$jscomp$1$$.reject);
  };
  $PolyfillPromise$$.prototype.$O$ = function($thenMethod$jscomp$1$$, $thenable$$) {
    var $methods$jscomp$2$$ = this.$C$();
    try {
      $thenMethod$jscomp$1$$.call($thenable$$, $methods$jscomp$2$$.resolve, $methods$jscomp$2$$.reject);
    } catch ($error$jscomp$4$$) {
      $methods$jscomp$2$$.reject($error$jscomp$4$$);
    }
  };
  $PolyfillPromise$$.prototype.then = function($onFulfilled$$, $onRejected$jscomp$1$$) {
    function $createCallback$$($paramF$$, $defaultF$$) {
      return "function" == typeof $paramF$$ ? function($x$jscomp$94$$) {
        try {
          $resolveChild$$($paramF$$($x$jscomp$94$$));
        } catch ($error$jscomp$5$$) {
          $rejectChild$$($error$jscomp$5$$);
        }
      } : $defaultF$$;
    }
    var $resolveChild$$, $rejectChild$$, $childPromise$$ = new $PolyfillPromise$$(function($resolve$$, $reject$$) {
      $resolveChild$$ = $resolve$$;
      $rejectChild$$ = $reject$$;
    });
    this.$callWhenSettled_$($createCallback$$($onFulfilled$$, $resolveChild$$), $createCallback$$($onRejected$jscomp$1$$, $rejectChild$$));
    return $childPromise$$;
  };
  $PolyfillPromise$$.prototype.catch = function($onRejected$jscomp$2$$) {
    return this.then(void 0, $onRejected$jscomp$2$$);
  };
  $PolyfillPromise$$.prototype.$callWhenSettled_$ = function($onFulfilled$jscomp$1$$, $onRejected$jscomp$3$$) {
    function $callback$jscomp$48$$() {
      switch($thisPromise$jscomp$1$$.$A$) {
        case 1:
          $onFulfilled$jscomp$1$$($thisPromise$jscomp$1$$.$B$);
          break;
        case 2:
          $onRejected$jscomp$3$$($thisPromise$jscomp$1$$.$B$);
          break;
        default:
          throw Error("Unexpected state: " + $thisPromise$jscomp$1$$.$A$);
      }
    }
    var $thisPromise$jscomp$1$$ = this;
    null == this.$v$ ? $asyncExecutor$$.$A$($callback$jscomp$48$$) : this.$v$.push($callback$jscomp$48$$);
    this.$G$ = !0;
  };
  $PolyfillPromise$$.resolve = $resolvingPromise$$;
  $PolyfillPromise$$.reject = function($opt_reason$jscomp$1$$) {
    return new $PolyfillPromise$$(function($resolve$jscomp$2$$, $reject$jscomp$2$$) {
      $reject$jscomp$2$$($opt_reason$jscomp$1$$);
    });
  };
  $PolyfillPromise$$.race = function($thenablesOrValues$$) {
    return new $PolyfillPromise$$(function($resolve$jscomp$3$$, $reject$jscomp$3$$) {
      for (var $iterator$jscomp$8$$ = $$jscomp$makeIterator$$($thenablesOrValues$$), $iterRec$$ = $iterator$jscomp$8$$.next(); !$iterRec$$.done; $iterRec$$ = $iterator$jscomp$8$$.next()) {
        $resolvingPromise$$($iterRec$$.value).$callWhenSettled_$($resolve$jscomp$3$$, $reject$jscomp$3$$);
      }
    });
  };
  $PolyfillPromise$$.all = function($thenablesOrValues$jscomp$1$$) {
    var $iterator$jscomp$9$$ = $$jscomp$makeIterator$$($thenablesOrValues$jscomp$1$$), $iterRec$jscomp$1$$ = $iterator$jscomp$9$$.next();
    return $iterRec$jscomp$1$$.done ? $resolvingPromise$$([]) : new $PolyfillPromise$$(function($resolveAll$$, $rejectAll$$) {
      function $onFulfilled$jscomp$2$$($i$jscomp$18$$) {
        return function($ithResult$$) {
          $resultsArray$$[$i$jscomp$18$$] = $ithResult$$;
          $unresolvedCount$$--;
          0 == $unresolvedCount$$ && $resolveAll$$($resultsArray$$);
        };
      }
      var $resultsArray$$ = [], $unresolvedCount$$ = 0;
      do {
        $resultsArray$$.push(void 0), $unresolvedCount$$++, $resolvingPromise$$($iterRec$jscomp$1$$.value).$callWhenSettled_$($onFulfilled$jscomp$2$$($resultsArray$$.length - 1), $rejectAll$$), $iterRec$jscomp$1$$ = $iterator$jscomp$9$$.next();
      } while (!$iterRec$jscomp$1$$.done);
    });
  };
  return $PolyfillPromise$$;
});
$$jscomp$polyfill$$("Array.prototype.fill", function($orig$jscomp$17$$) {
  return $orig$jscomp$17$$ ? $orig$jscomp$17$$ : function($value$jscomp$101$$, $i$jscomp$19_opt_start$$, $opt_end$jscomp$9$$) {
    var $length$jscomp$16$$ = this.length || 0;
    0 > $i$jscomp$19_opt_start$$ && ($i$jscomp$19_opt_start$$ = Math.max(0, $length$jscomp$16$$ + $i$jscomp$19_opt_start$$));
    if (null == $opt_end$jscomp$9$$ || $opt_end$jscomp$9$$ > $length$jscomp$16$$) {
      $opt_end$jscomp$9$$ = $length$jscomp$16$$;
    }
    $opt_end$jscomp$9$$ = Number($opt_end$jscomp$9$$);
    0 > $opt_end$jscomp$9$$ && ($opt_end$jscomp$9$$ = Math.max(0, $length$jscomp$16$$ + $opt_end$jscomp$9$$));
    for ($i$jscomp$19_opt_start$$ = Number($i$jscomp$19_opt_start$$ || 0); $i$jscomp$19_opt_start$$ < $opt_end$jscomp$9$$; $i$jscomp$19_opt_start$$++) {
      this[$i$jscomp$19_opt_start$$] = $value$jscomp$101$$;
    }
    return this;
  };
});
function $$jscomp$typedArrayFill$$($orig$jscomp$18$$) {
  return $orig$jscomp$18$$ ? $orig$jscomp$18$$ : Array.prototype.fill;
}
$$jscomp$polyfill$$("Int8Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Uint8Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Uint8ClampedArray.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Int16Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Uint16Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Int32Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Uint32Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Float32Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Float64Array.prototype.fill", $$jscomp$typedArrayFill$$);
$$jscomp$polyfill$$("Number.parseInt", function($orig$jscomp$19$$) {
  return $orig$jscomp$19$$ || parseInt;
});
$$jscomp$polyfill$$("Number.parseFloat", function($orig$jscomp$20$$) {
  return $orig$jscomp$20$$ || parseFloat;
});
/*

*************************************************************************
                   (C) Copyright 2011-2017 - Marauroa                    *
**************************************************************************
**************************************************************************
                                                                         *
   This program is free software; you can redistribute it and/or modify  *
   it under the terms of the GNU General Public License as published by  *
   the Free Software Foundation; either version 2 of the License, or     *
   (at your option) any later version.                                   *
                                                                         *
**************************************************************************/
var $marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.debug = {$messages$:!1, $unknownEvents$:!0};
window.console || (window.console = {});
window.console.log || (window.console.log = function() {
}, window.console.info = function() {
}, window.console.warn = function() {
}, window.console.error = function($text$jscomp$10$$) {
  alert($text$jscomp$10$$);
});
$marauroa$$.$util$ = {$isEmpty$:function($obj$jscomp$32$$) {
  for (var $i$jscomp$20$$ in $obj$jscomp$32$$) {
    if ($obj$jscomp$32$$.hasOwnProperty($i$jscomp$20$$)) {
      return !1;
    }
  }
  return !0;
}, $isEmptyExceptId$:function($obj$jscomp$33$$) {
  for (var $i$jscomp$21$$ in $obj$jscomp$33$$) {
    if ("id" !== $i$jscomp$21$$ && $obj$jscomp$33$$.hasOwnProperty($i$jscomp$21$$)) {
      return !1;
    }
  }
  return !0;
}, first:function($obj$jscomp$34$$) {
  for (var $i$jscomp$22$$ in $obj$jscomp$34$$) {
    if ($obj$jscomp$34$$.hasOwnProperty($i$jscomp$22$$)) {
      return $obj$jscomp$34$$[$i$jscomp$22$$];
    }
  }
}, $fromProto$:function($proto$jscomp$6$$, $def$$) {
  var $F_obj$jscomp$35$$ = void 0;
  "function" === typeof $proto$jscomp$6$$ ? $F_obj$jscomp$35$$ = new $proto$jscomp$6$$() : ($F_obj$jscomp$35$$ = function() {
  }, $F_obj$jscomp$35$$.prototype = $proto$jscomp$6$$, $F_obj$jscomp$35$$ = new $F_obj$jscomp$35$$());
  return $def$$ ? $marauroa$$.$util$.$merge$($F_obj$jscomp$35$$, $def$$) : $F_obj$jscomp$35$$;
}, $merge$:function($a$jscomp$1$$, $b$jscomp$1$$) {
  for (var $key$jscomp$51$$ in $b$jscomp$1$$) {
    $a$jscomp$1$$[$key$jscomp$51$$] = $b$jscomp$1$$[$key$jscomp$51$$];
  }
  return $a$jscomp$1$$;
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$clientFramework$ = {$clientid$:"-1", connect:function($host_socket$$, $port$$, $path$jscomp$5$$) {
  var $protocol$jscomp$1$$ = "ws";
  "https:" === window.location.protocol && ($protocol$jscomp$1$$ = "wss");
  null === $host_socket$$ && ($host_socket$$ = window.location.hostname);
  null === $port$$ && ($port$$ = window.location.port);
  "" != $port$$ && ($port$$ = ":" + $port$$);
  null === $path$jscomp$5$$ && ($path$jscomp$5$$ = "ws/");
  $host_socket$$ = new WebSocket($protocol$jscomp$1$$ + "://" + $host_socket$$ + $port$$ + "/" + $path$jscomp$5$$);
  $host_socket$$.onmessage = $marauroa$$.$clientFramework$.$onMessage$;
  $host_socket$$.onopen = function() {
    setInterval(function() {
      $marauroa$$.$clientFramework$.$sendMessage$({t:"8",});
    }, 10000);
    $marauroa$$.$clientFramework$.$onConnect$();
  };
  $host_socket$$.onclose = $marauroa$$.$clientFramework$.$onDisconnect$;
  $marauroa$$.$clientFramework$.$socket$ = $host_socket$$;
}, $onConnect$:function($reason$jscomp$7$$, $error$jscomp$6$$) {
  "undefined" == typeof $error$jscomp$6$$ ? console.log("connected") : console.error("onConnect: " + $reason$jscomp$7$$ + " error: " + $error$jscomp$6$$);
}, $onDisconnect$:function($reason$jscomp$8$$, $error$jscomp$7$$) {
  console.log("onDisconnect: " + $reason$jscomp$8$$ + " error: " + $error$jscomp$7$$);
}, $onLoginRequired$:function() {
}, $login$:function($username$$, $msg$jscomp$1_password$$) {
  $msg$jscomp$1_password$$ = {t:"34", u:$username$$, p:$msg$jscomp$1_password$$};
  $marauroa$$.$clientFramework$.username = $username$$;
  $marauroa$$.$clientFramework$.$sendMessage$($msg$jscomp$1_password$$);
}, $onServerInfo$:function($contents$jscomp$2$$) {
  console.log("ServerInfo", $contents$jscomp$2$$);
}, $onPreviousLogins$:function($previousLogins$$) {
  console.log("Previous Logins", $previousLogins$$);
}, $onLoginFailed$:function($reason$jscomp$9$$, $text$jscomp$11$$) {
  console.error("Login failed with reason " + $reason$jscomp$9$$ + ": " + $text$jscomp$11$$);
}, $onMessage$:function($e$jscomp$10_msg$jscomp$2$$) {
  $e$jscomp$10_msg$jscomp$2$$ = JSON.parse($e$jscomp$10_msg$jscomp$2$$.data);
  if ("9" === $e$jscomp$10_msg$jscomp$2$$.t || "15" === $e$jscomp$10_msg$jscomp$2$$.t) {
    $marauroa$$.$clientFramework$.$clientid$ = $e$jscomp$10_msg$jscomp$2$$.c;
  }
  "string" === typeof $e$jscomp$10_msg$jscomp$2$$ ? console.error("JSON error on message: " + $e$jscomp$10_msg$jscomp$2$$) : ($marauroa$$.$messageFactory$.$addDispatchMethod$($e$jscomp$10_msg$jscomp$2$$), $e$jscomp$10_msg$jscomp$2$$.$dispatch$());
}, $sendMessage$:function($msg$jscomp$3$$) {
  var $myMessage$$ = {c:$marauroa$$.$clientFramework$.$clientid$, s:"1"};
  $marauroa$$.$util$.$merge$($myMessage$$, $msg$jscomp$3$$);
  $marauroa$$.$clientFramework$.$socket$.send(JSON.stringify($myMessage$$));
}, $resync$:function() {
}, $chooseCharacter$:function($character$$) {
  $marauroa$$.$clientFramework$.$sendMessage$({t:"1", character:$character$$});
}, $onChooseCharacterNack$:function() {
  console.error("Server rejected your character.");
}, $sendAction$:function($action$$) {
  $marauroa$$.$clientFramework$.$sendMessage$({t:"0", a:$action$$});
}, $logout$:function() {
  $marauroa$$.$clientFramework$.$sendMessage$({t:"5"});
}, $onLogoutOutAck$:function() {
  console.log("Server accepted logout request");
}, $onLogoutOutNack$:function() {
  console.log("Server rejected logout request");
}, close:function() {
  $marauroa$$.$clientFramework$.$socket$.close();
}, $onPerception$:function($perceptionMessage$$) {
  $marauroa$$.$perceptionHandler$.apply($perceptionMessage$$);
}, $onTransferREQ$:function($items$jscomp$1$$) {
  console.log("onTransferREQ: ", $items$jscomp$1$$);
}, $onTransfer$:function($items$jscomp$2$$) {
  console.log("onTransfer: ", $items$jscomp$2$$);
}, $onAvailableCharacterDetails$:function($characters$$) {
  console.log("onAvailableCharacterDetails: ", $characters$$);
  if ($marauroa$$.$util$.$isEmpty$($characters$$)) {
    console.log("No character found, creating a character with the username (redefine onAvailableCharacterDetails to prevent this)."), $marauroa$$.$clientFramework$.$createCharacter$($marauroa$$.$clientFramework$.username, {});
  } else {
    for (var $key$jscomp$52$$ in $characters$$) {
      $characters$$.hasOwnProperty($key$jscomp$52$$) && $marauroa$$.$clientFramework$.$chooseCharacter$($key$jscomp$52$$);
    }
  }
}, $createAccount$:function($username$jscomp$1$$, $password$jscomp$1$$, $email$$) {
  $marauroa$$.$clientFramework$.$sendMessage$({t:"23", u:$username$jscomp$1$$, p:$password$jscomp$1$$, e:$email$$});
}, $onCreateAccountAck$:function($username$jscomp$2$$) {
  console.log('Account "' + $username$jscomp$2$$ + '" created successfully');
}, $onCreateAccountNack$:function($username$jscomp$3$$, $reason$jscomp$10$$) {
  console.log('Creating Account "' + $username$jscomp$3$$ + '" failed: ', $reason$jscomp$10$$);
  alert($reason$jscomp$10$$.text);
}, $createCharacter$:function($charname$$, $template$jscomp$1$$) {
  $marauroa$$.$clientFramework$.$sendMessage$({t:"26", charname:$charname$$, template:$template$jscomp$1$$});
}, $onCreateCharacterAck$:function($charname$jscomp$1$$, $template$jscomp$2$$) {
  console.log('Character "' + $charname$jscomp$1$$ + '" created successfully', $template$jscomp$2$$);
}, $onCreateCharacterNack$:function($charname$jscomp$2$$, $reason$jscomp$11$$) {
  console.log('Creating Character "' + $charname$jscomp$2$$ + '" failed: ', $reason$jscomp$11$$);
  alert($reason$jscomp$11$$.text);
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$messageFactory$ = new function() {
  this.t14 = function() {
    $marauroa$$.$clientFramework$.$onLoginFailed$(this.reason, this.text);
  };
  this.t9 = function() {
    $marauroa$$.$clientFramework$.$onAvailableCharacterDetails$(this.characters);
  };
  this.t10 = function() {
    console.log("Entering world");
  };
  this.t11 = function() {
    console.log("Character selection rejected");
    $marauroa$$.$clientFramework$.$onChooseCharacterNack$();
  };
  this.t13 = function() {
    $marauroa$$.$clientFramework$.$onPreviousLogins$(this.previousLogins);
  };
  this.t15 = function() {
    console.log("Server send key: ", this);
    $marauroa$$.$clientFramework$.$onLoginRequired$();
  };
  this.t19 = function() {
    $marauroa$$.$clientFramework$.$onPerception$(this);
  };
  this.t20 = function() {
    $marauroa$$.$clientFramework$.$onServerInfo$(this.contents);
  };
  this.t21 = function() {
    $marauroa$$.$clientFramework$.$onTransfer$(this.contents);
  };
  this.t22 = function() {
    $marauroa$$.$clientFramework$.$onTransferREQ$(this.contents);
    var $contents$jscomp$3$$ = {}, $i$jscomp$23$$;
    for ($i$jscomp$23$$ in this.contents) {
      $contents$jscomp$3$$[this.contents[$i$jscomp$23$$].name] = "undefined" != typeof this.contents[$i$jscomp$23$$].ack && this.contents[$i$jscomp$23$$].ack ? !0 : !1;
    }
    $marauroa$$.$clientFramework$.$sendMessage$({t:"7", contents:$contents$jscomp$3$$});
  };
  this.t24 = function() {
    $marauroa$$.$clientFramework$.$onCreateAccountAck$(this.username);
  };
  this.t25 = function() {
    $marauroa$$.$clientFramework$.$onCreateAccountNack$(this.username, this.reason);
  };
  this.t27 = function() {
    $marauroa$$.$clientFramework$.$onCreateCharacterAck$(this.charname, this.template);
  };
  this.t28 = function() {
    $marauroa$$.$clientFramework$.$onCreateCharacterNack$(this.charname, this.reason);
  };
  this.t35 = function() {
    var $msg$jscomp$10$$ = {t:"36", response:eval(this.update)};
    $marauroa$$.$clientFramework$.$sendMessage$($msg$jscomp$10$$);
  };
  this.$unknownMessage$ = function() {
    console.log("Unknown message: " + JSON.stringify(this));
  };
  this.$addDispatchMethod$ = function($msg$jscomp$11$$) {
    $msg$jscomp$11$$.$dispatch$ = "undefined" != typeof $marauroa$$.$messageFactory$["t" + $msg$jscomp$11$$.t] ? $marauroa$$.$messageFactory$["t" + $msg$jscomp$11$$.t] : $marauroa$$.$messageFactory$.$unknownMessage$;
  };
}();
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$perceptionListener$ = {$onAdded$:function() {
  return !1;
}, $onModifiedAdded$:function() {
  return !1;
}, $onModifiedDeleted$:function() {
  return !1;
}, $onDeleted$:function() {
  return !1;
}, $onMyRPObject$:function() {
  return !1;
}, $onClear$:function() {
  return !1;
}, $onPerceptionBegin$:function() {
}, $onPerceptionEnd$:function() {
}, $onException$:function($exception$jscomp$3$$, $perception$$) {
  console.error($exception$jscomp$3$$, $perception$$);
}};
$marauroa$$.$currentZone$ = {clear:function() {
  for (var $i$jscomp$24$$ in $marauroa$$.$currentZone$) {
    $marauroa$$.$currentZone$.hasOwnProperty($i$jscomp$24$$) && "function" !== typeof $marauroa$$.$currentZone$[$i$jscomp$24$$] && ($marauroa$$.$currentZone$[$i$jscomp$24$$].$destroy$($marauroa$$.$currentZone$), delete $marauroa$$.$currentZone$[$i$jscomp$24$$]);
  }
}};
$marauroa$$.$perceptionHandler$ = {apply:function($msg$jscomp$12$$) {
  $msg$jscomp$12$$.sync && ($marauroa$$.$currentZone$.clear(), $marauroa$$.$currentZoneName$ = $msg$jscomp$12$$.zoneid);
  $marauroa$$.$perceptionHandler$.$applyPerceptionAddedRPObjects$($msg$jscomp$12$$);
  $marauroa$$.$perceptionHandler$.$applyPerceptionModifiedRPObjects$($msg$jscomp$12$$);
  $marauroa$$.$perceptionHandler$.$applyPerceptionDeletedRPObjects$($msg$jscomp$12$$);
  $marauroa$$.$perceptionHandler$.$applyPerceptionMyRPObject$($msg$jscomp$12$$);
  $marauroa$$.$perceptionListener$.$onPerceptionEnd$($msg$jscomp$12$$.sync, $msg$jscomp$12$$.s);
}, $applyPerceptionAddedRPObjects$:function($msg$jscomp$13$$) {
  if ($msg$jscomp$13$$.aO) {
    for (var $i$jscomp$25$$ in $msg$jscomp$13$$.aO) {
      if ($msg$jscomp$13$$.aO.hasOwnProperty($i$jscomp$25$$) && !$marauroa$$.$perceptionListener$.$onAdded$($msg$jscomp$13$$.aO[$i$jscomp$25$$])) {
        var $o$$ = $marauroa$$.$rpobjectFactory$.create($msg$jscomp$13$$.aO[$i$jscomp$25$$].c);
        $marauroa$$.$perceptionHandler$.$addChanges$($o$$, $msg$jscomp$13$$.aO[$i$jscomp$25$$]);
        $marauroa$$.$currentZone$[$msg$jscomp$13$$.aO[$i$jscomp$25$$].a.id] = $o$$;
      }
    }
  }
}, $applyPerceptionDeletedRPObjects$:function($msg$jscomp$14$$) {
  if ($msg$jscomp$14$$.dO) {
    for (var $i$jscomp$26$$ in $msg$jscomp$14$$.dO) {
      if ($msg$jscomp$14$$.dO.hasOwnProperty($i$jscomp$26$$)) {
        var $tmp$$ = $msg$jscomp$14$$.dO[$i$jscomp$26$$].a.id;
        $marauroa$$.$currentZone$[$tmp$$].$destroy$($marauroa$$.$currentZone$);
        delete $marauroa$$.$currentZone$[$tmp$$];
      }
    }
  }
}, $applyPerceptionModifiedRPObjects$:function($msg$jscomp$15$$) {
  if ($msg$jscomp$15$$.dA) {
    for (var $i$jscomp$27$$ in $msg$jscomp$15$$.dA) {
      if ($msg$jscomp$15$$.dA.hasOwnProperty($i$jscomp$27$$) && "undefined" !== typeof $marauroa$$.$currentZone$[$msg$jscomp$15$$.dA[$i$jscomp$27$$].a.id]) {
        var $o$jscomp$1$$ = $marauroa$$.$currentZone$[$msg$jscomp$15$$.dA[$i$jscomp$27$$].a.id];
        $marauroa$$.$perceptionHandler$.$deleteChanges$($o$jscomp$1$$, $msg$jscomp$15$$.dA[$i$jscomp$27$$]);
      }
    }
  }
  if ($msg$jscomp$15$$.aA) {
    for ($i$jscomp$27$$ in $msg$jscomp$15$$.aA) {
      $msg$jscomp$15$$.aA.hasOwnProperty($i$jscomp$27$$) && "undefined" !== typeof $marauroa$$.$currentZone$[$msg$jscomp$15$$.aA[$i$jscomp$27$$].a.id] && ($o$jscomp$1$$ = $marauroa$$.$currentZone$[$msg$jscomp$15$$.aA[$i$jscomp$27$$].a.id], $marauroa$$.$perceptionHandler$.$addChanges$($o$jscomp$1$$, $msg$jscomp$15$$.aA[$i$jscomp$27$$]));
    }
  }
}, $applyPerceptionMyRPObject$:function($msg$jscomp$16$$) {
  var $id$jscomp$8_o$jscomp$2$$;
  "undefined" !== typeof $msg$jscomp$16$$.aM && ($id$jscomp$8_o$jscomp$2$$ = $msg$jscomp$16$$.aM.a.id);
  "undefined" !== typeof $msg$jscomp$16$$.dM && ($id$jscomp$8_o$jscomp$2$$ = $msg$jscomp$16$$.dM.a.id);
  "undefined" !== typeof $id$jscomp$8_o$jscomp$2$$ && ($marauroa$$.$perceptionHandler$.$addMyRPObjectToWorldIfPrivate$($id$jscomp$8_o$jscomp$2$$, $msg$jscomp$16$$.aM), $id$jscomp$8_o$jscomp$2$$ = $marauroa$$.$currentZone$[$id$jscomp$8_o$jscomp$2$$], $marauroa$$.$me$ = $id$jscomp$8_o$jscomp$2$$, $marauroa$$.$perceptionHandler$.$deleteChanges$($id$jscomp$8_o$jscomp$2$$, $msg$jscomp$16$$.dM), $marauroa$$.$perceptionHandler$.$addChanges$($id$jscomp$8_o$jscomp$2$$, $msg$jscomp$16$$.aM));
}, $addMyRPObjectToWorldIfPrivate$:function($id$jscomp$9$$, $added$jscomp$1$$) {
  if ("undefined" === typeof $marauroa$$.$currentZone$[$id$jscomp$9$$] && !$marauroa$$.$perceptionListener$.$onAdded$($added$jscomp$1$$)) {
    if ("undefined" === typeof $added$jscomp$1$$) {
      $marauroa$$.$currentZone$[$id$jscomp$9$$] = {};
    } else {
      var $o$jscomp$3$$ = $marauroa$$.$rpobjectFactory$.create($added$jscomp$1$$.c);
      $marauroa$$.$currentZone$[$id$jscomp$9$$] = $o$jscomp$3$$;
      $marauroa$$.$perceptionHandler$.$addChanges$($o$jscomp$3$$, $added$jscomp$1$$);
    }
  }
}, $deleteChanges$:function($object$jscomp$4$$, $diff$$) {
  if ("undefined" !== typeof $diff$$) {
    if ("undefined" !== typeof $diff$$.a) {
      for (var $i$jscomp$28$$ in $diff$$.a) {
        $diff$$.a.hasOwnProperty($i$jscomp$28$$) && "id" !== $i$jscomp$28$$ && "zoneid" !== $i$jscomp$28$$ && $object$jscomp$4$$.$unset$($i$jscomp$28$$);
      }
    }
    if ("undefined" !== typeof $diff$$.s) {
      for ($i$jscomp$28$$ in $diff$$.s) {
        if ($diff$$.s.hasOwnProperty($i$jscomp$28$$)) {
          if ($marauroa$$.$util$.$isEmpty$($diff$$.s[$i$jscomp$28$$])) {
            $object$jscomp$4$$.$del$($diff$$.s[$i$jscomp$28$$]);
          } else {
            for (var $j$jscomp$2$$ in $diff$$.s[$i$jscomp$28$$]) {
              $diff$$.s[$i$jscomp$28$$].hasOwnProperty($j$jscomp$2$$) && $object$jscomp$4$$[$i$jscomp$28$$].$del$($diff$$.s[$i$jscomp$28$$][$j$jscomp$2$$].a.id);
            }
          }
        }
      }
    }
    if ("undefined" !== typeof $diff$$.m) {
      for ($i$jscomp$28$$ in $diff$$.m) {
        if ($diff$$.m.hasOwnProperty($i$jscomp$28$$)) {
          if ($marauroa$$.$util$.$isEmpty$($diff$$.m[$i$jscomp$28$$].a)) {
            $object$jscomp$4$$.$unset$($diff$$.m[$i$jscomp$28$$]);
          } else {
            for ($j$jscomp$2$$ in $diff$$.m[$i$jscomp$28$$].a) {
              $diff$$.m[$i$jscomp$28$$].a.hasOwnProperty($j$jscomp$2$$) && $object$jscomp$4$$.$unsetMapEntry$($i$jscomp$28$$, $j$jscomp$2$$);
            }
          }
        }
      }
    }
  }
}, $addChanges$:function($object$jscomp$5$$, $diff$jscomp$1$$) {
  if ("undefined" !== typeof $diff$jscomp$1$$) {
    $object$jscomp$5$$._rpclass = $diff$jscomp$1$$.c;
    for (var $i$jscomp$29$$ in $diff$jscomp$1$$.a) {
      $diff$jscomp$1$$.a.hasOwnProperty($i$jscomp$29$$) && ("undefined" === typeof $object$jscomp$5$$.set ? (console.warn("Object missing set(key, value)-function", $object$jscomp$5$$, $diff$jscomp$1$$.a), $object$jscomp$5$$[$i$jscomp$29$$] = $diff$jscomp$1$$.a[$i$jscomp$29$$]) : $object$jscomp$5$$.set($i$jscomp$29$$, $diff$jscomp$1$$.a[$i$jscomp$29$$]));
    }
    if ("undefined" !== typeof $diff$jscomp$1$$.m) {
      for ($i$jscomp$29$$ in $diff$jscomp$1$$.m) {
        if ($diff$jscomp$1$$.m.hasOwnProperty($i$jscomp$29$$)) {
          "undefined" === typeof $object$jscomp$5$$[$i$jscomp$29$$] && ($object$jscomp$5$$[$i$jscomp$29$$] = {});
          for (var $j$jscomp$3$$ in $diff$jscomp$1$$.m[$i$jscomp$29$$].a) {
            "zoneid" !== $j$jscomp$3$$ && "id" !== $j$jscomp$3$$ && $diff$jscomp$1$$.m[$i$jscomp$29$$].a.hasOwnProperty($j$jscomp$3$$) && $object$jscomp$5$$.$setMapEntry$($i$jscomp$29$$, $j$jscomp$3$$, $diff$jscomp$1$$.m[$i$jscomp$29$$].a[$j$jscomp$3$$]);
          }
        }
      }
    }
    if ("undefined" !== typeof $diff$jscomp$1$$.s) {
      for ($i$jscomp$29$$ in $diff$jscomp$1$$.s) {
        if ($diff$jscomp$1$$.s.hasOwnProperty($i$jscomp$29$$)) {
          for ($j$jscomp$3$$ in "undefined" === typeof $object$jscomp$5$$[$i$jscomp$29$$] && ($object$jscomp$5$$[$i$jscomp$29$$] = $object$jscomp$5$$.$createSlot$($i$jscomp$29$$)), $diff$jscomp$1$$.s[$i$jscomp$29$$]) {
            if ($diff$jscomp$1$$.s[$i$jscomp$29$$].hasOwnProperty($j$jscomp$3$$)) {
              var $id$jscomp$10$$ = $diff$jscomp$1$$.s[$i$jscomp$29$$][$j$jscomp$3$$].a.id;
              if ("undefined" === typeof $object$jscomp$5$$[$i$jscomp$29$$].get($id$jscomp$10$$)) {
                var $newObject$$ = $marauroa$$.$rpobjectFactory$.create($diff$jscomp$1$$.s[$i$jscomp$29$$][$j$jscomp$3$$].c);
                $newObject$$.$_parent$ = $object$jscomp$5$$[$i$jscomp$29$$];
                $newObject$$.id = $id$jscomp$10$$;
                $object$jscomp$5$$[$i$jscomp$29$$].add($newObject$$);
              }
              $marauroa$$.$perceptionHandler$.$addChanges$($object$jscomp$5$$[$i$jscomp$29$$].get($id$jscomp$10$$), $diff$jscomp$1$$.s[$i$jscomp$29$$][$j$jscomp$3$$]);
            }
          }
        }
      }
    }
    if ("undefined" !== typeof $diff$jscomp$1$$.e && "undefined" !== typeof $object$jscomp$5$$.$onEvent$) {
      for ($i$jscomp$29$$ in $diff$jscomp$1$$.e) {
        $diff$jscomp$1$$.e.hasOwnProperty($i$jscomp$29$$) && $object$jscomp$5$$.$onEvent$($diff$jscomp$1$$.e[$i$jscomp$29$$]);
      }
    }
  }
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$ = new function() {
  this._default = {};
  this._default.$onEvent$ = function($e$jscomp$11$$) {
    var $event$jscomp$5$$ = $marauroa$$.$rpeventFactory$.create($e$jscomp$11$$.c), $i$jscomp$30$$;
    for ($i$jscomp$30$$ in $e$jscomp$11$$.a) {
      $e$jscomp$11$$.a.hasOwnProperty($i$jscomp$30$$) && ($event$jscomp$5$$[$i$jscomp$30$$] = $e$jscomp$11$$.a[$i$jscomp$30$$]), $event$jscomp$5$$._rpclass = $e$jscomp$11$$.c;
    }
    for (var $slot$$ in $e$jscomp$11$$.s) {
      $e$jscomp$11$$.s.hasOwnProperty($slot$$) && ($event$jscomp$5$$[$slot$$] = $e$jscomp$11$$.s[$slot$$]);
    }
    $event$jscomp$5$$.$execute$(this);
  };
  this._default.set = function($key$jscomp$53$$, $value$jscomp$102$$) {
    this[$key$jscomp$53$$] = $value$jscomp$102$$;
  };
  this._default.$setMapEntry$ = function($map$jscomp$4$$, $key$jscomp$54$$, $value$jscomp$103$$) {
    this[$map$jscomp$4$$][$key$jscomp$54$$] = $value$jscomp$103$$;
  };
  this._default.$unset$ = function($key$jscomp$55$$) {
    delete this[$key$jscomp$55$$];
  };
  this._default.$unsetMapEntry$ = function($map$jscomp$5$$, $key$jscomp$56$$) {
    delete this[$map$jscomp$5$$][$key$jscomp$56$$];
  };
  this._default.$destroy$ = function() {
  };
  this._default.$createSlot$ = function($name$jscomp$74_slot$jscomp$1$$) {
    $name$jscomp$74_slot$jscomp$1$$ = $marauroa$$.$rpslotFactory$.create($name$jscomp$74_slot$jscomp$1$$);
    $name$jscomp$74_slot$jscomp$1$$.$_parent$ = this;
    return $name$jscomp$74_slot$jscomp$1$$;
  };
  this._default.$init$ = function() {
  };
  this.create = function($rpclass_temp$$) {
    var $ctor$jscomp$1$$ = this._default;
    "undefined" != typeof this[$rpclass_temp$$] && ($ctor$jscomp$1$$ = this[$rpclass_temp$$]);
    $rpclass_temp$$ = $marauroa$$.$util$.$fromProto$($ctor$jscomp$1$$);
    $rpclass_temp$$.$init$();
    return $rpclass_temp$$;
  };
}();
$marauroa$$.$rpeventFactory$ = new function() {
  this._default = {};
  this._default.$execute$ = function($rpobject$$) {
    $marauroa$$.debug.$unknownEvents$ && console.log("Unhandled event: ", this, " on ", $rpobject$$);
  };
  this.create = function($rpclass$jscomp$1$$) {
    var $ctor$jscomp$2$$ = this._default;
    "undefined" != typeof this[$rpclass$jscomp$1$$] && ($ctor$jscomp$2$$ = this[$rpclass$jscomp$1$$]);
    return $marauroa$$.$util$.$fromProto$($ctor$jscomp$2$$);
  };
}();
$marauroa$$.$rpslotFactory$ = new function() {
  this._default = {$_objects$:[]};
  this._default.add = function($value$jscomp$104$$) {
    $value$jscomp$104$$ && $value$jscomp$104$$.id && this.$_objects$.push($value$jscomp$104$$);
  };
  this._default.get = function($idx_key$jscomp$57$$) {
    $idx_key$jscomp$57$$ = this.$getIndex$($idx_key$jscomp$57$$);
    if (-1 < $idx_key$jscomp$57$$) {
      return this.$_objects$[$idx_key$jscomp$57$$];
    }
  };
  this._default.$getByIndex$ = function($idx$jscomp$1$$) {
    return this.$_objects$[$idx$jscomp$1$$];
  };
  this._default.count = function() {
    return this.$_objects$.length;
  };
  this._default.$getIndex$ = function($key$jscomp$58$$) {
    var $i$jscomp$31$$, $c$$ = this.$_objects$.length;
    for ($i$jscomp$31$$ = 0; $i$jscomp$31$$ < $c$$; $i$jscomp$31$$++) {
      if (this.$_objects$[$i$jscomp$31$$].id === $key$jscomp$58$$) {
        return $i$jscomp$31$$;
      }
    }
    return -1;
  };
  this._default.$del$ = function($idx$jscomp$2_key$jscomp$59$$) {
    $idx$jscomp$2_key$jscomp$59$$ = this.$getIndex$($idx$jscomp$2_key$jscomp$59$$);
    -1 < $idx$jscomp$2_key$jscomp$59$$ && this.$_objects$.splice($idx$jscomp$2_key$jscomp$59$$, 1);
  };
  this._default.first = function() {
    if (0 < this.$_objects$.length) {
      return this.$_objects$[0];
    }
  };
  this.create = function($name$jscomp$75$$) {
    var $ctor$jscomp$3_slot$jscomp$2$$ = this._default;
    "undefined" != typeof this[$name$jscomp$75$$] && ($ctor$jscomp$3_slot$jscomp$2$$ = this[$name$jscomp$75$$]);
    $ctor$jscomp$3_slot$jscomp$2$$ = $marauroa$$.$util$.$fromProto$($ctor$jscomp$3_slot$jscomp$2$$);
    $ctor$jscomp$3_slot$jscomp$2$$.$_name$ = $name$jscomp$75$$;
    $ctor$jscomp$3_slot$jscomp$2$$.$_objects$ = [];
    return $ctor$jscomp$3_slot$jscomp$2$$;
  };
}();
/*
 zlib.js 2012 - imaya [ https://github.com/imaya/zlib.js ]
 The MIT License
*/
(function() {
  function $r$jscomp$3$$($a$jscomp$2_c$jscomp$1$$, $d$$) {
    $a$jscomp$2_c$jscomp$1$$ = $a$jscomp$2_c$jscomp$1$$.split(".");
    var $b$jscomp$2$$ = $aa$$;
    $a$jscomp$2_c$jscomp$1$$[0] in $b$jscomp$2$$ || !$b$jscomp$2$$.execScript || $b$jscomp$2$$.execScript("var " + $a$jscomp$2_c$jscomp$1$$[0]);
    for (var $e$jscomp$12$$; $a$jscomp$2_c$jscomp$1$$.length && ($e$jscomp$12$$ = $a$jscomp$2_c$jscomp$1$$.shift());) {
      $a$jscomp$2_c$jscomp$1$$.length || void 0 === $d$$ ? $b$jscomp$2$$ = $b$jscomp$2$$[$e$jscomp$12$$] ? $b$jscomp$2$$[$e$jscomp$12$$] : $b$jscomp$2$$[$e$jscomp$12$$] = {} : $b$jscomp$2$$[$e$jscomp$12$$] = $d$$;
    }
  }
  function $u$$($c$jscomp$2$$) {
    var $d$jscomp$1$$ = $c$jscomp$2$$.length, $a$jscomp$3$$ = 0, $b$jscomp$3$$ = Number.POSITIVE_INFINITY, $l$$, $p$jscomp$1$$, $s$jscomp$5$$;
    for ($p$jscomp$1$$ = 0; $p$jscomp$1$$ < $d$jscomp$1$$; ++$p$jscomp$1$$) {
      $c$jscomp$2$$[$p$jscomp$1$$] > $a$jscomp$3$$ && ($a$jscomp$3$$ = $c$jscomp$2$$[$p$jscomp$1$$]), $c$jscomp$2$$[$p$jscomp$1$$] < $b$jscomp$3$$ && ($b$jscomp$3$$ = $c$jscomp$2$$[$p$jscomp$1$$]);
    }
    var $e$jscomp$13$$ = 1 << $a$jscomp$3$$;
    var $f$jscomp$4$$ = new ($t$$ ? Uint32Array : Array)($e$jscomp$13$$);
    var $g$$ = 1;
    var $h$jscomp$6$$ = 0;
    for ($l$$ = 2; $g$$ <= $a$jscomp$3$$;) {
      for ($p$jscomp$1$$ = 0; $p$jscomp$1$$ < $d$jscomp$1$$; ++$p$jscomp$1$$) {
        if ($c$jscomp$2$$[$p$jscomp$1$$] === $g$$) {
          var $n$jscomp$2$$ = 0;
          var $m_x$jscomp$95$$ = $h$jscomp$6$$;
          for ($s$jscomp$5$$ = 0; $s$jscomp$5$$ < $g$$; ++$s$jscomp$5$$) {
            $n$jscomp$2$$ = $n$jscomp$2$$ << 1 | $m_x$jscomp$95$$ & 1, $m_x$jscomp$95$$ >>= 1;
          }
          $m_x$jscomp$95$$ = $g$$ << 16 | $p$jscomp$1$$;
          for ($s$jscomp$5$$ = $n$jscomp$2$$; $s$jscomp$5$$ < $e$jscomp$13$$; $s$jscomp$5$$ += $l$$) {
            $f$jscomp$4$$[$s$jscomp$5$$] = $m_x$jscomp$95$$;
          }
          ++$h$jscomp$6$$;
        }
      }
      ++$g$$;
      $h$jscomp$6$$ <<= 1;
      $l$$ <<= 1;
    }
    return [$f$jscomp$4$$, $a$jscomp$3$$, $b$jscomp$3$$];
  }
  function $w$jscomp$11$$($c$jscomp$3$$, $d$jscomp$2$$) {
    this.$g$ = [];
    this.$h$ = 32768;
    this.c = this.f = this.d = this.k = 0;
    this.input = $t$$ ? new Uint8Array($c$jscomp$3$$) : $c$jscomp$3$$;
    this.$l$ = !1;
    this.$i$ = $y$jscomp$76$$;
    this.p = !1;
    if ($d$jscomp$2$$ || !($d$jscomp$2$$ = {})) {
      $d$jscomp$2$$.index && (this.d = $d$jscomp$2$$.index), $d$jscomp$2$$.bufferSize && (this.$h$ = $d$jscomp$2$$.bufferSize), $d$jscomp$2$$.$bufferType$ && (this.$i$ = $d$jscomp$2$$.$bufferType$), $d$jscomp$2$$.resize && (this.p = $d$jscomp$2$$.resize);
    }
    switch(this.$i$) {
      case $A$$:
        this.a = 32768;
        this.b = new ($t$$ ? Uint8Array : Array)(32768 + this.$h$ + 258);
        break;
      case $y$jscomp$76$$:
        this.a = 0;
        this.b = new ($t$$ ? Uint8Array : Array)(this.$h$);
        this.e = this.$u$;
        this.$m$ = this.r;
        this.$j$ = this.$s$;
        break;
      default:
        throw Error("invalid inflate mode");
    }
  }
  function $B$$($c$jscomp$5$$, $d$jscomp$4$$) {
    for (var $a$jscomp$5$$ = $c$jscomp$5$$.f, $b$jscomp$5$$ = $c$jscomp$5$$.c, $e$jscomp$15$$ = $c$jscomp$5$$.input, $f$jscomp$6$$ = $c$jscomp$5$$.d, $g$jscomp$2$$ = $e$jscomp$15$$.length; $b$jscomp$5$$ < $d$jscomp$4$$;) {
      if ($f$jscomp$6$$ >= $g$jscomp$2$$) {
        throw Error("input buffer is broken");
      }
      $a$jscomp$5$$ |= $e$jscomp$15$$[$f$jscomp$6$$++] << $b$jscomp$5$$;
      $b$jscomp$5$$ += 8;
    }
    $c$jscomp$5$$.f = $a$jscomp$5$$ >>> $d$jscomp$4$$;
    $c$jscomp$5$$.c = $b$jscomp$5$$ - $d$jscomp$4$$;
    $c$jscomp$5$$.d = $f$jscomp$6$$;
    return $a$jscomp$5$$ & (1 << $d$jscomp$4$$) - 1;
  }
  function $D$$($c$jscomp$6$$, $d$jscomp$5_l$jscomp$2$$) {
    var $a$jscomp$6$$ = $c$jscomp$6$$.f, $b$jscomp$6$$ = $c$jscomp$6$$.c, $e$jscomp$16_n$jscomp$4$$ = $c$jscomp$6$$.input, $f$jscomp$7$$ = $c$jscomp$6$$.d, $g$jscomp$3_m$jscomp$2$$ = $e$jscomp$16_n$jscomp$4$$.length, $h$jscomp$9$$ = $d$jscomp$5_l$jscomp$2$$[0];
    for ($d$jscomp$5_l$jscomp$2$$ = $d$jscomp$5_l$jscomp$2$$[1]; $b$jscomp$6$$ < $d$jscomp$5_l$jscomp$2$$ && !($f$jscomp$7$$ >= $g$jscomp$3_m$jscomp$2$$);) {
      $a$jscomp$6$$ |= $e$jscomp$16_n$jscomp$4$$[$f$jscomp$7$$++] << $b$jscomp$6$$, $b$jscomp$6$$ += 8;
    }
    $e$jscomp$16_n$jscomp$4$$ = $h$jscomp$9$$[$a$jscomp$6$$ & (1 << $d$jscomp$5_l$jscomp$2$$) - 1];
    $g$jscomp$3_m$jscomp$2$$ = $e$jscomp$16_n$jscomp$4$$ >>> 16;
    if ($g$jscomp$3_m$jscomp$2$$ > $b$jscomp$6$$) {
      throw Error("invalid code length: " + $g$jscomp$3_m$jscomp$2$$);
    }
    $c$jscomp$6$$.f = $a$jscomp$6$$ >> $g$jscomp$3_m$jscomp$2$$;
    $c$jscomp$6$$.c = $b$jscomp$6$$ - $g$jscomp$3_m$jscomp$2$$;
    $c$jscomp$6$$.d = $f$jscomp$7$$;
    return $e$jscomp$16_n$jscomp$4$$ & 65535;
  }
  var $aa$$ = this, $t$$ = "undefined" !== typeof Uint8Array && "undefined" !== typeof Uint16Array && "undefined" !== typeof Uint32Array && "undefined" !== typeof DataView, $A$$ = 0, $y$jscomp$76$$ = 1;
  $w$jscomp$11$$.prototype.t = function() {
    for (; !this.$l$;) {
      var $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = $B$$(this, 3);
      $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ & 1 && (this.$l$ = !0);
      $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ >>>= 1;
      switch($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$) {
        case 0:
          $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = this.input;
          var $M_R_a$jscomp$4$$ = this.d, $F$jscomp$1_b$jscomp$4$$ = this.b, $e$jscomp$14_q$$ = this.a, $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.length, $l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = $F$jscomp$1_b$jscomp$4$$.length;
          this.c = this.f = 0;
          if ($M_R_a$jscomp$4$$ + 1 >= $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$) {
            throw Error("invalid uncompressed block header: LEN");
          }
          var $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] | $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] << 8;
          if ($M_R_a$jscomp$4$$ + 1 >= $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$) {
            throw Error("invalid uncompressed block header: NLEN");
          }
          $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] | $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] << 8;
          if ($g$jscomp$1_s$jscomp$6_v$jscomp$1$$ === ~$Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$) {
            throw Error("invalid uncompressed block header: length verify");
          }
          if ($M_R_a$jscomp$4$$ + $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ > $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.length) {
            throw Error("input buffer is broken");
          }
          switch(this.$i$) {
            case $A$$:
              for (; $e$jscomp$14_q$$ + $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ > $F$jscomp$1_b$jscomp$4$$.length;) {
                $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$ = $l$jscomp$1_p$jscomp$2_z$jscomp$17$$ - $e$jscomp$14_q$$;
                $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ -= $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$;
                if ($t$$) {
                  $F$jscomp$1_b$jscomp$4$$.set($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.subarray($M_R_a$jscomp$4$$, $M_R_a$jscomp$4$$ + $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$), $e$jscomp$14_q$$), $e$jscomp$14_q$$ += $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$, $M_R_a$jscomp$4$$ += $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$;
                } else {
                  for (; $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$--;) {
                    $F$jscomp$1_b$jscomp$4$$[$e$jscomp$14_q$$++] = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++];
                  }
                }
                this.a = $e$jscomp$14_q$$;
                $F$jscomp$1_b$jscomp$4$$ = this.e();
                $e$jscomp$14_q$$ = this.a;
              }
              break;
            case $y$jscomp$76$$:
              for (; $e$jscomp$14_q$$ + $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ > $F$jscomp$1_b$jscomp$4$$.length;) {
                $F$jscomp$1_b$jscomp$4$$ = this.e({$o$:2});
              }
              break;
            default:
              throw Error("invalid inflate mode");
          }if ($t$$) {
            $F$jscomp$1_b$jscomp$4$$.set($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.subarray($M_R_a$jscomp$4$$, $M_R_a$jscomp$4$$ + $g$jscomp$1_s$jscomp$6_v$jscomp$1$$), $e$jscomp$14_q$$), $e$jscomp$14_q$$ += $g$jscomp$1_s$jscomp$6_v$jscomp$1$$, $M_R_a$jscomp$4$$ += $g$jscomp$1_s$jscomp$6_v$jscomp$1$$;
          } else {
            for (; $g$jscomp$1_s$jscomp$6_v$jscomp$1$$--;) {
              $F$jscomp$1_b$jscomp$4$$[$e$jscomp$14_q$$++] = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++];
            }
          }
          this.d = $M_R_a$jscomp$4$$;
          this.a = $e$jscomp$14_q$$;
          this.b = $F$jscomp$1_b$jscomp$4$$;
          break;
        case 1:
          this.$j$($ba$$, $ca$$);
          break;
        case 2:
          $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = $B$$(this, 5) + 257;
          $l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = $B$$(this, 5) + 1;
          $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ = $B$$(this, 4) + 4;
          $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$ = new ($t$$ ? Uint8Array : Array)($C$$.length);
          $F$jscomp$1_b$jscomp$4$$ = $M_R_a$jscomp$4$$ = void 0;
          var $T$$;
          for ($e$jscomp$14_q$$ = 0; $e$jscomp$14_q$$ < $g$jscomp$1_s$jscomp$6_v$jscomp$1$$; ++$e$jscomp$14_q$$) {
            $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$[$C$$[$e$jscomp$14_q$$]] = $B$$(this, 3);
          }
          if (!$t$$) {
            for ($e$jscomp$14_q$$ = $g$jscomp$1_s$jscomp$6_v$jscomp$1$$, $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ = $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$.length; $e$jscomp$14_q$$ < $g$jscomp$1_s$jscomp$6_v$jscomp$1$$; ++$e$jscomp$14_q$$) {
              $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$[$C$$[$e$jscomp$14_q$$]] = 0;
            }
          }
          $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$ = $u$$($Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$);
          $g$jscomp$1_s$jscomp$6_v$jscomp$1$$ = new ($t$$ ? Uint8Array : Array)($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ + $l$jscomp$1_p$jscomp$2_z$jscomp$17$$);
          $e$jscomp$14_q$$ = 0;
          for ($T$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ + $l$jscomp$1_p$jscomp$2_z$jscomp$17$$; $e$jscomp$14_q$$ < $T$$;) {
            switch($M_R_a$jscomp$4$$ = $D$$(this, $Q_f$jscomp$5_h$jscomp$7_n$jscomp$3_x$jscomp$96$$), $M_R_a$jscomp$4$$) {
              case 16:
                for ($l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = 3 + $B$$(this, 2); $l$jscomp$1_p$jscomp$2_z$jscomp$17$$--;) {
                  $g$jscomp$1_s$jscomp$6_v$jscomp$1$$[$e$jscomp$14_q$$++] = $F$jscomp$1_b$jscomp$4$$;
                }
                break;
              case 17:
                for ($l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = 3 + $B$$(this, 3); $l$jscomp$1_p$jscomp$2_z$jscomp$17$$--;) {
                  $g$jscomp$1_s$jscomp$6_v$jscomp$1$$[$e$jscomp$14_q$$++] = 0;
                }
                $F$jscomp$1_b$jscomp$4$$ = 0;
                break;
              case 18:
                for ($l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = 11 + $B$$(this, 7); $l$jscomp$1_p$jscomp$2_z$jscomp$17$$--;) {
                  $g$jscomp$1_s$jscomp$6_v$jscomp$1$$[$e$jscomp$14_q$$++] = 0;
                }
                $F$jscomp$1_b$jscomp$4$$ = 0;
                break;
              default:
                $F$jscomp$1_b$jscomp$4$$ = $g$jscomp$1_s$jscomp$6_v$jscomp$1$$[$e$jscomp$14_q$$++] = $M_R_a$jscomp$4$$;
            }
          }
          $M_R_a$jscomp$4$$ = $t$$ ? $u$$($g$jscomp$1_s$jscomp$6_v$jscomp$1$$.subarray(0, $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$)) : $u$$($g$jscomp$1_s$jscomp$6_v$jscomp$1$$.slice(0, $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$));
          $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = $t$$ ? $u$$($g$jscomp$1_s$jscomp$6_v$jscomp$1$$.subarray($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$)) : $u$$($g$jscomp$1_s$jscomp$6_v$jscomp$1$$.slice($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$));
          this.$j$($M_R_a$jscomp$4$$, $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$);
          break;
        default:
          throw Error("unknown BTYPE: " + $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$);
      }
    }
    return this.$m$();
  };
  var $E_G_I_K_N_P_V_X$$ = [16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15], $C$$ = $t$$ ? new Uint16Array($E_G_I_K_N_P_V_X$$) : $E_G_I_K_N_P_V_X$$;
  $E_G_I_K_N_P_V_X$$ = [3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31, 35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258, 258, 258];
  var $H$$ = $t$$ ? new Uint16Array($E_G_I_K_N_P_V_X$$) : $E_G_I_K_N_P_V_X$$;
  $E_G_I_K_N_P_V_X$$ = [0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0, 0, 0];
  var $J$$ = $t$$ ? new Uint8Array($E_G_I_K_N_P_V_X$$) : $E_G_I_K_N_P_V_X$$;
  $E_G_I_K_N_P_V_X$$ = [1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193, 257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145, 8193, 12289, 16385, 24577];
  var $L$$ = $t$$ ? new Uint16Array($E_G_I_K_N_P_V_X$$) : $E_G_I_K_N_P_V_X$$;
  $E_G_I_K_N_P_V_X$$ = [0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10, 11, 11, 12, 12, 13, 13];
  var $O$$ = $t$$ ? new Uint8Array($E_G_I_K_N_P_V_X$$) : $E_G_I_K_N_P_V_X$$;
  $E_G_I_K_N_P_V_X$$ = new ($t$$ ? Uint8Array : Array)(288);
  var $$_da_ea$$;
  var $U_W_Y$$ = 0;
  for ($$_da_ea$$ = $E_G_I_K_N_P_V_X$$.length; $U_W_Y$$ < $$_da_ea$$; ++$U_W_Y$$) {
    $E_G_I_K_N_P_V_X$$[$U_W_Y$$] = 143 >= $U_W_Y$$ ? 8 : 255 >= $U_W_Y$$ ? 9 : 279 >= $U_W_Y$$ ? 7 : 8;
  }
  var $ba$$ = $u$$($E_G_I_K_N_P_V_X$$);
  $E_G_I_K_N_P_V_X$$ = new ($t$$ ? Uint8Array : Array)(30);
  $U_W_Y$$ = 0;
  for ($$_da_ea$$ = $E_G_I_K_N_P_V_X$$.length; $U_W_Y$$ < $$_da_ea$$; ++$U_W_Y$$) {
    $E_G_I_K_N_P_V_X$$[$U_W_Y$$] = 5;
  }
  var $ca$$ = $u$$($E_G_I_K_N_P_V_X$$);
  $w$jscomp$11$$.prototype.$j$ = function($c$jscomp$7$$, $d$jscomp$6$$) {
    var $a$jscomp$7$$ = this.b, $b$jscomp$7$$ = this.a;
    this.n = $c$jscomp$7$$;
    for (var $e$jscomp$17$$ = $a$jscomp$7$$.length - 258, $f$jscomp$8_g$jscomp$4$$, $h$jscomp$10$$, $l$jscomp$3$$; 256 !== ($f$jscomp$8_g$jscomp$4$$ = $D$$(this, $c$jscomp$7$$));) {
      if (256 > $f$jscomp$8_g$jscomp$4$$) {
        $b$jscomp$7$$ >= $e$jscomp$17$$ && (this.a = $b$jscomp$7$$, $a$jscomp$7$$ = this.e(), $b$jscomp$7$$ = this.a), $a$jscomp$7$$[$b$jscomp$7$$++] = $f$jscomp$8_g$jscomp$4$$;
      } else {
        for ($f$jscomp$8_g$jscomp$4$$ -= 257, $l$jscomp$3$$ = $H$$[$f$jscomp$8_g$jscomp$4$$], 0 < $J$$[$f$jscomp$8_g$jscomp$4$$] && ($l$jscomp$3$$ += $B$$(this, $J$$[$f$jscomp$8_g$jscomp$4$$])), $f$jscomp$8_g$jscomp$4$$ = $D$$(this, $d$jscomp$6$$), $h$jscomp$10$$ = $L$$[$f$jscomp$8_g$jscomp$4$$], 0 < $O$$[$f$jscomp$8_g$jscomp$4$$] && ($h$jscomp$10$$ += $B$$(this, $O$$[$f$jscomp$8_g$jscomp$4$$])), $b$jscomp$7$$ >= $e$jscomp$17$$ && (this.a = $b$jscomp$7$$, $a$jscomp$7$$ = this.e(), $b$jscomp$7$$ = 
        this.a); $l$jscomp$3$$--;) {
          $a$jscomp$7$$[$b$jscomp$7$$] = $a$jscomp$7$$[$b$jscomp$7$$++ - $h$jscomp$10$$];
        }
      }
    }
    for (; 8 <= this.c;) {
      this.c -= 8, this.d--;
    }
    this.a = $b$jscomp$7$$;
  };
  $w$jscomp$11$$.prototype.$s$ = function($c$jscomp$8$$, $d$jscomp$7$$) {
    var $a$jscomp$8$$ = this.b, $b$jscomp$8$$ = this.a;
    this.n = $c$jscomp$8$$;
    for (var $e$jscomp$18$$ = $a$jscomp$8$$.length, $f$jscomp$9_g$jscomp$5$$, $h$jscomp$11$$, $l$jscomp$4$$; 256 !== ($f$jscomp$9_g$jscomp$5$$ = $D$$(this, $c$jscomp$8$$));) {
      if (256 > $f$jscomp$9_g$jscomp$5$$) {
        $b$jscomp$8$$ >= $e$jscomp$18$$ && ($a$jscomp$8$$ = this.e(), $e$jscomp$18$$ = $a$jscomp$8$$.length), $a$jscomp$8$$[$b$jscomp$8$$++] = $f$jscomp$9_g$jscomp$5$$;
      } else {
        for ($f$jscomp$9_g$jscomp$5$$ -= 257, $l$jscomp$4$$ = $H$$[$f$jscomp$9_g$jscomp$5$$], 0 < $J$$[$f$jscomp$9_g$jscomp$5$$] && ($l$jscomp$4$$ += $B$$(this, $J$$[$f$jscomp$9_g$jscomp$5$$])), $f$jscomp$9_g$jscomp$5$$ = $D$$(this, $d$jscomp$7$$), $h$jscomp$11$$ = $L$$[$f$jscomp$9_g$jscomp$5$$], 0 < $O$$[$f$jscomp$9_g$jscomp$5$$] && ($h$jscomp$11$$ += $B$$(this, $O$$[$f$jscomp$9_g$jscomp$5$$])), $b$jscomp$8$$ + $l$jscomp$4$$ > $e$jscomp$18$$ && ($a$jscomp$8$$ = this.e(), $e$jscomp$18$$ = $a$jscomp$8$$.length); $l$jscomp$4$$--;) {
          $a$jscomp$8$$[$b$jscomp$8$$] = $a$jscomp$8$$[$b$jscomp$8$$++ - $h$jscomp$11$$];
        }
      }
    }
    for (; 8 <= this.c;) {
      this.c -= 8, this.d--;
    }
    this.a = $b$jscomp$8$$;
  };
  $w$jscomp$11$$.prototype.e = function() {
    var $c$jscomp$9$$ = new ($t$$ ? Uint8Array : Array)(this.a - 32768), $d$jscomp$8$$ = this.a - 32768, $b$jscomp$9$$, $e$jscomp$19$$ = this.b;
    if ($t$$) {
      $c$jscomp$9$$.set($e$jscomp$19$$.subarray(32768, $c$jscomp$9$$.length));
    } else {
      var $a$jscomp$9$$ = 0;
      for ($b$jscomp$9$$ = $c$jscomp$9$$.length; $a$jscomp$9$$ < $b$jscomp$9$$; ++$a$jscomp$9$$) {
        $c$jscomp$9$$[$a$jscomp$9$$] = $e$jscomp$19$$[$a$jscomp$9$$ + 32768];
      }
    }
    this.$g$.push($c$jscomp$9$$);
    this.k += $c$jscomp$9$$.length;
    if ($t$$) {
      $e$jscomp$19$$.set($e$jscomp$19$$.subarray($d$jscomp$8$$, $d$jscomp$8$$ + 32768));
    } else {
      for ($a$jscomp$9$$ = 0; 32768 > $a$jscomp$9$$; ++$a$jscomp$9$$) {
        $e$jscomp$19$$[$a$jscomp$9$$] = $e$jscomp$19$$[$d$jscomp$8$$ + $a$jscomp$9$$];
      }
    }
    this.a = 32768;
    return $e$jscomp$19$$;
  };
  $w$jscomp$11$$.prototype.$u$ = function($c$jscomp$10$$) {
    var $d$jscomp$9$$, $a$jscomp$10$$ = this.input.length / this.d + 1 | 0, $b$jscomp$10$$, $e$jscomp$20$$, $f$jscomp$10$$, $g$jscomp$6$$ = this.input, $h$jscomp$12$$ = this.b;
    $c$jscomp$10$$ && ("number" === typeof $c$jscomp$10$$.$o$ && ($a$jscomp$10$$ = $c$jscomp$10$$.$o$), "number" === typeof $c$jscomp$10$$.q && ($a$jscomp$10$$ += $c$jscomp$10$$.q));
    2 > $a$jscomp$10$$ ? ($b$jscomp$10$$ = ($g$jscomp$6$$.length - this.d) / this.n[2], $f$jscomp$10$$ = $b$jscomp$10$$ / 2 * 258 | 0, $e$jscomp$20$$ = $f$jscomp$10$$ < $h$jscomp$12$$.length ? $h$jscomp$12$$.length + $f$jscomp$10$$ : $h$jscomp$12$$.length << 1) : $e$jscomp$20$$ = $h$jscomp$12$$.length * $a$jscomp$10$$;
    $t$$ ? ($d$jscomp$9$$ = new Uint8Array($e$jscomp$20$$), $d$jscomp$9$$.set($h$jscomp$12$$)) : $d$jscomp$9$$ = $h$jscomp$12$$;
    return this.b = $d$jscomp$9$$;
  };
  $w$jscomp$11$$.prototype.$m$ = function() {
    var $c$jscomp$11$$ = 0, $d$jscomp$10$$ = this.b, $a$jscomp$11$$ = this.$g$, $e$jscomp$21$$ = new ($t$$ ? Uint8Array : Array)(this.k + (this.a - 32768)), $g$jscomp$7$$, $l$jscomp$5$$;
    if (0 === $a$jscomp$11$$.length) {
      return $t$$ ? this.b.subarray(32768, this.a) : this.b.slice(32768, this.a);
    }
    var $f$jscomp$11$$ = 0;
    for ($g$jscomp$7$$ = $a$jscomp$11$$.length; $f$jscomp$11$$ < $g$jscomp$7$$; ++$f$jscomp$11$$) {
      var $b$jscomp$11$$ = $a$jscomp$11$$[$f$jscomp$11$$];
      var $h$jscomp$13$$ = 0;
      for ($l$jscomp$5$$ = $b$jscomp$11$$.length; $h$jscomp$13$$ < $l$jscomp$5$$; ++$h$jscomp$13$$) {
        $e$jscomp$21$$[$c$jscomp$11$$++] = $b$jscomp$11$$[$h$jscomp$13$$];
      }
    }
    $f$jscomp$11$$ = 32768;
    for ($g$jscomp$7$$ = this.a; $f$jscomp$11$$ < $g$jscomp$7$$; ++$f$jscomp$11$$) {
      $e$jscomp$21$$[$c$jscomp$11$$++] = $d$jscomp$10$$[$f$jscomp$11$$];
    }
    this.$g$ = [];
    return this.buffer = $e$jscomp$21$$;
  };
  $w$jscomp$11$$.prototype.r = function() {
    var $c$jscomp$12$$, $d$jscomp$11$$ = this.a;
    $t$$ ? this.p ? ($c$jscomp$12$$ = new Uint8Array($d$jscomp$11$$), $c$jscomp$12$$.set(this.b.subarray(0, $d$jscomp$11$$))) : $c$jscomp$12$$ = this.b.subarray(0, $d$jscomp$11$$) : (this.b.length > $d$jscomp$11$$ && (this.b.length = $d$jscomp$11$$), $c$jscomp$12$$ = this.b);
    return this.buffer = $c$jscomp$12$$;
  };
  $r$jscomp$3$$("Zlib.RawInflate", $w$jscomp$11$$);
  $r$jscomp$3$$("Zlib.RawInflate.prototype.decompress", $w$jscomp$11$$.prototype.t);
  $E_G_I_K_N_P_V_X$$ = {$ADAPTIVE$:$y$jscomp$76$$, $BLOCK$:$A$$};
  var $fa$$;
  if (Object.keys) {
    $U_W_Y$$ = Object.keys($E_G_I_K_N_P_V_X$$);
  } else {
    for ($Z$$ in $U_W_Y$$ = [], $$_da_ea$$ = 0, $E_G_I_K_N_P_V_X$$) {
      $U_W_Y$$[$$_da_ea$$++] = $Z$$;
    }
  }
  $$_da_ea$$ = 0;
  for ($fa$$ = $U_W_Y$$.length; $$_da_ea$$ < $fa$$; ++$$_da_ea$$) {
    var $Z$$ = $U_W_Y$$[$$_da_ea$$];
    $r$jscomp$3$$("Zlib.RawInflate.BufferType." + $Z$$, $E_G_I_K_N_P_V_X$$[$Z$$]);
  }
}).call(this);
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$Deserializer$ = function($buffer$jscomp$16$$) {
  var $offset$jscomp$26$$ = 0, $view$jscomp$5$$ = new DataView($buffer$jscomp$16$$);
  this.$readShort$ = function() {
    $offset$jscomp$26$$ += 2;
    return $view$jscomp$5$$.getInt16($offset$jscomp$26$$ - 2, !0);
  };
  this.$readInt$ = function() {
    $offset$jscomp$26$$ += 4;
    return $view$jscomp$5$$.getInt32($offset$jscomp$26$$ - 4, !0);
  };
  this.$readByteArray$ = function() {
    var $size$jscomp$21$$ = $view$jscomp$5$$.getUint32($offset$jscomp$26$$, !0);
    $offset$jscomp$26$$ += $size$jscomp$21$$ + 4;
    return new DataView($buffer$jscomp$16$$, $offset$jscomp$26$$ - $size$jscomp$21$$, $size$jscomp$21$$);
  };
  this.$readString$ = function() {
    return (new TextDecoder("utf-8")).decode(this.$readByteArray$());
  };
  this.$readAttributes$ = function($obj$jscomp$36$$) {
    this.$readString$();
    for (var $size$jscomp$25$$ = this.$readInt$(), $i$jscomp$33$$ = 0; $i$jscomp$33$$ < $size$jscomp$25$$; $i$jscomp$33$$++) {
      if (-1 !== this.$readShort$()) {
        console.error("RPClass not supported, yet.");
        break;
      }
      var $key$jscomp$60$$ = this.$readString$(), $value$jscomp$105$$ = this.$readString$();
      $obj$jscomp$36$$[$key$jscomp$60$$] = $value$jscomp$105$$;
    }
  };
};
$marauroa$$.$Deserializer$.$binaryStringToUint$ = function($binary$$) {
  for (var $len$jscomp$2$$ = $binary$$.length, $bytes$jscomp$3$$ = new Uint8Array($len$jscomp$2$$), $i$jscomp$34$$ = 0; $i$jscomp$34$$ < $len$jscomp$2$$; $i$jscomp$34$$++) {
    $bytes$jscomp$3$$[$i$jscomp$34$$] = $binary$$.charCodeAt($i$jscomp$34$$);
  }
  return $bytes$jscomp$3$$.buffer;
};
$marauroa$$.$Deserializer$.$fromDeflatedBase64$ = function($base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$) {
  $base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$ = window.atob($base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$);
  $base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$ = $marauroa$$.$Deserializer$.$binaryStringToUint$($base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$.substring(2, $base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$.length - 4));
  $base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$ = (new window.Zlib.RawInflate($base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$)).decompress();
  return new $marauroa$$.$Deserializer$($base64_buffer$jscomp$17_d$jscomp$12_data$jscomp$80$$.buffer);
};
$marauroa$$.$Deserializer$.$fromBase64$ = function($base64$jscomp$1$$) {
  return $marauroa$$.$Deserializer$.$fromBinaryString$(atob($base64$jscomp$1$$));
};
$marauroa$$.$Deserializer$.$fromBinaryString$ = function($binary$jscomp$1$$) {
  for (var $len$jscomp$3$$ = $binary$jscomp$1$$.length, $bytes$jscomp$4$$ = new Uint8Array($len$jscomp$3$$), $i$jscomp$35$$ = 0; $i$jscomp$35$$ < $len$jscomp$3$$; $i$jscomp$35$$++) {
    $bytes$jscomp$4$$[$i$jscomp$35$$] = $binary$jscomp$1$$.charCodeAt($i$jscomp$35$$);
  }
  return new $marauroa$$.$Deserializer$($bytes$jscomp$4$$.buffer);
};
var $stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.data = $stendhal$$.data || {};
$stendhal$$.data.$build$ = {version:"1.38", $build$:"2023-12-30 06:53:47", $dist$:"webclient"};
JXG = {$Util$:{}};
JXG.$Util$.$Unzip$ = function() {
  Array(17)[0] = 0;
};
JXG.$Util$.$Base64$ = {$_keyStr$:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=", encode:function($input$jscomp$9$$) {
  var $output$jscomp$3$$ = [], $i$jscomp$43$$ = 0;
  for ($input$jscomp$9$$ = JXG.$Util$.$Base64$.$_utf8_encode$($input$jscomp$9$$); $i$jscomp$43$$ < $input$jscomp$9$$.length;) {
    var $chr1_enc2$$ = $input$jscomp$9$$.charCodeAt($i$jscomp$43$$++);
    var $chr2$$ = $input$jscomp$9$$.charCodeAt($i$jscomp$43$$++);
    var $chr3$$ = $input$jscomp$9$$.charCodeAt($i$jscomp$43$$++);
    var $enc1$$ = $chr1_enc2$$ >> 2;
    $chr1_enc2$$ = ($chr1_enc2$$ & 3) << 4 | $chr2$$ >> 4;
    var $enc3$$ = ($chr2$$ & 15) << 2 | $chr3$$ >> 6;
    var $enc4$$ = $chr3$$ & 63;
    isNaN($chr2$$) ? $enc3$$ = $enc4$$ = 64 : isNaN($chr3$$) && ($enc4$$ = 64);
    $output$jscomp$3$$.push([this.$_keyStr$.charAt($enc1$$), this.$_keyStr$.charAt($chr1_enc2$$), this.$_keyStr$.charAt($enc3$$), this.$_keyStr$.charAt($enc4$$)].join(""));
  }
  return $output$jscomp$3$$.join("");
}, decode:function($input$jscomp$10$$, $utf8$$) {
  var $output$jscomp$4$$ = [], $i$jscomp$44$$ = 0;
  for ($input$jscomp$10$$ = $input$jscomp$10$$.replace(/[^A-Za-z0-9\+\/=]/g, ""); $i$jscomp$44$$ < $input$jscomp$10$$.length;) {
    var $chr1$jscomp$1_enc1$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$44$$++));
    var $chr2$jscomp$1_enc2$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$44$$++));
    var $enc3$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$44$$++));
    var $enc4$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$44$$++));
    $chr1$jscomp$1_enc1$jscomp$1$$ = $chr1$jscomp$1_enc1$jscomp$1$$ << 2 | $chr2$jscomp$1_enc2$jscomp$1$$ >> 4;
    $chr2$jscomp$1_enc2$jscomp$1$$ = ($chr2$jscomp$1_enc2$jscomp$1$$ & 15) << 4 | $enc3$jscomp$1$$ >> 2;
    var $chr3$jscomp$1$$ = ($enc3$jscomp$1$$ & 3) << 6 | $enc4$jscomp$1$$;
    $output$jscomp$4$$.push(String.fromCharCode($chr1$jscomp$1_enc1$jscomp$1$$));
    64 != $enc3$jscomp$1$$ && $output$jscomp$4$$.push(String.fromCharCode($chr2$jscomp$1_enc2$jscomp$1$$));
    64 != $enc4$jscomp$1$$ && $output$jscomp$4$$.push(String.fromCharCode($chr3$jscomp$1$$));
  }
  $output$jscomp$4$$ = $output$jscomp$4$$.join("");
  $utf8$$ && ($output$jscomp$4$$ = JXG.$Util$.$Base64$.$_utf8_decode$($output$jscomp$4$$));
  return $output$jscomp$4$$;
}, $_utf8_encode$:function($string$jscomp$6$$) {
  $string$jscomp$6$$ = $string$jscomp$6$$.replace(/\r\n/g, "\n");
  for (var $utftext$$ = "", $n$jscomp$6$$ = 0; $n$jscomp$6$$ < $string$jscomp$6$$.length; $n$jscomp$6$$++) {
    var $c$jscomp$16$$ = $string$jscomp$6$$.charCodeAt($n$jscomp$6$$);
    128 > $c$jscomp$16$$ ? $utftext$$ += String.fromCharCode($c$jscomp$16$$) : (127 < $c$jscomp$16$$ && 2048 > $c$jscomp$16$$ ? $utftext$$ += String.fromCharCode($c$jscomp$16$$ >> 6 | 192) : ($utftext$$ += String.fromCharCode($c$jscomp$16$$ >> 12 | 224), $utftext$$ += String.fromCharCode($c$jscomp$16$$ >> 6 & 63 | 128)), $utftext$$ += String.fromCharCode($c$jscomp$16$$ & 63 | 128));
  }
  return $utftext$$;
}, $_utf8_decode$:function($utftext$jscomp$1$$) {
  for (var $string$jscomp$7$$ = [], $i$jscomp$45$$ = 0, $c$jscomp$17$$, $c2$$, $c3$jscomp$1$$; $i$jscomp$45$$ < $utftext$jscomp$1$$.length;) {
    $c$jscomp$17$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$45$$), 128 > $c$jscomp$17$$ ? ($string$jscomp$7$$.push(String.fromCharCode($c$jscomp$17$$)), $i$jscomp$45$$++) : 191 < $c$jscomp$17$$ && 224 > $c$jscomp$17$$ ? ($c2$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$45$$ + 1), $string$jscomp$7$$.push(String.fromCharCode(($c$jscomp$17$$ & 31) << 6 | $c2$$ & 63)), $i$jscomp$45$$ += 2) : ($c2$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$45$$ + 1), $c3$jscomp$1$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$45$$ + 
    2), $string$jscomp$7$$.push(String.fromCharCode(($c$jscomp$17$$ & 15) << 12 | ($c2$$ & 63) << 6 | $c3$jscomp$1$$ & 63)), $i$jscomp$45$$ += 3);
  }
  return $string$jscomp$7$$.join("");
}, $_destrip$:function($stripped$$, $wrap$$) {
  var $lines$$ = [], $i$jscomp$46$$, $destripped$$ = [];
  null == $wrap$$ && ($wrap$$ = 76);
  $stripped$$.replace(/ /g, "");
  var $lineno$$ = $stripped$$.length / $wrap$$;
  for ($i$jscomp$46$$ = 0; $i$jscomp$46$$ < $lineno$$; $i$jscomp$46$$++) {
    $lines$$[$i$jscomp$46$$] = $stripped$$.substr($i$jscomp$46$$ * $wrap$$, $wrap$$);
  }
  $lineno$$ != $stripped$$.length / $wrap$$ && ($lines$$[$lines$$.length] = $stripped$$.substr($lineno$$ * $wrap$$, $stripped$$.length - $lineno$$ * $wrap$$));
  for ($i$jscomp$46$$ = 0; $i$jscomp$46$$ < $lines$$.length; $i$jscomp$46$$++) {
    $destripped$$.push($lines$$[$i$jscomp$46$$]);
  }
  return $destripped$$.join("\n");
}, $decodeAsArray$:function($dec_input$jscomp$11$$) {
  $dec_input$jscomp$11$$ = this.decode($dec_input$jscomp$11$$);
  var $ar$$ = [], $i$jscomp$47$$;
  for ($i$jscomp$47$$ = 0; $i$jscomp$47$$ < $dec_input$jscomp$11$$.length; $i$jscomp$47$$++) {
    $ar$$[$i$jscomp$47$$] = $dec_input$jscomp$11$$.charCodeAt($i$jscomp$47$$);
  }
  return $ar$$;
}, $decodeGEONExT$:function($input$jscomp$12$$) {
  return decodeAsArray(destrip($input$jscomp$12$$), !1);
}};
JXG.$Util$.$asciiCharCodeAt$ = function($c$jscomp$18_str$jscomp$6$$, $i$jscomp$48$$) {
  $c$jscomp$18_str$jscomp$6$$ = $c$jscomp$18_str$jscomp$6$$.charCodeAt($i$jscomp$48$$);
  if (255 < $c$jscomp$18_str$jscomp$6$$) {
    switch($c$jscomp$18_str$jscomp$6$$) {
      case 8364:
        $c$jscomp$18_str$jscomp$6$$ = 128;
        break;
      case 8218:
        $c$jscomp$18_str$jscomp$6$$ = 130;
        break;
      case 402:
        $c$jscomp$18_str$jscomp$6$$ = 131;
        break;
      case 8222:
        $c$jscomp$18_str$jscomp$6$$ = 132;
        break;
      case 8230:
        $c$jscomp$18_str$jscomp$6$$ = 133;
        break;
      case 8224:
        $c$jscomp$18_str$jscomp$6$$ = 134;
        break;
      case 8225:
        $c$jscomp$18_str$jscomp$6$$ = 135;
        break;
      case 710:
        $c$jscomp$18_str$jscomp$6$$ = 136;
        break;
      case 8240:
        $c$jscomp$18_str$jscomp$6$$ = 137;
        break;
      case 352:
        $c$jscomp$18_str$jscomp$6$$ = 138;
        break;
      case 8249:
        $c$jscomp$18_str$jscomp$6$$ = 139;
        break;
      case 338:
        $c$jscomp$18_str$jscomp$6$$ = 140;
        break;
      case 381:
        $c$jscomp$18_str$jscomp$6$$ = 142;
        break;
      case 8216:
        $c$jscomp$18_str$jscomp$6$$ = 145;
        break;
      case 8217:
        $c$jscomp$18_str$jscomp$6$$ = 146;
        break;
      case 8220:
        $c$jscomp$18_str$jscomp$6$$ = 147;
        break;
      case 8221:
        $c$jscomp$18_str$jscomp$6$$ = 148;
        break;
      case 8226:
        $c$jscomp$18_str$jscomp$6$$ = 149;
        break;
      case 8211:
        $c$jscomp$18_str$jscomp$6$$ = 150;
        break;
      case 8212:
        $c$jscomp$18_str$jscomp$6$$ = 151;
        break;
      case 732:
        $c$jscomp$18_str$jscomp$6$$ = 152;
        break;
      case 8482:
        $c$jscomp$18_str$jscomp$6$$ = 153;
        break;
      case 353:
        $c$jscomp$18_str$jscomp$6$$ = 154;
        break;
      case 8250:
        $c$jscomp$18_str$jscomp$6$$ = 155;
        break;
      case 339:
        $c$jscomp$18_str$jscomp$6$$ = 156;
        break;
      case 382:
        $c$jscomp$18_str$jscomp$6$$ = 158;
        break;
      case 376:
        $c$jscomp$18_str$jscomp$6$$ = 159;
    }
  }
  return $c$jscomp$18_str$jscomp$6$$;
};
JXG.$Util$.$utf8Decode$ = function($utftext$jscomp$2$$) {
  for (var $string$jscomp$8$$ = [], $i$jscomp$49$$ = 0, $c$jscomp$19$$, $c2$jscomp$1$$; $i$jscomp$49$$ < $utftext$jscomp$2$$.length;) {
    $c$jscomp$19$$ = $utftext$jscomp$2$$.charCodeAt($i$jscomp$49$$), 128 > $c$jscomp$19$$ ? ($string$jscomp$8$$.push(String.fromCharCode($c$jscomp$19$$)), $i$jscomp$49$$++) : 191 < $c$jscomp$19$$ && 224 > $c$jscomp$19$$ ? ($c2$jscomp$1$$ = $utftext$jscomp$2$$.charCodeAt($i$jscomp$49$$ + 1), $string$jscomp$8$$.push(String.fromCharCode(($c$jscomp$19$$ & 31) << 6 | $c2$jscomp$1$$ & 63)), $i$jscomp$49$$ += 2) : ($c2$jscomp$1$$ = $utftext$jscomp$2$$.charCodeAt($i$jscomp$49$$ + 1), c3 = $utftext$jscomp$2$$.charCodeAt($i$jscomp$49$$ + 
    2), $string$jscomp$8$$.push(String.fromCharCode(($c$jscomp$19$$ & 15) << 12 | ($c2$jscomp$1$$ & 63) << 6 | c3 & 63)), $i$jscomp$49$$ += 3);
  }
  return $string$jscomp$8$$.join("");
};
(function($L$jscomp$1$$) {
  function $g$jscomp$8$$($d$jscomp$16$$, $b$jscomp$19$$) {
    this.a = $d$jscomp$16$$;
    this.b = $b$jscomp$19$$;
  }
  function $D$jscomp$1$$($d$jscomp$22$$, $b$jscomp$25$$, $h$jscomp$23$$) {
    switch($b$jscomp$25$$) {
      case "UTF8":
      case "UTF16BE":
      case "UTF16LE":
        break;
      default:
        throw Error("encoding must be UTF8, UTF16BE, or UTF16LE");
    }
    switch($d$jscomp$22$$) {
      case "HEX":
        $d$jscomp$22$$ = function($c$jscomp$27$$, $a$jscomp$32$$, $b$jscomp$26$$) {
          var $e$jscomp$32$$ = $c$jscomp$27$$.length, $d$jscomp$23$$, $f$jscomp$16$$;
          if (0 !== $e$jscomp$32$$ % 2) {
            throw Error("String of HEX type must be in byte increments");
          }
          $a$jscomp$32$$ = $a$jscomp$32$$ || [0];
          $b$jscomp$26$$ = $b$jscomp$26$$ || 0;
          var $r$jscomp$5$$ = $b$jscomp$26$$ >>> 3;
          var $q$jscomp$3$$ = -1 === $h$jscomp$23$$ ? 3 : 0;
          for ($d$jscomp$23$$ = 0; $d$jscomp$23$$ < $e$jscomp$32$$; $d$jscomp$23$$ += 2) {
            var $k$jscomp$8$$ = parseInt($c$jscomp$27$$.substr($d$jscomp$23$$, 2), 16);
            if (isNaN($k$jscomp$8$$)) {
              throw Error("String of HEX type contains invalid characters");
            }
            var $g$jscomp$12$$ = ($d$jscomp$23$$ >>> 1) + $r$jscomp$5$$;
            for ($f$jscomp$16$$ = $g$jscomp$12$$ >>> 2; $a$jscomp$32$$.length <= $f$jscomp$16$$;) {
              $a$jscomp$32$$.push(0);
            }
            $a$jscomp$32$$[$f$jscomp$16$$] |= $k$jscomp$8$$ << 8 * ($q$jscomp$3$$ + $g$jscomp$12$$ % 4 * $h$jscomp$23$$);
          }
          return {value:$a$jscomp$32$$, $binLen$:4 * $e$jscomp$32$$ + $b$jscomp$26$$};
        };
        break;
      case "TEXT":
        $d$jscomp$22$$ = function($c$jscomp$28$$, $a$jscomp$33$$, $d$jscomp$24$$) {
          var $k$jscomp$9$$ = 0, $f$jscomp$17$$, $g$jscomp$13$$, $q$jscomp$4$$;
          $a$jscomp$33$$ = $a$jscomp$33$$ || [0];
          $d$jscomp$24$$ = $d$jscomp$24$$ || 0;
          var $r$jscomp$6$$ = $d$jscomp$24$$ >>> 3;
          if ("UTF8" === $b$jscomp$25$$) {
            var $p$jscomp$6$$ = -1 === $h$jscomp$23$$ ? 3 : 0;
            for ($f$jscomp$17$$ = 0; $f$jscomp$17$$ < $c$jscomp$28$$.length; $f$jscomp$17$$ += 1) {
              var $e$jscomp$33$$ = $c$jscomp$28$$.charCodeAt($f$jscomp$17$$);
              var $m$jscomp$7$$ = [];
              128 > $e$jscomp$33$$ ? $m$jscomp$7$$.push($e$jscomp$33$$) : 2048 > $e$jscomp$33$$ ? ($m$jscomp$7$$.push(192 | $e$jscomp$33$$ >>> 6), $m$jscomp$7$$.push(128 | $e$jscomp$33$$ & 63)) : 55296 > $e$jscomp$33$$ || 57344 <= $e$jscomp$33$$ ? $m$jscomp$7$$.push(224 | $e$jscomp$33$$ >>> 12, 128 | $e$jscomp$33$$ >>> 6 & 63, 128 | $e$jscomp$33$$ & 63) : ($f$jscomp$17$$ += 1, $e$jscomp$33$$ = 65536 + (($e$jscomp$33$$ & 1023) << 10 | $c$jscomp$28$$.charCodeAt($f$jscomp$17$$) & 1023), $m$jscomp$7$$.push(240 | 
              $e$jscomp$33$$ >>> 18, 128 | $e$jscomp$33$$ >>> 12 & 63, 128 | $e$jscomp$33$$ >>> 6 & 63, 128 | $e$jscomp$33$$ & 63));
              for ($g$jscomp$13$$ = 0; $g$jscomp$13$$ < $m$jscomp$7$$.length; $g$jscomp$13$$ += 1) {
                var $v$jscomp$3$$ = $k$jscomp$9$$ + $r$jscomp$6$$;
                for ($q$jscomp$4$$ = $v$jscomp$3$$ >>> 2; $a$jscomp$33$$.length <= $q$jscomp$4$$;) {
                  $a$jscomp$33$$.push(0);
                }
                $a$jscomp$33$$[$q$jscomp$4$$] |= $m$jscomp$7$$[$g$jscomp$13$$] << 8 * ($p$jscomp$6$$ + $v$jscomp$3$$ % 4 * $h$jscomp$23$$);
                $k$jscomp$9$$ += 1;
              }
            }
          } else {
            if ("UTF16BE" === $b$jscomp$25$$ || "UTF16LE" === $b$jscomp$25$$) {
              for ($p$jscomp$6$$ = -1 === $h$jscomp$23$$ ? 2 : 0, $f$jscomp$17$$ = 0; $f$jscomp$17$$ < $c$jscomp$28$$.length; $f$jscomp$17$$ += 1) {
                $e$jscomp$33$$ = $c$jscomp$28$$.charCodeAt($f$jscomp$17$$);
                "UTF16LE" === $b$jscomp$25$$ && ($g$jscomp$13$$ = $e$jscomp$33$$ & 255, $e$jscomp$33$$ = $g$jscomp$13$$ << 8 | $e$jscomp$33$$ >>> 8);
                $v$jscomp$3$$ = $k$jscomp$9$$ + $r$jscomp$6$$;
                for ($q$jscomp$4$$ = $v$jscomp$3$$ >>> 2; $a$jscomp$33$$.length <= $q$jscomp$4$$;) {
                  $a$jscomp$33$$.push(0);
                }
                $a$jscomp$33$$[$q$jscomp$4$$] |= $e$jscomp$33$$ << 8 * ($p$jscomp$6$$ + $v$jscomp$3$$ % 4 * $h$jscomp$23$$);
                $k$jscomp$9$$ += 2;
              }
            }
          }
          return {value:$a$jscomp$33$$, $binLen$:8 * $k$jscomp$9$$ + $d$jscomp$24$$};
        };
        break;
      case "B64":
        $d$jscomp$22$$ = function($c$jscomp$29$$, $a$jscomp$34$$, $b$jscomp$27$$) {
          var $e$jscomp$34$$ = 0, $f$jscomp$18$$, $n$jscomp$9$$;
          if (-1 === $c$jscomp$29$$.search(/^[a-zA-Z0-9=+\/]+$/)) {
            throw Error("Invalid character in base-64 string");
          }
          var $g$jscomp$14$$ = $c$jscomp$29$$.indexOf("=");
          $c$jscomp$29$$ = $c$jscomp$29$$.replace(/=/g, "");
          if (-1 !== $g$jscomp$14$$ && $g$jscomp$14$$ < $c$jscomp$29$$.length) {
            throw Error("Invalid '=' found in base-64 string");
          }
          $a$jscomp$34$$ = $a$jscomp$34$$ || [0];
          $b$jscomp$27$$ = $b$jscomp$27$$ || 0;
          var $q$jscomp$5$$ = $b$jscomp$27$$ >>> 3;
          var $u$jscomp$3$$ = -1 === $h$jscomp$23$$ ? 3 : 0;
          for ($g$jscomp$14$$ = 0; $g$jscomp$14$$ < $c$jscomp$29$$.length; $g$jscomp$14$$ += 4) {
            var $r$jscomp$7$$ = $c$jscomp$29$$.substr($g$jscomp$14$$, 4);
            for ($f$jscomp$18$$ = $n$jscomp$9$$ = 0; $f$jscomp$18$$ < $r$jscomp$7$$.length; $f$jscomp$18$$ += 1) {
              var $d$jscomp$25$$ = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf($r$jscomp$7$$[$f$jscomp$18$$]);
              $n$jscomp$9$$ |= $d$jscomp$25$$ << 18 - 6 * $f$jscomp$18$$;
            }
            for ($f$jscomp$18$$ = 0; $f$jscomp$18$$ < $r$jscomp$7$$.length - 1; $f$jscomp$18$$ += 1) {
              var $p$jscomp$7$$ = $e$jscomp$34$$ + $q$jscomp$5$$;
              for ($d$jscomp$25$$ = $p$jscomp$7$$ >>> 2; $a$jscomp$34$$.length <= $d$jscomp$25$$;) {
                $a$jscomp$34$$.push(0);
              }
              $a$jscomp$34$$[$d$jscomp$25$$] |= ($n$jscomp$9$$ >>> 16 - 8 * $f$jscomp$18$$ & 255) << 8 * ($u$jscomp$3$$ + $p$jscomp$7$$ % 4 * $h$jscomp$23$$);
              $e$jscomp$34$$ += 1;
            }
          }
          return {value:$a$jscomp$34$$, $binLen$:8 * $e$jscomp$34$$ + $b$jscomp$27$$};
        };
        break;
      case "BYTES":
        $d$jscomp$22$$ = function($c$jscomp$30$$, $a$jscomp$35$$, $b$jscomp$28$$) {
          var $d$jscomp$26$$;
          $a$jscomp$35$$ = $a$jscomp$35$$ || [0];
          $b$jscomp$28$$ = $b$jscomp$28$$ || 0;
          var $g$jscomp$15$$ = $b$jscomp$28$$ >>> 3;
          var $p$jscomp$8$$ = -1 === $h$jscomp$23$$ ? 3 : 0;
          for ($d$jscomp$26$$ = 0; $d$jscomp$26$$ < $c$jscomp$30$$.length; $d$jscomp$26$$ += 1) {
            var $e$jscomp$35$$ = $c$jscomp$30$$.charCodeAt($d$jscomp$26$$);
            var $n$jscomp$10$$ = $d$jscomp$26$$ + $g$jscomp$15$$;
            var $f$jscomp$19$$ = $n$jscomp$10$$ >>> 2;
            $a$jscomp$35$$.length <= $f$jscomp$19$$ && $a$jscomp$35$$.push(0);
            $a$jscomp$35$$[$f$jscomp$19$$] |= $e$jscomp$35$$ << 8 * ($p$jscomp$8$$ + $n$jscomp$10$$ % 4 * $h$jscomp$23$$);
          }
          return {value:$a$jscomp$35$$, $binLen$:8 * $c$jscomp$30$$.length + $b$jscomp$28$$};
        };
        break;
      case "ARRAYBUFFER":
        try {
          $d$jscomp$22$$ = new ArrayBuffer(0);
        } catch ($c$jscomp$31$$) {
          throw Error("ARRAYBUFFER not supported by this environment");
        }
        $d$jscomp$22$$ = function($c$jscomp$32$$, $a$jscomp$36$$, $b$jscomp$29$$) {
          var $d$jscomp$27$$;
          $a$jscomp$36$$ = $a$jscomp$36$$ || [0];
          $b$jscomp$29$$ = $b$jscomp$29$$ || 0;
          var $g$jscomp$16$$ = $b$jscomp$29$$ >>> 3;
          var $n$jscomp$11$$ = -1 === $h$jscomp$23$$ ? 3 : 0;
          var $p$jscomp$9$$ = new Uint8Array($c$jscomp$32$$);
          for ($d$jscomp$27$$ = 0; $d$jscomp$27$$ < $c$jscomp$32$$.byteLength; $d$jscomp$27$$ += 1) {
            var $f$jscomp$20$$ = $d$jscomp$27$$ + $g$jscomp$16$$;
            var $k$jscomp$10$$ = $f$jscomp$20$$ >>> 2;
            $a$jscomp$36$$.length <= $k$jscomp$10$$ && $a$jscomp$36$$.push(0);
            $a$jscomp$36$$[$k$jscomp$10$$] |= $p$jscomp$9$$[$d$jscomp$27$$] << 8 * ($n$jscomp$11$$ + $f$jscomp$20$$ % 4 * $h$jscomp$23$$);
          }
          return {value:$a$jscomp$36$$, $binLen$:8 * $c$jscomp$32$$.byteLength + $b$jscomp$29$$};
        };
        break;
      default:
        throw Error("format must be HEX, TEXT, B64, BYTES, or ARRAYBUFFER");
    }
    return $d$jscomp$22$$;
  }
  function $z$jscomp$19$$($d$jscomp$28$$, $b$jscomp$30$$) {
    return 32 < $b$jscomp$30$$ ? ($b$jscomp$30$$ -= 32, new $g$jscomp$8$$($d$jscomp$28$$.b << $b$jscomp$30$$ | $d$jscomp$28$$.a >>> 32 - $b$jscomp$30$$, $d$jscomp$28$$.a << $b$jscomp$30$$ | $d$jscomp$28$$.b >>> 32 - $b$jscomp$30$$)) : 0 !== $b$jscomp$30$$ ? new $g$jscomp$8$$($d$jscomp$28$$.a << $b$jscomp$30$$ | $d$jscomp$28$$.b >>> 32 - $b$jscomp$30$$, $d$jscomp$28$$.b << $b$jscomp$30$$ | $d$jscomp$28$$.a >>> 32 - $b$jscomp$30$$) : $d$jscomp$28$$;
  }
  function $p$jscomp$3$$($d$jscomp$29$$, $b$jscomp$31$$) {
    return new $g$jscomp$8$$($d$jscomp$29$$.a ^ $b$jscomp$31$$.a, $d$jscomp$29$$.b ^ $b$jscomp$31$$.b);
  }
  function $y$jscomp$77$$($d$jscomp$30$$) {
    var $b$jscomp$32$$ = [];
    if (0 === $d$jscomp$30$$.lastIndexOf("SHA3-", 0) || 0 === $d$jscomp$30$$.lastIndexOf("SHAKE", 0)) {
      for ($d$jscomp$30$$ = 0; 5 > $d$jscomp$30$$; $d$jscomp$30$$ += 1) {
        $b$jscomp$32$$[$d$jscomp$30$$] = [new $g$jscomp$8$$(0, 0), new $g$jscomp$8$$(0, 0), new $g$jscomp$8$$(0, 0), new $g$jscomp$8$$(0, 0), new $g$jscomp$8$$(0, 0)];
      }
    } else {
      throw Error("No SHA variants supported");
    }
    return $b$jscomp$32$$;
  }
  function $B$jscomp$1$$($d$jscomp$31_h$jscomp$24$$, $b$jscomp$33$$) {
    var $c$jscomp$33$$, $e$jscomp$36$$ = [], $m$jscomp$8$$ = [];
    if (null !== $d$jscomp$31_h$jscomp$24$$) {
      for ($c$jscomp$33$$ = 0; $c$jscomp$33$$ < $d$jscomp$31_h$jscomp$24$$.length; $c$jscomp$33$$ += 2) {
        $b$jscomp$33$$[($c$jscomp$33$$ >>> 1) % 5][($c$jscomp$33$$ >>> 1) / 5 | 0] = $p$jscomp$3$$($b$jscomp$33$$[($c$jscomp$33$$ >>> 1) % 5][($c$jscomp$33$$ >>> 1) / 5 | 0], new $g$jscomp$8$$($d$jscomp$31_h$jscomp$24$$[$c$jscomp$33$$ + 1], $d$jscomp$31_h$jscomp$24$$[$c$jscomp$33$$]));
      }
    }
    for ($d$jscomp$31_h$jscomp$24$$ = 0; 24 > $d$jscomp$31_h$jscomp$24$$; $d$jscomp$31_h$jscomp$24$$ += 1) {
      var $l$jscomp$14$$ = $y$jscomp$77$$("SHA3-");
      for ($c$jscomp$33$$ = 0; 5 > $c$jscomp$33$$; $c$jscomp$33$$ += 1) {
        var $a$jscomp$37$$ = $b$jscomp$33$$[$c$jscomp$33$$][0];
        var $k$jscomp$11$$ = $b$jscomp$33$$[$c$jscomp$33$$][1], $f$jscomp$21$$ = $b$jscomp$33$$[$c$jscomp$33$$][2], $n$jscomp$12$$ = $b$jscomp$33$$[$c$jscomp$33$$][3], $r$jscomp$8$$ = $b$jscomp$33$$[$c$jscomp$33$$][4];
        $e$jscomp$36$$[$c$jscomp$33$$] = new $g$jscomp$8$$($a$jscomp$37$$.a ^ $k$jscomp$11$$.a ^ $f$jscomp$21$$.a ^ $n$jscomp$12$$.a ^ $r$jscomp$8$$.a, $a$jscomp$37$$.b ^ $k$jscomp$11$$.b ^ $f$jscomp$21$$.b ^ $n$jscomp$12$$.b ^ $r$jscomp$8$$.b);
      }
      for ($c$jscomp$33$$ = 0; 5 > $c$jscomp$33$$; $c$jscomp$33$$ += 1) {
        $m$jscomp$8$$[$c$jscomp$33$$] = $p$jscomp$3$$($e$jscomp$36$$[($c$jscomp$33$$ + 4) % 5], $z$jscomp$19$$($e$jscomp$36$$[($c$jscomp$33$$ + 1) % 5], 1));
      }
      for ($c$jscomp$33$$ = 0; 5 > $c$jscomp$33$$; $c$jscomp$33$$ += 1) {
        for ($a$jscomp$37$$ = 0; 5 > $a$jscomp$37$$; $a$jscomp$37$$ += 1) {
          $b$jscomp$33$$[$c$jscomp$33$$][$a$jscomp$37$$] = $p$jscomp$3$$($b$jscomp$33$$[$c$jscomp$33$$][$a$jscomp$37$$], $m$jscomp$8$$[$c$jscomp$33$$]);
        }
      }
      for ($c$jscomp$33$$ = 0; 5 > $c$jscomp$33$$; $c$jscomp$33$$ += 1) {
        for ($a$jscomp$37$$ = 0; 5 > $a$jscomp$37$$; $a$jscomp$37$$ += 1) {
          $l$jscomp$14$$[$a$jscomp$37$$][(2 * $c$jscomp$33$$ + 3 * $a$jscomp$37$$) % 5] = $z$jscomp$19$$($b$jscomp$33$$[$c$jscomp$33$$][$a$jscomp$37$$], $J$jscomp$1$$[$c$jscomp$33$$][$a$jscomp$37$$]);
        }
      }
      for ($c$jscomp$33$$ = 0; 5 > $c$jscomp$33$$; $c$jscomp$33$$ += 1) {
        for ($a$jscomp$37$$ = 0; 5 > $a$jscomp$37$$; $a$jscomp$37$$ += 1) {
          $b$jscomp$33$$[$c$jscomp$33$$][$a$jscomp$37$$] = $p$jscomp$3$$($l$jscomp$14$$[$c$jscomp$33$$][$a$jscomp$37$$], new $g$jscomp$8$$(~$l$jscomp$14$$[($c$jscomp$33$$ + 1) % 5][$a$jscomp$37$$].a & $l$jscomp$14$$[($c$jscomp$33$$ + 2) % 5][$a$jscomp$37$$].a, ~$l$jscomp$14$$[($c$jscomp$33$$ + 1) % 5][$a$jscomp$37$$].b & $l$jscomp$14$$[($c$jscomp$33$$ + 2) % 5][$a$jscomp$37$$].b));
        }
      }
      $b$jscomp$33$$[0][0] = $p$jscomp$3$$($b$jscomp$33$$[0][0], $K$jscomp$1$$[$d$jscomp$31_h$jscomp$24$$]);
    }
    return $b$jscomp$33$$;
  }
  var $K$jscomp$1$$ = [new $g$jscomp$8$$(0, 1), new $g$jscomp$8$$(0, 32898), new $g$jscomp$8$$(2147483648, 32906), new $g$jscomp$8$$(2147483648, 2147516416), new $g$jscomp$8$$(0, 32907), new $g$jscomp$8$$(0, 2147483649), new $g$jscomp$8$$(2147483648, 2147516545), new $g$jscomp$8$$(2147483648, 32777), new $g$jscomp$8$$(0, 138), new $g$jscomp$8$$(0, 136), new $g$jscomp$8$$(0, 2147516425), new $g$jscomp$8$$(0, 2147483658), new $g$jscomp$8$$(0, 2147516555), new $g$jscomp$8$$(2147483648, 139), new $g$jscomp$8$$(2147483648, 
  32905), new $g$jscomp$8$$(2147483648, 32771), new $g$jscomp$8$$(2147483648, 32770), new $g$jscomp$8$$(2147483648, 128), new $g$jscomp$8$$(0, 32778), new $g$jscomp$8$$(2147483648, 2147483658), new $g$jscomp$8$$(2147483648, 2147516545), new $g$jscomp$8$$(2147483648, 32896), new $g$jscomp$8$$(0, 2147483649), new $g$jscomp$8$$(2147483648, 2147516424)];
  var $J$jscomp$1$$ = [[0, 36, 3, 41, 18], [1, 44, 10, 45, 2], [62, 6, 43, 15, 61], [28, 55, 25, 21, 56], [27, 20, 39, 8, 14]];
  $L$jscomp$1$$.$B$ = function($d$jscomp$13$$, $b$jscomp$13$$, $h$jscomp$14_w$jscomp$12$$) {
    var $a$jscomp$16$$ = [], $l$jscomp$7$$ = 0;
    $h$jscomp$14_w$jscomp$12$$ = $h$jscomp$14_w$jscomp$12$$ || {};
    var $e$jscomp$22$$ = $h$jscomp$14_w$jscomp$12$$.encoding || "UTF8";
    $h$jscomp$14_w$jscomp$12$$ = $h$jscomp$14_w$jscomp$12$$.$numRounds$ || 1;
    if ($h$jscomp$14_w$jscomp$12$$ !== parseInt($h$jscomp$14_w$jscomp$12$$, 10) || 1 > $h$jscomp$14_w$jscomp$12$$) {
      throw Error("numRounds must a integer >= 1");
    }
    if (0 === $d$jscomp$13$$.lastIndexOf("SHA3-", 0) || 0 === $d$jscomp$13$$.lastIndexOf("SHAKE", 0)) {
      var $g$jscomp$9$$ = $B$jscomp$1$$;
      if ("SHA3-224" === $d$jscomp$13$$) {
        var $n$jscomp$7$$ = 1152;
      } else {
        if ("SHA3-256" === $d$jscomp$13$$) {
          $n$jscomp$7$$ = 1088;
        } else {
          if ("SHA3-384" === $d$jscomp$13$$) {
            $n$jscomp$7$$ = 832;
          } else {
            if ("SHA3-512" === $d$jscomp$13$$) {
              $n$jscomp$7$$ = 576;
            } else {
              if ("SHAKE128" === $d$jscomp$13$$) {
                $n$jscomp$7$$ = 1344;
              } else {
                if ("SHAKE256" === $d$jscomp$13$$) {
                  $n$jscomp$7$$ = 1088;
                } else {
                  throw Error("Chosen SHA variant is not supported");
                }
              }
            }
          }
        }
      }
    } else {
      throw Error("Chosen SHA variant is not supported");
    }
    var $k$jscomp$2$$ = $D$jscomp$1$$($b$jscomp$13$$, $e$jscomp$22$$, 1);
    var $m$jscomp$3$$ = $y$jscomp$77$$($d$jscomp$13$$);
    this.update = function($e$jscomp$25$$) {
      var $d$jscomp$15$$, $h$jscomp$16$$ = 0, $q$jscomp$2$$ = $n$jscomp$7$$ >>> 5;
      var $b$jscomp$16$$ = $k$jscomp$2$$($e$jscomp$25$$, $a$jscomp$16$$, $l$jscomp$7$$);
      $e$jscomp$25$$ = $b$jscomp$16$$.$binLen$;
      var $f$jscomp$14$$ = $b$jscomp$16$$.value;
      $b$jscomp$16$$ = $e$jscomp$25$$ >>> 5;
      for ($d$jscomp$15$$ = 0; $d$jscomp$15$$ < $b$jscomp$16$$; $d$jscomp$15$$ += $q$jscomp$2$$) {
        $h$jscomp$16$$ + $n$jscomp$7$$ <= $e$jscomp$25$$ && ($m$jscomp$3$$ = $g$jscomp$9$$($f$jscomp$14$$.slice($d$jscomp$15$$, $d$jscomp$15$$ + $q$jscomp$2$$), $m$jscomp$3$$), $h$jscomp$16$$ += $n$jscomp$7$$);
      }
      $a$jscomp$16$$ = $f$jscomp$14$$.slice($h$jscomp$16$$ >>> 5);
      $l$jscomp$7$$ = $e$jscomp$25$$ % $n$jscomp$7$$;
    };
  };
})(this);
var $module$build$ts$PerceptionListener$default$$ = {};
Object.defineProperty($module$build$ts$PerceptionListener$default$$, "__esModule", {value:!0});
$module$build$ts$PerceptionListener$default$$.$PerceptionListener$ = void 0;
$module$build$ts$PerceptionListener$default$$.$PerceptionListener$ = function($perceptionListener$$) {
  this.$v$ = $perceptionListener$$;
  for (var $$jscomp$iter$5$$ = $$jscomp$makeIterator$$(Object.getOwnPropertyNames($perceptionListener$$)), $$jscomp$key$prop_prop$jscomp$4$$ = $$jscomp$iter$5$$.next(); !$$jscomp$key$prop_prop$jscomp$4$$.done; $$jscomp$key$prop_prop$jscomp$4$$ = $$jscomp$iter$5$$.next()) {
    $$jscomp$key$prop_prop$jscomp$4$$ = $$jscomp$key$prop_prop$jscomp$4$$.value, "undefined" === typeof this[$$jscomp$key$prop_prop$jscomp$4$$] && (this[$$jscomp$key$prop_prop$jscomp$4$$] = $perceptionListener$$[$$jscomp$key$prop_prop$jscomp$4$$]);
  }
};
$module$build$ts$PerceptionListener$default$$.$PerceptionListener$.prototype.$onAdded$ = function($obj$jscomp$38$$) {
  "player" === $obj$jscomp$38$$.c && $obj$jscomp$38$$.hasOwnProperty("a") && $obj$jscomp$38$$.a.name === ($stendhal$$.$session$.$charname$ || "") && ($obj$jscomp$38$$.c = "user");
  return this.$v$.$onAdded$($obj$jscomp$38$$);
};
var $module$build$ts$ui$toolkit$Component$default$$ = {};
Object.defineProperty($module$build$ts$ui$toolkit$Component$default$$, "__esModule", {value:!0});
$module$build$ts$ui$toolkit$Component$default$$.$Component$ = void 0;
$module$build$ts$ui$toolkit$Component$default$$.$Component$ = function($id$jscomp$11$$, $themable$$) {
  var $element$jscomp$9$$ = document.getElementById($id$jscomp$11$$);
  if (!$element$jscomp$9$$) {
    throw Error("Cannot create component because there is no HTML element with id " + $id$jscomp$11$$);
  }
  $element$jscomp$9$$ instanceof HTMLTemplateElement && ($element$jscomp$9$$ = $element$jscomp$9$$.content.cloneNode(!0).children[0]);
  this.$componentElement$ = $element$jscomp$9$$;
  (void 0 === $themable$$ ? 0 : $themable$$) && this.$componentElement$.classList.add("background");
  this.$F$ = $element$jscomp$9$$.style.display;
  "none" === this.$F$ && (this.$F$ = "");
};
$module$build$ts$ui$toolkit$Component$default$$.$Component$.prototype.$onParentClose$ = function() {
};
$module$build$ts$ui$toolkit$Component$default$$.$Component$.prototype.$onMoved$ = function() {
};
$module$build$ts$ui$toolkit$Component$default$$.$Component$.prototype.refresh = function() {
};
$module$build$ts$ui$toolkit$Component$default$$.$Component$.prototype.isVisible = function() {
  return "none" !== this.$componentElement$.style.display;
};
function $JSCompiler_StaticMethods_setVisible$$($JSCompiler_StaticMethods_setVisible$self$$, $visible$$) {
  void 0 === $visible$$ || $visible$$ ? $JSCompiler_StaticMethods_setVisible$self$$.$componentElement$.style.display = $JSCompiler_StaticMethods_setVisible$self$$.$F$ : $JSCompiler_StaticMethods_setVisible$self$$.$componentElement$.style.display = "none";
}
function $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_child$self$$, $selector$jscomp$1$$) {
  return $JSCompiler_StaticMethods_child$self$$.$componentElement$.querySelector($selector$jscomp$1$$);
}
;var $module$build$ts$ui$SoundManager$default$$ = {};
Object.defineProperty($module$build$ts$ui$SoundManager$default$$, "__esModule", {value:!0});
$module$build$ts$ui$SoundManager$default$$.$SoundManager$ = void 0;
$module$build$ts$ui$SoundManager$default$$.$SoundManager$ = function() {
  this.$layers$ = ["music", "ambient", "creature", "sfx", "gui"];
  this.$v$ = {};
  this.cache = {};
  var $$jscomp$compprop0$$ = {};
  this.active = ($$jscomp$compprop0$$.music = [], $$jscomp$compprop0$$.ambient = [], $$jscomp$compprop0$$.creature = [], $$jscomp$compprop0$$.sfx = [], $$jscomp$compprop0$$.gui = [], $$jscomp$compprop0$$);
};
$module$build$ts$ui$SoundManager$default$$.$SoundManager$.get = function() {
  $module$build$ts$ui$SoundManager$default$$.$SoundManager$.$v$ || ($module$build$ts$ui$SoundManager$default$$.$SoundManager$.$v$ = new $module$build$ts$ui$SoundManager$default$$.$SoundManager$());
  return $module$build$ts$ui$SoundManager$default$$.$SoundManager$.$v$;
};
function $JSCompiler_StaticMethods_getActive$$($JSCompiler_StaticMethods_getActive$self$$) {
  var $includeGui$$ = void 0 === $includeGui$$ ? !1 : $includeGui$$;
  for (var $active$$ = [], $$jscomp$iter$7$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getActive$self$$.$layers$), $$jscomp$iter$6_$jscomp$key$layerName_layerName$$ = $$jscomp$iter$7$$.next(); !$$jscomp$iter$6_$jscomp$key$layerName_layerName$$.done; $$jscomp$iter$6_$jscomp$key$layerName_layerName$$ = $$jscomp$iter$7$$.next()) {
    if ($$jscomp$iter$6_$jscomp$key$layerName_layerName$$ = $$jscomp$iter$6_$jscomp$key$layerName_layerName$$.value, "gui" !== $$jscomp$iter$6_$jscomp$key$layerName_layerName$$ || $includeGui$$) {
      $$jscomp$iter$6_$jscomp$key$layerName_layerName$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getActive$self$$.active[$$jscomp$iter$6_$jscomp$key$layerName_layerName$$]);
      for (var $$jscomp$key$snd$$ = $$jscomp$iter$6_$jscomp$key$layerName_layerName$$.next(); !$$jscomp$key$snd$$.done; $$jscomp$key$snd$$ = $$jscomp$iter$6_$jscomp$key$layerName_layerName$$.next()) {
        $active$$.push($$jscomp$key$snd$$.value);
      }
    }
  }
  return $active$$;
}
$module$build$ts$ui$SoundManager$default$$.$SoundManager$.prototype.load = function($id$jscomp$12$$, $filename$jscomp$2_snd$jscomp$1$$, $global$jscomp$1$$) {
  $global$jscomp$1$$ = void 0 === $global$jscomp$1$$ ? !1 : $global$jscomp$1$$;
  $filename$jscomp$2_snd$jscomp$1$$ = new Audio($filename$jscomp$2_snd$jscomp$1$$);
  $filename$jscomp$2_snd$jscomp$1$$.autoplay = !1;
  $global$jscomp$1$$ ? this.$v$[$id$jscomp$12$$] = $filename$jscomp$2_snd$jscomp$1$$ : this.cache[$id$jscomp$12$$] = $filename$jscomp$2_snd$jscomp$1$$;
  return $filename$jscomp$2_snd$jscomp$1$$;
};
function $JSCompiler_StaticMethods_getLayerName$$($JSCompiler_StaticMethods_getLayerName$self$$, $layer$jscomp$2$$) {
  var $layername$$ = "gui";
  0 <= $layer$jscomp$2$$ && $layer$jscomp$2$$ < $JSCompiler_StaticMethods_getLayerName$self$$.$layers$.length ? $layername$$ = $JSCompiler_StaticMethods_getLayerName$self$$.$layers$[$layer$jscomp$2$$] : console.warn("unknown layer index: " + $layer$jscomp$2$$);
  return $layername$$;
}
function $JSCompiler_StaticMethods_onSoundAdded$$($JSCompiler_StaticMethods_onSoundAdded$self$$, $layername$jscomp$1$$, $sound$$) {
  $sound$$.onended = function() {
    var $idx$jscomp$3$$ = $JSCompiler_StaticMethods_onSoundAdded$self$$.active[$layername$jscomp$1$$].indexOf($sound$$);
    -1 < $idx$jscomp$3$$ && $JSCompiler_StaticMethods_onSoundAdded$self$$.active[$layername$jscomp$1$$].splice($idx$jscomp$3$$, 1);
  };
  $JSCompiler_StaticMethods_onSoundAdded$self$$.active[$layername$jscomp$1$$].push($sound$$);
}
function $JSCompiler_StaticMethods_playEffect$$($JSCompiler_StaticMethods_playEffect$self$$, $scopy_soundname$$, $layername$jscomp$2$$, $volume$$, $loop$$) {
  $volume$$ = void 0 === $volume$$ ? 1.0 : $volume$$;
  $loop$$ = void 0 === $loop$$ ? !1 : $loop$$;
  var $muted$$ = !$JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "ui.sound");
  if (!$muted$$ || $loop$$) {
    $volume$$ = $JSCompiler_StaticMethods_normVolume$$($volume$$);
    var $actualvolume$$ = $JSCompiler_StaticMethods_getAdjustedVolume$$($layername$jscomp$2$$, $volume$$), $snd$jscomp$2$$ = $JSCompiler_StaticMethods_playEffect$self$$.cache[$scopy_soundname$$] || $JSCompiler_StaticMethods_playEffect$self$$.$v$[$scopy_soundname$$];
    $snd$jscomp$2$$ || ($snd$jscomp$2$$ = $JSCompiler_StaticMethods_playEffect$self$$.load($scopy_soundname$$, $stendhal$$.$paths$.$sounds$ + "/" + $scopy_soundname$$ + ".ogg"));
    $JSCompiler_StaticMethods_playEffect$self$$.cache[$scopy_soundname$$] || ($JSCompiler_StaticMethods_playEffect$self$$.cache[$scopy_soundname$$] = $snd$jscomp$2$$);
    "gui" !== $layername$jscomp$2$$ || $JSCompiler_StaticMethods_playEffect$self$$.$v$[$scopy_soundname$$] || ($JSCompiler_StaticMethods_playEffect$self$$.$v$[$scopy_soundname$$] = $snd$jscomp$2$$);
    $scopy_soundname$$ = $snd$jscomp$2$$.cloneNode();
    $scopy_soundname$$.autoplay = !0;
    $scopy_soundname$$.$basevolume$ = $volume$$;
    $scopy_soundname$$.volume = Math.min($actualvolume$$, $volume$$);
    $scopy_soundname$$.loop = $loop$$;
    $scopy_soundname$$.muted = $muted$$;
    $JSCompiler_StaticMethods_onSoundAdded$$($JSCompiler_StaticMethods_playEffect$self$$, $layername$jscomp$2$$, $scopy_soundname$$);
    return $scopy_soundname$$;
  }
}
function $JSCompiler_StaticMethods_playLocalizedEffect$$($JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$, $x$jscomp$98$$, $y$jscomp$78$$, $radius$jscomp$6$$, $layer$jscomp$3_layername$jscomp$3$$, $soundname$jscomp$1$$, $volume$jscomp$1$$, $loop$jscomp$1$$) {
  $volume$jscomp$1$$ = void 0 === $volume$jscomp$1$$ ? 1.0 : $volume$jscomp$1$$;
  $loop$jscomp$1$$ = void 0 === $loop$jscomp$1$$ ? !1 : $loop$jscomp$1$$;
  $layer$jscomp$3_layername$jscomp$3$$ = $JSCompiler_StaticMethods_getLayerName$$($JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$, $layer$jscomp$3_layername$jscomp$3$$);
  if ($JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$ = $JSCompiler_StaticMethods_playEffect$$($JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$, $soundname$jscomp$1$$, $layer$jscomp$3_layername$jscomp$3$$, $volume$jscomp$1$$, $loop$jscomp$1$$)) {
    return $radius$jscomp$6$$ && ($marauroa$$.$me$ && $x$jscomp$98$$ ? $JSCompiler_StaticMethods_adjustForDistance$$($layer$jscomp$3_layername$jscomp$3$$, $JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$, $radius$jscomp$6$$, $x$jscomp$98$$, $y$jscomp$78$$, $marauroa$$.$me$._x, $marauroa$$.$me$._y) : $JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$.volume = 0.0), $JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$.$radius$ = $radius$jscomp$6$$, $JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$.x = 
    $x$jscomp$98$$, $JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$.y = $y$jscomp$78$$, $JSCompiler_StaticMethods_playLocalizedEffect$self_snd$jscomp$3$$;
  }
}
function $JSCompiler_StaticMethods_playGlobalizedEffect$$($JSCompiler_StaticMethods_playGlobalizedEffect$self$$, $soundname$jscomp$2$$, $layer$jscomp$4$$, $volume$jscomp$2$$, $loop$jscomp$2$$) {
  $volume$jscomp$2$$ = void 0 === $volume$jscomp$2$$ ? 1.0 : $volume$jscomp$2$$;
  $loop$jscomp$2$$ = void 0 === $loop$jscomp$2$$ ? !1 : $loop$jscomp$2$$;
  "undefined" === typeof $layer$jscomp$4$$ && ($layer$jscomp$4$$ = $JSCompiler_StaticMethods_playGlobalizedEffect$self$$.$layers$.indexOf("gui"));
  return $JSCompiler_StaticMethods_playEffect$$($JSCompiler_StaticMethods_playGlobalizedEffect$self$$, $soundname$jscomp$2$$, $JSCompiler_StaticMethods_getLayerName$$($JSCompiler_StaticMethods_playGlobalizedEffect$self$$, $layer$jscomp$4$$), $volume$jscomp$2$$, $loop$jscomp$2$$);
}
$module$build$ts$ui$SoundManager$default$$.$SoundManager$.prototype.stop = function($layer$jscomp$8_layerName$jscomp$1$$, $sound$jscomp$1$$) {
  if (0 > $layer$jscomp$8_layerName$jscomp$1$$ || $layer$jscomp$8_layerName$jscomp$1$$ >= this.$layers$.length) {
    return console.error("cannot stop sound on non-existent layer: " + $layer$jscomp$8_layerName$jscomp$1$$), !1;
  }
  $layer$jscomp$8_layerName$jscomp$1$$ = this.$layers$[$layer$jscomp$8_layerName$jscomp$1$$];
  var $idx$jscomp$4$$ = this.active[$layer$jscomp$8_layerName$jscomp$1$$].indexOf($sound$jscomp$1$$);
  if ($sound$jscomp$1$$ && -1 < $idx$jscomp$4$$ && ($sound$jscomp$1$$.pause(), $sound$jscomp$1$$.currentTime = 0, $sound$jscomp$1$$.onended)) {
    $sound$jscomp$1$$.onended(new Event("stopsound"));
  }
  return 0 > this.active[$layer$jscomp$8_layerName$jscomp$1$$].indexOf($sound$jscomp$1$$);
};
function $JSCompiler_StaticMethods_adjustForDistance$$($layername$jscomp$4_maxvol$$, $snd$jscomp$6$$, $rad2_radius$jscomp$9$$, $sx$jscomp$3_xdist$$, $dist2_sy$jscomp$4_ydist$$, $ex$$, $ey$$) {
  $sx$jscomp$3_xdist$$ = $ex$$ - $sx$jscomp$3_xdist$$;
  $dist2_sy$jscomp$4_ydist$$ = $ey$$ - $dist2_sy$jscomp$4_ydist$$;
  $dist2_sy$jscomp$4_ydist$$ = $sx$jscomp$3_xdist$$ * $sx$jscomp$3_xdist$$ + $dist2_sy$jscomp$4_ydist$$ * $dist2_sy$jscomp$4_ydist$$;
  $rad2_radius$jscomp$9$$ *= $rad2_radius$jscomp$9$$;
  $dist2_sy$jscomp$4_ydist$$ > $rad2_radius$jscomp$9$$ ? $snd$jscomp$6$$.volume = 0.0 : ($layername$jscomp$4_maxvol$$ = $JSCompiler_StaticMethods_getAdjustedVolume$$($layername$jscomp$4_maxvol$$, $snd$jscomp$6$$.$basevolume$), $snd$jscomp$6$$.volume = $JSCompiler_StaticMethods_normVolume$$(Math.min($rad2_radius$jscomp$9$$ / (20 * $dist2_sy$jscomp$4_ydist$$), $layername$jscomp$4_maxvol$$)));
}
function $JSCompiler_StaticMethods_normVolume$$($vol$$) {
  return 0 > $vol$$ ? 0 : 1 < $vol$$ ? 1 : $vol$$;
}
function $JSCompiler_StaticMethods_getAdjustedVolume$$($layername$jscomp$5$$, $actualvol_basevol$$) {
  $actualvol_basevol$$ *= $JSCompiler_StaticMethods_getFloat$$($stendhal$$.$config$, "ui.sound.master.volume");
  var $lvol$$ = $JSCompiler_StaticMethods_getFloat$$($stendhal$$.$config$, "ui.sound." + $layername$jscomp$5$$ + ".volume");
  return "number" !== typeof $lvol$$ ? (console.warn('cannot adjust volume for layer "' + $layername$jscomp$5$$ + '"'), $actualvol_basevol$$) : $actualvol_basevol$$ * $lvol$$;
}
function $JSCompiler_StaticMethods_getVolume$$($layername$jscomp$8$$) {
  $layername$jscomp$8$$ = void 0 === $layername$jscomp$8$$ ? "master" : $layername$jscomp$8$$;
  var $vol$jscomp$2$$ = $JSCompiler_StaticMethods_getFloat$$($stendhal$$.$config$, "ui.sound." + $layername$jscomp$8$$ + ".volume");
  return "undefined" === typeof $vol$jscomp$2$$ || isNaN($vol$jscomp$2$$) || !isFinite($vol$jscomp$2$$) ? (console.warn('could not get volume for channel "' + $layername$jscomp$8$$ + '"'), 1) : $JSCompiler_StaticMethods_normVolume$$($vol$jscomp$2$$);
}
$module$build$ts$ui$SoundManager$default$$.$SoundManager$.prototype.$startupCache$ = function() {
  this.load("ui/login", $stendhal$$.$paths$.$sounds$ + "/ui/login.ogg", !0);
};
var $module$build$ts$ui$toolkit$FloatingWindow$default$$ = {};
Object.defineProperty($module$build$ts$ui$toolkit$FloatingWindow$default$$, "__esModule", {value:!0});
$module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$ = void 0;
$module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$ = function($closeButton_title$jscomp$12$$, $contentComponent$$, $titleBar_x$jscomp$101$$, $y$jscomp$81$$) {
  var $$jscomp$super$this$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "window-template") || this;
  $$jscomp$super$this$$.$B$ = $contentComponent$$;
  $$jscomp$super$this$$.$C$ = "click-1";
  $$jscomp$super$this$$.$D$ = !0;
  $$jscomp$super$this$$.offsetX = 0;
  $$jscomp$super$this$$.offsetY = 0;
  $$jscomp$super$this$$.content = $contentComponent$$;
  $$jscomp$super$this$$.$componentElement$.style.position = "absolute";
  $$jscomp$super$this$$.$componentElement$.style.left = $titleBar_x$jscomp$101$$ + "px";
  $$jscomp$super$this$$.$componentElement$.style.top = $y$jscomp$81$$ + "px";
  $titleBar_x$jscomp$101$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$$, ".windowtitlebar");
  $JSCompiler_StaticMethods_applyTheme$$($stendhal$$.$config$, $titleBar_x$jscomp$101$$);
  $closeButton_title$jscomp$12$$ ? $JSCompiler_StaticMethods_child$$($$jscomp$super$this$$, ".windowtitle").textContent = $closeButton_title$jscomp$12$$ : $titleBar_x$jscomp$101$$.classList.add("hidden");
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$$, ".windowcontent").append($contentComponent$$.$componentElement$);
  $titleBar_x$jscomp$101$$.addEventListener("mousedown", function($event$jscomp$6$$) {
    $$jscomp$super$this$$.$onMouseDown$($event$jscomp$6$$);
  });
  $titleBar_x$jscomp$101$$.addEventListener("touchstart", function($event$jscomp$7$$) {
    $$jscomp$super$this$$.$onTouchStart$($event$jscomp$7$$);
  });
  $closeButton_title$jscomp$12$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$$, ".windowtitleclose");
  $closeButton_title$jscomp$12$$.addEventListener("click", function($event$jscomp$8$$) {
    $$jscomp$super$this$$.close();
    $event$jscomp$8$$.preventDefault();
    $JSCompiler_StaticMethods_playGlobalizedEffect$$($module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$.$A$, $$jscomp$super$this$$.$C$);
  });
  $closeButton_title$jscomp$12$$.addEventListener("touchend", function($event$jscomp$9$$) {
    $$jscomp$super$this$$.close();
    $event$jscomp$9$$.preventDefault();
    $JSCompiler_StaticMethods_playGlobalizedEffect$$($module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$.$A$, $$jscomp$super$this$$.$C$);
  });
  $$jscomp$super$this$$.$v$ = function($event$jscomp$10_firstT$jscomp$inline_246$$) {
    if ("mousemove" === $event$jscomp$10_firstT$jscomp$inline_246$$.type) {
      $$jscomp$super$this$$.$componentElement$.style.left = $event$jscomp$10_firstT$jscomp$inline_246$$.clientX - $$jscomp$super$this$$.offsetX + "px", $$jscomp$super$this$$.$componentElement$.style.top = $event$jscomp$10_firstT$jscomp$inline_246$$.clientY - $$jscomp$super$this$$.offsetY + "px", $$jscomp$super$this$$.$onMoved$();
    } else {
      $event$jscomp$10_firstT$jscomp$inline_246$$ = $event$jscomp$10_firstT$jscomp$inline_246$$.changedTouches[0];
      var $simulated$jscomp$inline_247$$ = new MouseEvent("mousemove", {screenX:$event$jscomp$10_firstT$jscomp$inline_246$$.screenX, screenY:$event$jscomp$10_firstT$jscomp$inline_246$$.screenY, clientX:$event$jscomp$10_firstT$jscomp$inline_246$$.clientX, clientY:$event$jscomp$10_firstT$jscomp$inline_246$$.clientY});
      $event$jscomp$10_firstT$jscomp$inline_246$$.target.dispatchEvent($simulated$jscomp$inline_247$$);
    }
  };
  $$jscomp$super$this$$.$A$ = function() {
    window.removeEventListener("mousemove", $$jscomp$super$this$$.$v$, !0);
    window.removeEventListener("mouseup", $$jscomp$super$this$$.$A$, !0);
    window.removeEventListener("touchmove", $$jscomp$super$this$$.$v$, !0);
    window.removeEventListener("touchend", $$jscomp$super$this$$.$A$, !0);
  };
  $contentComponent$$.$componentElement$.addEventListener("close", function($event$jscomp$11$$) {
    $$jscomp$super$this$$.close();
    $event$jscomp$11$$.preventDefault();
  });
  $$jscomp$super$this$$.$B$.$parentComponent$ = $$jscomp$super$this$$;
  document.getElementById("popupcontainer").appendChild($$jscomp$super$this$$.$componentElement$);
  return $$jscomp$super$this$$;
};
$$jscomp$inherits$$($module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$.prototype;
$JSCompiler_prototypeAlias$$.close = function() {
  this.$componentElement$.remove();
  this.$B$.$onParentClose$();
  this.$D$ = !1;
  this.$B$.$parentComponent$ = void 0;
};
$JSCompiler_prototypeAlias$$.$isOpen$ = function() {
  return this.$D$;
};
$JSCompiler_prototypeAlias$$.$onMouseDown$ = function($event$jscomp$13$$) {
  window.addEventListener("mousemove", this.$v$, !0);
  window.addEventListener("mouseup", this.$A$, !0);
  window.addEventListener("touchmove", this.$v$, !0);
  window.addEventListener("touchend", this.$A$, !0);
  $event$jscomp$13$$.preventDefault();
  var $box$$ = this.$componentElement$.getBoundingClientRect();
  this.offsetX = $event$jscomp$13$$.clientX - $box$$.left - window.pageXOffset;
  this.offsetY = $event$jscomp$13$$.clientY - $box$$.top - window.pageYOffset;
};
$JSCompiler_prototypeAlias$$.$onTouchStart$ = function($event$jscomp$14$$) {
  var $firstT$$ = $event$jscomp$14$$.changedTouches[0], $simulated$$ = new MouseEvent("mousedown", {screenX:$firstT$$.screenX, screenY:$firstT$$.screenY, clientX:$firstT$$.clientX, clientY:$firstT$$.clientY});
  $firstT$$.target.dispatchEvent($simulated$$);
  $event$jscomp$14$$.preventDefault();
};
$JSCompiler_prototypeAlias$$.$onMoved$ = function() {
  var $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$;
  this.content && this.content.$onMoved$();
  var $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$ = this.$componentElement$.getBoundingClientRect();
  var $JSCompiler_object_inline_y_987_clientArea$jscomp$inline_257$$ = document.documentElement.getBoundingClientRect();
  var $clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$ = $JSCompiler_object_inline_y_987_clientArea$jscomp$inline_257$$.height;
  0 == $clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$ && ($clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$ = (null === ($JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$ = window.visualViewport) || void 0 === $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$ ? void 0 : $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.height) || 
  200);
  $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$ = $JSCompiler_StaticMethods_getPageOffset$$();
  var $newX$jscomp$inline_260$$ = $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.x, $newY$jscomp$inline_261$$ = $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.y;
  0 > $newX$jscomp$inline_260$$ ? ($newX$jscomp$inline_260$$ = 0, this.$componentElement$.style.left = $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.x + $newX$jscomp$inline_260$$ + "px") : $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.x + $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.width > $JSCompiler_object_inline_y_987_clientArea$jscomp$inline_257$$.right + $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.x && 
  ($newX$jscomp$inline_260$$ = $JSCompiler_object_inline_y_987_clientArea$jscomp$inline_257$$.right - $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.width, this.$componentElement$.style.left = $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.x + $newX$jscomp$inline_260$$ + "px");
  0 > $newY$jscomp$inline_261$$ ? ($newY$jscomp$inline_261$$ = 0, this.$componentElement$.style.top = $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.y + $newY$jscomp$inline_261$$ + "px") : $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.y + $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.height > $clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$ && ($newY$jscomp$inline_261$$ = $clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$ - 
  $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$.height, this.$componentElement$.style.top = $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.y + $newY$jscomp$inline_261$$ + "px");
  $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$ = $newX$jscomp$inline_260$$ + $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.x;
  $JSCompiler_object_inline_y_987_clientArea$jscomp$inline_257$$ = $newY$jscomp$inline_261$$ + $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.y;
  "undefined" !== typeof this.$windowId$ && ($clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$ = this.$windowId$, $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$ = $stendhal$$.$config$, $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.$A$[$clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$] = {x:$JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$, 
  y:$JSCompiler_object_inline_y_987_clientArea$jscomp$inline_257$$}, $JSCompiler_StaticMethods_setWindowState$self$jscomp$inline_266__a$jscomp$inline_255_offset$jscomp$inline_259$$.set("ui.window." + $clientAreaHeight$jscomp$inline_258_id$jscomp$inline_263$$, $JSCompiler_object_inline_x_986_dialogArea$jscomp$inline_256$$ + "," + $JSCompiler_object_inline_y_987_clientArea$jscomp$inline_257$$));
};
$module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$.$A$ = $module$build$ts$ui$SoundManager$default$$.$SoundManager$.get();
var $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$ = {};
Object.defineProperty($module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$, "__esModule", {value:!0});
$module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$ = void 0;
$module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$ = function($$jscomp$super$this$jscomp$1_title$jscomp$13$$, $contentComponent$jscomp$1$$, $x$jscomp$102$$, $y$jscomp$82$$) {
  $$jscomp$super$this$jscomp$1_title$jscomp$13$$ = $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$.call(this, $$jscomp$super$this$jscomp$1_title$jscomp$13$$, $contentComponent$jscomp$1$$, $x$jscomp$102$$, $y$jscomp$82$$) || this;
  $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$v$ && $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$v$.close();
  return $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$v$ = $$jscomp$super$this$jscomp$1_title$jscomp$13$$;
};
$$jscomp$inherits$$($module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$, $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$);
$module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$A$ = $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$.$A$;
$module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.prototype.close = function() {
  $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$.prototype.close.call(this);
  $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$v$ === this && ($module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$v$ = void 0);
};
$module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$B$ = function() {
  $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$v$ && $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$v$.close();
};
var $module$build$ts$ui$UI$default$$ = {};
Object.defineProperty($module$build$ts$ui$UI$default$$, "__esModule", {value:!0});
$module$build$ts$ui$UI$default$$.$ui$ = void 0;
function $UI$$module$build$ts$ui$UI$$() {
  this.$v$ = new Map();
}
function $JSCompiler_StaticMethods_createSingletonFloatingWindow$$($title$jscomp$14$$, $contentComponent$jscomp$2$$, $x$jscomp$103$$, $y$jscomp$83$$) {
  return new $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$($title$jscomp$14$$, $contentComponent$jscomp$2$$, $x$jscomp$103$$, $y$jscomp$83$$);
}
function $JSCompiler_StaticMethods_registerComponent$$($key$jscomp$61$$, $component$$) {
  $module$build$ts$ui$UI$default$$.$ui$.$v$.set($key$jscomp$61$$, $component$$);
}
function $JSCompiler_StaticMethods_unregisterComponent$$($component$jscomp$1$$) {
  for (var $JSCompiler_StaticMethods_unregisterComponent$self$$ = $module$build$ts$ui$UI$default$$.$ui$, $$jscomp$iter$15$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_unregisterComponent$self$$.$v$.entries()), $$jscomp$key$entry_entry$jscomp$10$$ = $$jscomp$iter$15$$.next(); !$$jscomp$key$entry_entry$jscomp$10$$.done; $$jscomp$key$entry_entry$jscomp$10$$ = $$jscomp$iter$15$$.next()) {
    if ($$jscomp$key$entry_entry$jscomp$10$$ = $$jscomp$key$entry_entry$jscomp$10$$.value, $$jscomp$key$entry_entry$jscomp$10$$[1] === $component$jscomp$1$$) {
      $JSCompiler_StaticMethods_unregisterComponent$self$$.$v$.delete($$jscomp$key$entry_entry$jscomp$10$$[0]);
      break;
    }
  }
}
$UI$$module$build$ts$ui$UI$$.prototype.get = function($key$jscomp$62$$) {
  return this.$v$.get($key$jscomp$62$$);
};
function $JSCompiler_StaticMethods_getPageOffset$$() {
  var $body$jscomp$1$$ = document.body, $delem$$ = document.documentElement;
  return {x:window.pageXOffset || $delem$$.scrollLeft || $body$jscomp$1$$.scrollLeft, y:window.pageYOffset || $delem$$.scrollTop || $body$jscomp$1$$.scrollTop};
}
$module$build$ts$ui$UI$default$$.$ui$ = new $UI$$module$build$ts$ui$UI$$();
var $module$build$ts$ui$UIComponentEnum$default$$ = {};
Object.defineProperty($module$build$ts$ui$UIComponentEnum$default$$, "__esModule", {value:!0});
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$ = void 0;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$ = function() {
};
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$O$ = 0;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$H$ = 1;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$L$ = 2;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$A$ = 3;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$v$ = 101;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$B$ = 102;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$C$ = 103;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$D$ = 104;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$F$ = 105;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$J$ = 106;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$G$ = 107;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$I$ = 108;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$P$ = 109;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$K$ = 110;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$M$ = 111;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$N$ = 112;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$TradeDialog$ = 201;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$TravelLogDialog$ = 202;
$module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$OutfitDialog$ = 203;
var $module$build$ts$util$Chat$default$$ = {};
Object.defineProperty($module$build$ts$util$Chat$default$$, "__esModule", {value:!0});
$module$build$ts$util$Chat$default$$.$Chat$ = void 0;
$module$build$ts$util$Chat$default$$.$Chat$ = function() {
};
$module$build$ts$util$Chat$default$$.$Chat$.log = function($type$jscomp$158$$, $$jscomp$iter$16_message$jscomp$29$$, $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$, $profile$$, $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$) {
  $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$ = (void 0 === $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$ ? !1 : $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$) || "undefined" !== typeof $profile$$;
  $module$build$ts$util$Chat$default$$.$Chat$.$B$ || ($module$build$ts$util$Chat$default$$.$Chat$.$B$ = $module$build$ts$ui$UI$default$$.$ui$.get(104));
  if ("emoji" === $type$jscomp$158$$ && $$jscomp$iter$16_message$jscomp$29$$ instanceof HTMLImageElement) {
    var $JSCompiler_StaticMethods_addEmojiLine$self$jscomp$inline_270_JSCompiler_StaticMethods_addLines$self$jscomp$inline_278$$ = $module$build$ts$util$Chat$default$$.$Chat$.$B$, $lcol$jscomp$inline_271_stamped$jscomp$inline_279$$ = document.createElement("div");
    $lcol$jscomp$inline_271_stamped$jscomp$inline_279$$.className = "logcolL";
    $lcol$jscomp$inline_271_stamped$jscomp$inline_279$$.appendChild($JSCompiler_StaticMethods_createTimestamp$$());
    var $$jscomp$inline_280_rcol$jscomp$inline_272$$ = document.createElement("div");
    $$jscomp$inline_280_rcol$jscomp$inline_272$$.className = "logcolR lognormal";
    $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$ && ($$jscomp$inline_280_rcol$jscomp$inline_272$$.innerHTML += $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$ + ": ");
    $$jscomp$inline_280_rcol$jscomp$inline_272$$.appendChild($$jscomp$iter$16_message$jscomp$29$$.cloneNode());
    $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$ = document.createElement("div");
    $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$.className = "logrow";
    $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$.appendChild($lcol$jscomp$inline_271_stamped$jscomp$inline_279$$);
    $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$.appendChild($$jscomp$inline_280_rcol$jscomp$inline_272$$);
    $JSCompiler_StaticMethods_addEmojiLine$self$jscomp$inline_270_JSCompiler_StaticMethods_addLines$self$jscomp$inline_278$$.add($$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$);
  } else {
    if ("string" === typeof $$jscomp$iter$16_message$jscomp$29$$) {
      $JSCompiler_StaticMethods_addLine$$($module$build$ts$util$Chat$default$$.$Chat$.$B$, $type$jscomp$158$$, $$jscomp$iter$16_message$jscomp$29$$, $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$);
    } else {
      if ("[object Array]" === Object.prototype.toString.call($$jscomp$iter$16_message$jscomp$29$$)) {
        $JSCompiler_StaticMethods_addEmojiLine$self$jscomp$inline_270_JSCompiler_StaticMethods_addLines$self$jscomp$inline_278$$ = $module$build$ts$util$Chat$default$$.$Chat$.$B$;
        $lcol$jscomp$inline_271_stamped$jscomp$inline_279$$ = !1;
        $$jscomp$inline_280_rcol$jscomp$inline_272$$ = $$jscomp$makeIterator$$($$jscomp$iter$16_message$jscomp$29$$);
        for (var $$jscomp$inline_281_line$jscomp$inline_282$$ = $$jscomp$inline_280_rcol$jscomp$inline_272$$.next(); !$$jscomp$inline_281_line$jscomp$inline_282$$.done; $$jscomp$inline_281_line$jscomp$inline_282$$ = $$jscomp$inline_280_rcol$jscomp$inline_272$$.next()) {
          $$jscomp$inline_281_line$jscomp$inline_282$$ = $$jscomp$inline_281_line$jscomp$inline_282$$.value, $lcol$jscomp$inline_271_stamped$jscomp$inline_279$$ ? $JSCompiler_StaticMethods_addLine$$($JSCompiler_StaticMethods_addEmojiLine$self$jscomp$inline_270_JSCompiler_StaticMethods_addLines$self$jscomp$inline_278$$, $type$jscomp$158$$, $$jscomp$inline_281_line$jscomp$inline_282$$, $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$, !1) : ($JSCompiler_StaticMethods_addLine$$($JSCompiler_StaticMethods_addEmojiLine$self$jscomp$inline_270_JSCompiler_StaticMethods_addLines$self$jscomp$inline_278$$, 
          $type$jscomp$158$$, $$jscomp$inline_281_line$jscomp$inline_282$$, $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$), $lcol$jscomp$inline_271_stamped$jscomp$inline_279$$ = !0);
        }
      }
    }
  }
  if ($marauroa$$.$me$ && $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$) {
    for ($JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$ = [], "string" === typeof $$jscomp$iter$16_message$jscomp$29$$ ? $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$.push($$jscomp$iter$16_message$jscomp$29$$) : $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$ = $$jscomp$iter$16_message$jscomp$29$$, $$jscomp$iter$16_message$jscomp$29$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$), 
    $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$ = $$jscomp$iter$16_message$jscomp$29$$.next(); !$$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$.done; $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$ = $$jscomp$iter$16_message$jscomp$29$$.next()) {
      $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$ = $stendhal$$.$ui$.$gamewindow$, $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$ = new $module$build$ts$sprite$NotificationBubble$default$$.$NotificationBubble$($type$jscomp$158$$, $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$.value, $profile$$), $JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$.$v$.push($$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$), 
      $$jscomp$key$m_bubble$jscomp$inline_288_orator_row$jscomp$inline_273$$.$onAdded$($JSCompiler_StaticMethods_addNotifSprite$self$jscomp$inline_287_headed_messages$$.$ctx$);
    }
  }
};
$module$build$ts$util$Chat$default$$.$Chat$.$v$ = function($type$jscomp$159$$, $message$jscomp$30$$) {
  $module$build$ts$util$Chat$default$$.$Chat$.log($type$jscomp$159$$, $message$jscomp$30$$, void 0, void 0, !0);
};
$module$build$ts$util$Chat$default$$.$Chat$.debug = function($message$jscomp$31$$) {
  $module$build$ts$util$Chat$default$$.$Chat$.$D$ && $module$build$ts$util$Chat$default$$.$Chat$.log("client", $message$jscomp$31$$);
};
$module$build$ts$util$Chat$default$$.$Chat$.$D$ = !1;
$module$build$ts$util$Chat$default$$.$Chat$.$A$ = ["hello"];
var $module$build$ts$event$RPEvent$default$$ = {};
Object.defineProperty($module$build$ts$event$RPEvent$default$$, "__esModule", {value:!0});
$module$build$ts$event$RPEvent$default$$.$RPEvent$ = void 0;
$module$build$ts$event$RPEvent$default$$.$RPEvent$ = function() {
};
var $module$build$ts$event$ChatOptionsEvent$default$$ = {};
Object.defineProperty($module$build$ts$event$ChatOptionsEvent$default$$, "__esModule", {value:!0});
$module$build$ts$event$ChatOptionsEvent$default$$.$ChatOptionsEvent$ = void 0;
$module$build$ts$event$ChatOptionsEvent$default$$.$ChatOptionsEvent$ = function() {
  return $module$build$ts$event$RPEvent$default$$.$RPEvent$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$event$ChatOptionsEvent$default$$.$ChatOptionsEvent$, $module$build$ts$event$RPEvent$default$$.$RPEvent$);
$module$build$ts$event$ChatOptionsEvent$default$$.$ChatOptionsEvent$.prototype.$execute$ = function($entity_m$jscomp$10_message$jscomp$32$$) {
  if ($entity_m$jscomp$10_message$jscomp$32$$ === $marauroa$$.$me$) {
    $entity_m$jscomp$10_message$jscomp$32$$ = [];
    for (var $$jscomp$iter$17$$ = $$jscomp$makeIterator$$(this.options.split("\t")), $$jscomp$key$optionListEntry$$ = $$jscomp$iter$17$$.next(); !$$jscomp$key$optionListEntry$$.done; $$jscomp$key$optionListEntry$$ = $$jscomp$iter$17$$.next()) {
      $entity_m$jscomp$10_message$jscomp$32$$.push($$jscomp$key$optionListEntry$$.value.split("|~|")[1]);
    }
    $module$build$ts$util$Chat$default$$.$Chat$.$C$ = this.npc;
    $module$build$ts$util$Chat$default$$.$Chat$.$A$ = $entity_m$jscomp$10_message$jscomp$32$$;
    $entity_m$jscomp$10_message$jscomp$32$$ = "Chat options for " + this.npc + ": " + $entity_m$jscomp$10_message$jscomp$32$$.join(", ");
    console.log($entity_m$jscomp$10_message$jscomp$32$$);
    $module$build$ts$util$Chat$default$$.$Chat$.debug($entity_m$jscomp$10_message$jscomp$32$$);
  }
};
var $module$build$ts$ui$dialog$ImageViewerDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$ImageViewerDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$ImageViewerDialog$default$$.$ImageViewerDialog$ = void 0;
$module$build$ts$ui$dialog$ImageViewerDialog$default$$.$ImageViewerDialog$ = function($caption$$, $imageFilename$$) {
  var $$jscomp$super$this$jscomp$2$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "imageviewer-template") || this;
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$2$$, "h3").textContent = $caption$$;
  $$jscomp$super$this$jscomp$2$$.$componentElement$.querySelector("img").src = $imageFilename$$;
  return $$jscomp$super$this$jscomp$2$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$ImageViewerDialog$default$$.$ImageViewerDialog$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
var $module$build$ts$event$ExamineEvent$default$$ = {};
Object.defineProperty($module$build$ts$event$ExamineEvent$default$$, "__esModule", {value:!0});
$module$build$ts$event$ExamineEvent$default$$.$ExamineEvent$ = void 0;
$module$build$ts$event$ExamineEvent$default$$.$ExamineEvent$ = function() {
  return $module$build$ts$event$RPEvent$default$$.$RPEvent$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$event$ExamineEvent$default$$.$ExamineEvent$, $module$build$ts$event$RPEvent$default$$.$RPEvent$);
$module$build$ts$event$ExamineEvent$default$$.$ExamineEvent$.prototype.$execute$ = function($entity$jscomp$1$$) {
  $entity$jscomp$1$$ === $marauroa$$.$me$ && $JSCompiler_StaticMethods_createSingletonFloatingWindow$$(this.title, new $module$build$ts$ui$dialog$ImageViewerDialog$default$$.$ImageViewerDialog$(this.caption, this.path), 100, 50);
};
var $module$build$ts$event$GroupChangeEvent$default$$ = {};
Object.defineProperty($module$build$ts$event$GroupChangeEvent$default$$, "__esModule", {value:!0});
$module$build$ts$event$GroupChangeEvent$default$$.$GroupChangeEvent$ = void 0;
$module$build$ts$event$GroupChangeEvent$default$$.$GroupChangeEvent$ = function() {
  return $module$build$ts$event$RPEvent$default$$.$RPEvent$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$event$GroupChangeEvent$default$$.$GroupChangeEvent$, $module$build$ts$event$RPEvent$default$$.$RPEvent$);
$module$build$ts$event$GroupChangeEvent$default$$.$GroupChangeEvent$.prototype.$execute$ = function($entity$jscomp$2$$) {
  $entity$jscomp$2$$ === $marauroa$$.$me$ && ($stendhal$$.data.group.$updateGroupStatus$(this.members, this.leader, this.lootmode), $module$build$ts$ui$UI$default$$.$ui$.get(105).$updateGroupStatus$());
};
var $module$build$ts$event$GroupInviteEvent$default$$ = {};
Object.defineProperty($module$build$ts$event$GroupInviteEvent$default$$, "__esModule", {value:!0});
$module$build$ts$event$GroupInviteEvent$default$$.$GroupInviteEvent$ = void 0;
$module$build$ts$event$GroupInviteEvent$default$$.$GroupInviteEvent$ = function() {
  return $module$build$ts$event$RPEvent$default$$.$RPEvent$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$event$GroupInviteEvent$default$$.$GroupInviteEvent$, $module$build$ts$event$RPEvent$default$$.$RPEvent$);
$module$build$ts$event$GroupInviteEvent$default$$.$GroupInviteEvent$.prototype.$execute$ = function($JSCompiler_StaticMethods_expiredInvite$self$jscomp$inline_290_entity$jscomp$3$$) {
  if ($JSCompiler_StaticMethods_expiredInvite$self$jscomp$inline_290_entity$jscomp$3$$ === $marauroa$$.$me$) {
    if (this.expire) {
      $JSCompiler_StaticMethods_expiredInvite$self$jscomp$inline_290_entity$jscomp$3$$ = $module$build$ts$ui$UI$default$$.$ui$.get(105);
      var $leader$jscomp$inline_291$$ = this.leader, $button$jscomp$inline_292$$ = $JSCompiler_StaticMethods_expiredInvite$self$jscomp$inline_290_entity$jscomp$3$$.$v$[$leader$jscomp$inline_291$$];
      $button$jscomp$inline_292$$ && ($button$jscomp$inline_292$$.remove(), delete $JSCompiler_StaticMethods_expiredInvite$self$jscomp$inline_290_entity$jscomp$3$$.$v$[$leader$jscomp$inline_291$$]);
    } else {
      $JSCompiler_StaticMethods_receivedInvite$$($module$build$ts$ui$UI$default$$.$ui$.get(105), this.leader);
    }
  }
};
var $module$build$ts$ui$toolkit$DialogContentComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$toolkit$DialogContentComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$ = void 0;
$module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$ = function($$jscomp$super$this$jscomp$3_id$jscomp$14$$) {
  $$jscomp$super$this$jscomp$3_id$jscomp$14$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, $$jscomp$super$this$jscomp$3_id$jscomp$14$$, !0) || this;
  $$jscomp$super$this$jscomp$3_id$jscomp$14$$.$componentElement$.classList.add("dialogcontents");
  return $$jscomp$super$this$jscomp$3_id$jscomp$14$$;
};
$$jscomp$inherits$$($module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.prototype.close = function() {
  this.frame && this.frame.close();
};
var $module$build$ts$ui$dialog$TravelLogDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$TravelLogDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$TravelLogDialog$default$$.$TravelLogDialog$ = void 0;
$module$build$ts$ui$dialog$TravelLogDialog$default$$.$TravelLogDialog$ = function($dataItems$$) {
  var $$jscomp$super$this$jscomp$4$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "travellogdialog-template") || this;
  $$jscomp$super$this$jscomp$4$$.$v$ = "";
  $$jscomp$super$this$jscomp$4$$.$A$ = {};
  $JSCompiler_StaticMethods_registerComponent$$($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$TravelLogDialog$, $$jscomp$super$this$jscomp$4$$);
  $$jscomp$super$this$jscomp$4$$.refresh();
  $JSCompiler_StaticMethods_JSC$1814_createHtml$$($$jscomp$super$this$jscomp$4$$, $dataItems$$);
  $$jscomp$super$this$jscomp$4$$.$v$ = $dataItems$$[0];
  $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$$jscomp$super$this$jscomp$4$$.$v$});
  return $$jscomp$super$this$jscomp$4$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$TravelLogDialog$default$$.$TravelLogDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
$module$build$ts$ui$dialog$TravelLogDialog$default$$.$TravelLogDialog$.prototype.refresh = function() {
  this.$componentElement$.style.setProperty("font-family", $stendhal$$.$config$.get("ui.font.tlog"));
};
function $JSCompiler_StaticMethods_JSC$1814_createHtml$$($JSCompiler_StaticMethods_JSC$1814_createHtml$self$$, $dataItems$jscomp$1$$) {
  for (var $buttons$$ = "", $i$jscomp$50$$ = 0; $i$jscomp$50$$ < $dataItems$jscomp$1$$.length; $i$jscomp$50$$++) {
    $buttons$$ = $buttons$$ + '<button id="' + $JSCompiler_StaticMethods_esc$$($dataItems$jscomp$1$$[$i$jscomp$50$$]) + '" class="progressTypeButton">' + $JSCompiler_StaticMethods_esc$$($dataItems$jscomp$1$$[$i$jscomp$50$$]) + "</button>";
  }
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_JSC$1814_createHtml$self$$, ".tavellogtabpanel").innerHTML = $buttons$$;
  $JSCompiler_StaticMethods_JSC$1814_createHtml$self$$.$componentElement$.querySelectorAll(".progressTypeButton").forEach(function($button$$) {
    $button$$.addEventListener("click", function($event$jscomp$17$$) {
      $JSCompiler_StaticMethods_refreshDetails$$($JSCompiler_StaticMethods_JSC$1814_createHtml$self$$);
      $JSCompiler_StaticMethods_JSC$1814_createHtml$self$$.$v$ = $event$jscomp$17$$.target.id;
      $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$JSCompiler_StaticMethods_JSC$1814_createHtml$self$$.$v$});
      "Completed Quests" === $JSCompiler_StaticMethods_JSC$1814_createHtml$self$$.$v$ && $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:"repeatable"});
    });
  });
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_JSC$1814_createHtml$self$$, ".travellogitems").addEventListener("change", function($event$jscomp$18$$) {
    $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$JSCompiler_StaticMethods_JSC$1814_createHtml$self$$.$v$, item:$event$jscomp$18$$.target.value});
  });
}
function $JSCompiler_StaticMethods_refreshDetails$$($JSCompiler_StaticMethods_refreshDetails$self_details$jscomp$1$$, $html$jscomp$2$$, $newDetails$$) {
  $JSCompiler_StaticMethods_refreshDetails$self_details$jscomp$1$$ = $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_refreshDetails$self_details$jscomp$1$$, ".travellogdetails");
  $JSCompiler_StaticMethods_refreshDetails$self_details$jscomp$1$$.innerHTML = void 0 === $html$jscomp$2$$ ? "" : $html$jscomp$2$$;
  $newDetails$$ && $JSCompiler_StaticMethods_refreshDetails$self_details$jscomp$1$$.appendChild($newDetails$$);
}
$module$build$ts$ui$dialog$TravelLogDialog$default$$.$TravelLogDialog$.prototype.$onParentClose$ = function() {
  $JSCompiler_StaticMethods_unregisterComponent$$(this);
};
var $module$build$ts$event$ProgressStatusEvent$default$$ = {};
Object.defineProperty($module$build$ts$event$ProgressStatusEvent$default$$, "__esModule", {value:!0});
$module$build$ts$event$ProgressStatusEvent$default$$.$ProgressStatusEvent$ = void 0;
$module$build$ts$event$ProgressStatusEvent$default$$.$ProgressStatusEvent$ = function() {
  return $module$build$ts$event$RPEvent$default$$.$RPEvent$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$event$ProgressStatusEvent$default$$.$ProgressStatusEvent$, $module$build$ts$event$RPEvent$default$$.$RPEvent$);
$module$build$ts$event$ProgressStatusEvent$default$$.$ProgressStatusEvent$.prototype.$execute$ = function() {
  var $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$ = this.progress_type, $$jscomp$inline_315_dataItems$jscomp$5$$ = this.data.substring(1, this.data.length - 1).split(/\t/), $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$ = $module$build$ts$ui$UI$default$$.$ui$.get($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$TravelLogDialog$);
  if (this.progress_type) {
    if ($JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$) {
      if (this.item) {
        var $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ = this.item, $description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$ = this.description;
        if ($detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$ === $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$.$v$) {
          $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$ = document.createElement("span");
          $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$.innerHTML = "<h3>" + $JSCompiler_StaticMethods_esc$$($$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$) + "</h3>";
          $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$.$A$[$$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$] && ($detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$.innerHTML += '<p id="travellogrepeatable"><img src="' + $stendhal$$.$paths$.$gui$ + '/rp.png" /> <em>I can do this quest again.</em></p>');
          $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$.innerHTML += '<p id="travellogdescription">' + $JSCompiler_StaticMethods_esc$$($description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$) + "</p>";
          $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ = document.createElement("ul");
          $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$.className = "uniform";
          for ($description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$ = 0; $description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$ < $$jscomp$inline_315_dataItems$jscomp$5$$.length; $description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$++) {
            var $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$ = [], $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$ = $JSCompiler_StaticMethods_esc$$($$jscomp$inline_315_dataItems$jscomp$5$$[$description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$], ["em", "tally"]);
            if ($html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$.includes("<tally>") && $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$.includes("</tally>")) {
              var $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$ = $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$.split("<tally>");
              $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$ = $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$[0];
              $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$ = $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$[1].split("</tally>");
              $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$ = $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$[1];
              var $count$jscomp$inline_1011_tally$jscomp$inline_1015$$ = parseInt($tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$[0].trim(), 10);
              $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$ = "";
              if (0 < $count$jscomp$inline_1011_tally$jscomp$inline_1015$$) {
                for (var $t$jscomp$inline_1013$$ = 0, $idx$jscomp$inline_1014$$ = 0; $idx$jscomp$inline_1014$$ < $count$jscomp$inline_1011_tally$jscomp$inline_1015$$; $idx$jscomp$inline_1014$$++) {
                  $t$jscomp$inline_1013$$++, 5 == $t$jscomp$inline_1013$$ && ($tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$ += "5", $t$jscomp$inline_1013$$ = 0);
                }
                0 < $t$jscomp$inline_1013$$ && ($tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$ += $t$jscomp$inline_1013$$);
              } else {
                $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$ = "0";
              }
              $count$jscomp$inline_1011_tally$jscomp$inline_1015$$ = document.createElement("span");
              $count$jscomp$inline_1011_tally$jscomp$inline_1015$$.className = "tally";
              $count$jscomp$inline_1011_tally$jscomp$inline_1015$$.textContent = $tallyString$jscomp$inline_1012_tmp$jscomp$inline_1008$$;
              $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$ = [$content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$, $count$jscomp$inline_1011_tally$jscomp$inline_1015$$, $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$];
            } else {
              $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$.push($html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$);
            }
            $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$ = document.createElement("li");
            $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$.className = "uniform";
            $html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$.innerHTML = $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$[0];
            $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$[1] && ($html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$.appendChild($content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$[1]), $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$[2] && ($html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$.innerHTML += $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$[2]));
            $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$.appendChild($html$jscomp$inline_309_li$jscomp$inline_310_post$jscomp$inline_1010$$);
          }
          $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$.appendChild($$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$);
          $JSCompiler_StaticMethods_refreshDetails$$($JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$, "", $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$);
        }
      } else {
        if ("repeatable" === $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$) {
          $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$ = {};
          $$jscomp$inline_315_dataItems$jscomp$5$$ = $$jscomp$makeIterator$$($$jscomp$inline_315_dataItems$jscomp$5$$);
          for ($$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ = $$jscomp$inline_315_dataItems$jscomp$5$$.next(); !$$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$.done; $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ = $$jscomp$inline_315_dataItems$jscomp$5$$.next()) {
            $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$[$$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$.value] = !0;
          }
          $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$.$A$ = $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$;
        } else {
          if ($detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$ === $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$.$v$) {
            $$jscomp$inline_315_dataItems$jscomp$5$$.sort();
            $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ = "";
            for ($description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$ = 0; $description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$ < $$jscomp$inline_315_dataItems$jscomp$5$$.length; $description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$++) {
              $content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$ = $$jscomp$inline_315_dataItems$jscomp$5$$[$description$jscomp$inline_303_i$jscomp$inline_307_i$jscomp$inline_322$$], $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ += '<option value="' + $JSCompiler_StaticMethods_esc$$($content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$) + '">' + $JSCompiler_StaticMethods_esc$$($content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$), 
              $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$.$A$[$content$jscomp$inline_308_currentItem$jscomp$inline_323_pre$jscomp$inline_1009$$] && ($$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ += " (R)"), $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$ += 
              "</option>";
            }
            $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$, ".travellogitems").innerHTML = $$jscomp$inline_316_html$jscomp$inline_321_selectedItem$jscomp$inline_302_ul$jscomp$inline_306$$;
            $$jscomp$inline_315_dataItems$jscomp$5$$ && $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$, item:$$jscomp$inline_315_dataItems$jscomp$5$$[0]});
          }
        }
      }
    }
  } else {
    $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$ || ($detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$ = $JSCompiler_StaticMethods_getWindowState$$("travellog"), $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$ = 
    new $module$build$ts$ui$dialog$TravelLogDialog$default$$.$TravelLogDialog$($$jscomp$inline_315_dataItems$jscomp$5$$), (new $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$("Travel Log", $JSCompiler_StaticMethods_itemData$self$jscomp$inline_300_JSCompiler_StaticMethods_progressTypeData$self$jscomp$inline_318_JSCompiler_StaticMethods_setRepeatable$self$jscomp$inline_312_travelLogDialog$$, $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$.x, 
    $detailsSpan$jscomp$inline_305_dstate_progressType$jscomp$2_repeatable$jscomp$inline_314$$.y)).$windowId$ = "travellog");
  }
};
var $module$build$ts$ui$dialog$ActionContextMenu$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$ActionContextMenu$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$ActionContextMenu$default$$.$ActionContextMenu$ = void 0;
$module$build$ts$ui$dialog$ActionContextMenu$default$$.$ActionContextMenu$ = function($content$jscomp$1_entity$jscomp$4$$, $append_i$jscomp$53$$) {
  $append_i$jscomp$53$$ = void 0 === $append_i$jscomp$53$$ ? [] : $append_i$jscomp$53$$;
  var $$jscomp$super$this$jscomp$5$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "contextmenu-template") || this;
  $$jscomp$super$this$jscomp$5$$.$v$ = $content$jscomp$1_entity$jscomp$4$$;
  $$jscomp$super$this$jscomp$5$$.$A$ = $append_i$jscomp$53$$;
  $JSCompiler_StaticMethods_gatherActions$$($$jscomp$super$this$jscomp$5$$);
  $content$jscomp$1_entity$jscomp$4$$ = '<div class="actionmenu">';
  for ($append_i$jscomp$53$$ = 0; $append_i$jscomp$53$$ < $$jscomp$super$this$jscomp$5$$.actions.length; $append_i$jscomp$53$$++) {
    $content$jscomp$1_entity$jscomp$4$$ += '<button id="actionbutton.' + $append_i$jscomp$53$$ + '">' + $JSCompiler_StaticMethods_esc$$($$jscomp$super$this$jscomp$5$$.actions[$append_i$jscomp$53$$].title) + "</button><br>";
  }
  $$jscomp$super$this$jscomp$5$$.$componentElement$.innerHTML = $content$jscomp$1_entity$jscomp$4$$ + "</div>";
  $$jscomp$super$this$jscomp$5$$.$componentElement$.addEventListener("click", function($event$jscomp$21_iStr$jscomp$inline_331$$) {
    var $_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$;
    $event$jscomp$21_iStr$jscomp$inline_331$$ = null === ($_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$ = $event$jscomp$21_iStr$jscomp$inline_331$$.target.getAttribute("id")) || void 0 === $_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$ ? void 0 : $_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$.substring(13);
    if (void 0 !== $event$jscomp$21_iStr$jscomp$inline_331$$ && "" !== $event$jscomp$21_iStr$jscomp$inline_331$$ && ($_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$ = parseInt($event$jscomp$21_iStr$jscomp$inline_331$$), !(0 > $_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$))) {
      $$jscomp$super$this$jscomp$5$$.$componentElement$.dispatchEvent(new Event("close"));
      if ($_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$ >= $$jscomp$super$this$jscomp$5$$.actions.length) {
        throw Error("actions index is larger than number of actions");
      }
      $$jscomp$super$this$jscomp$5$$.actions[$_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$].action ? $$jscomp$super$this$jscomp$5$$.actions[$_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$].action($$jscomp$super$this$jscomp$5$$.$v$) : ($_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$ = {type:$$jscomp$super$this$jscomp$5$$.actions[$_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$].type, target_path:$JSCompiler_StaticMethods_getIdPath$$($$jscomp$super$this$jscomp$5$$.$v$), 
      zone:$marauroa$$.$currentZoneName$}, "[" + $$jscomp$super$this$jscomp$5$$.$v$.id + "]" === $JSCompiler_StaticMethods_getIdPath$$($$jscomp$super$this$jscomp$5$$.$v$) && ($_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$.target = "#" + $$jscomp$super$this$jscomp$5$$.$v$.id), $marauroa$$.$clientFramework$.$sendAction$($_a$jscomp$inline_330_action$jscomp$inline_1019_i$jscomp$inline_332$$));
    }
  });
  return $$jscomp$super$this$jscomp$5$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$ActionContextMenu$default$$.$ActionContextMenu$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
function $JSCompiler_StaticMethods_gatherActions$$($JSCompiler_StaticMethods_gatherActions$self$$) {
  var $actions$$ = [];
  $JSCompiler_StaticMethods_gatherActions$self$$.$v$.$buildActions$($actions$$);
  for (var $$jscomp$iter$19$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_gatherActions$self$$.$A$), $$jscomp$key$action$$ = $$jscomp$iter$19$$.next(); !$$jscomp$key$action$$.done; $$jscomp$key$action$$ = $$jscomp$iter$19$$.next()) {
    $actions$$.push($$jscomp$key$action$$.value);
  }
  $marauroa$$.$me$.adminlevel && 600 <= $marauroa$$.$me$.adminlevel && ($actions$$.push({title:"(*) Inspect", action:function($entity$jscomp$5$$) {
    var $action$jscomp$7$$ = {type:"inspect"};
    $entity$jscomp$5$$.hasOwnProperty("id") && ($action$jscomp$7$$.target = "#" + $entity$jscomp$5$$.id);
    $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$7$$);
  }}), $actions$$.push({title:"(*) Destroy", action:function($entity$jscomp$6$$) {
    var $action$jscomp$8$$ = {type:"destroy",};
    void 0 !== $entity$jscomp$6$$.$_parent$ ? ($action$jscomp$8$$.baseobject = $marauroa$$.$me$.id, $action$jscomp$8$$.baseslot = $entity$jscomp$6$$.$_parent$.$_name$, $action$jscomp$8$$.baseitem = $entity$jscomp$6$$.id) : $action$jscomp$8$$.target = "#" + $entity$jscomp$6$$.id;
    $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$8$$);
  }}), $actions$$.push({title:"(*) Alter", action:function($entity$jscomp$7$$) {
    $JSCompiler_StaticMethods_setText$$($module$build$ts$ui$UI$default$$.$ui$.get(103), "/alter #" + $entity$jscomp$7$$.id + " ");
  }}));
  $JSCompiler_StaticMethods_gatherActions$self$$.actions = $actions$$;
}
;var $module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$.$DropQuantitySelectorDialog$ = void 0;
$module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$.$DropQuantitySelectorDialog$ = function($action$jscomp$9_allButton$$, $is_touch$$) {
  $is_touch$$ = void 0 === $is_touch$$ ? !1 : $is_touch$$;
  var $_a$jscomp$2$$;
  var $$jscomp$super$this$jscomp$6$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "dropquantityselectordialog-template") || this;
  $$jscomp$super$this$jscomp$6$$.action = $action$jscomp$9_allButton$$;
  null === ($_a$jscomp$2$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$6$$, ".quantityselectorbutton")) || void 0 === $_a$jscomp$2$$ ? void 0 : $_a$jscomp$2$$.addEventListener("click", function($event$jscomp$23$$) {
    $$jscomp$super$this$jscomp$6$$.$onDrop$($event$jscomp$23$$);
  });
  $is_touch$$ && ($action$jscomp$9_allButton$$ = document.createElement("button"), $action$jscomp$9_allButton$$.className = "allselectorbutton", $action$jscomp$9_allButton$$.innerText = "All", $$jscomp$super$this$jscomp$6$$.$componentElement$.appendChild($action$jscomp$9_allButton$$), $action$jscomp$9_allButton$$.addEventListener("click", function($event$jscomp$24$$) {
    $marauroa$$.$clientFramework$.$sendAction$($$jscomp$super$this$jscomp$6$$.action);
    $$jscomp$super$this$jscomp$6$$.$componentElement$.dispatchEvent(new Event("close"));
    $event$jscomp$24$$.preventDefault();
  }));
  var $valueInput$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$6$$, ".quantityselectorvalue");
  queueMicrotask(function() {
    $valueInput$$.select();
    $valueInput$$.focus();
  });
  $valueInput$$.addEventListener("keydown", function($event$jscomp$25$$) {
    "Enter" === $event$jscomp$25$$.key && $$jscomp$super$this$jscomp$6$$.$onDrop$($event$jscomp$25$$);
  });
  return $$jscomp$super$this$jscomp$6$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$.$DropQuantitySelectorDialog$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$.$DropQuantitySelectorDialog$.prototype.$onDrop$ = function($event$jscomp$26$$) {
  var $quantityStr$$ = $JSCompiler_StaticMethods_child$$(this, ".quantityselectorvalue").value;
  0 < parseInt($quantityStr$$) && (this.action.quantity = $quantityStr$$, $marauroa$$.$clientFramework$.$sendAction$(this.action));
  this.$componentElement$.dispatchEvent(new Event("close"));
  $event$jscomp$26$$.preventDefault();
};
var $module$build$ts$ui$component$ItemContainerImplementation$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$ItemContainerImplementation$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$ItemContainerImplementation$default$$.$ItemContainerImplementation$ = void 0;
$module$build$ts$ui$component$ItemContainerImplementation$default$$.$ItemContainerImplementation$ = function($parentElement$$, $slot$jscomp$3$$, $size$jscomp$28$$, $object$jscomp$6$$, $suffix$$, $quickPickup$$, $defaultImage$$) {
  this.parentElement = $parentElement$$;
  this.slot = $slot$jscomp$3$$;
  this.size = $size$jscomp$28$$;
  this.$A$ = $object$jscomp$6$$;
  this.$B$ = $suffix$$;
  this.$G$ = $quickPickup$$;
  this.$C$ = $defaultImage$$;
  this.$F$ = this.$timestampMouseDown$ = 0;
  this.$D$ = void 0;
  this.$v$ = !1;
  this.$init$($size$jscomp$28$$);
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$ui$component$ItemContainerImplementation$default$$.$ItemContainerImplementation$.prototype;
$JSCompiler_prototypeAlias$$.$init$ = function($size$jscomp$29$$) {
  var $$jscomp$this$jscomp$5$$ = this;
  this.size = $size$jscomp$29$$;
  for (var $i$jscomp$55$$ = 0; $i$jscomp$55$$ < $size$jscomp$29$$; $i$jscomp$55$$++) {
    var $e$jscomp$38$$ = this.parentElement.querySelector("#" + this.slot + this.$B$ + $i$jscomp$55$$);
    $e$jscomp$38$$.setAttribute("draggable", "true");
    $e$jscomp$38$$.addEventListener("dragstart", function($event$jscomp$28$$) {
      $$jscomp$this$jscomp$5$$.$onDragStart$($event$jscomp$28$$);
    });
    $e$jscomp$38$$.addEventListener("dragover", function($event$jscomp$29$$) {
      $$jscomp$this$jscomp$5$$.$onDragOver$($event$jscomp$29$$);
    });
    $e$jscomp$38$$.addEventListener("drop", function($event$jscomp$30$$) {
      $$jscomp$this$jscomp$5$$.$onDrop$($event$jscomp$30$$);
    });
    $e$jscomp$38$$.addEventListener("mousedown", function($event$jscomp$31$$) {
      $$jscomp$this$jscomp$5$$.$onMouseDown$($event$jscomp$31$$);
    });
    $e$jscomp$38$$.addEventListener("mouseup", function($event$jscomp$32$$) {
      $$jscomp$this$jscomp$5$$.$onMouseUp$($event$jscomp$32$$);
    });
    $e$jscomp$38$$.addEventListener("touchstart", function($event$jscomp$33$$) {
      $$jscomp$this$jscomp$5$$.$onTouchStart$($event$jscomp$33$$);
    });
    $e$jscomp$38$$.addEventListener("touchend", function($event$jscomp$34$$) {
      $$jscomp$this$jscomp$5$$.$onTouchEnd$($event$jscomp$34$$);
    });
    $e$jscomp$38$$.addEventListener("contextmenu", function($event$jscomp$35$$) {
      $event$jscomp$35$$.preventDefault();
    });
    $e$jscomp$38$$.addEventListener("mouseenter", function($event$jscomp$36$$) {
      $$jscomp$this$jscomp$5$$.$onMouseEnter$($event$jscomp$36$$);
    });
    $e$jscomp$38$$.addEventListener("mouseleave", function() {
    });
  }
  this.update();
};
$JSCompiler_prototypeAlias$$.$markDirty$ = function() {
  this.$v$ = !0;
};
$JSCompiler_prototypeAlias$$.update = function() {
  this.$render$();
};
$JSCompiler_prototypeAlias$$.$render$ = function() {
  var $i$90_myobject$$ = this.$A$ || $marauroa$$.$me$, $cnt_e$91$$ = 0;
  if ($i$90_myobject$$ && $i$90_myobject$$[this.slot]) {
    for (var $i$jscomp$56$$ = 0; $i$jscomp$56$$ < $i$90_myobject$$[this.slot].count(); $i$jscomp$56$$++) {
      var $o$jscomp$4$$ = $i$90_myobject$$[this.slot].$getByIndex$($i$jscomp$56$$), $e$jscomp$39$$ = this.parentElement.querySelector("#" + this.slot + this.$B$ + $cnt_e$91$$);
      if ($e$jscomp$39$$) {
        this.$v$ = this.$v$ || $o$jscomp$4$$ !== $e$jscomp$39$$.$dataItem$;
        var $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$ = $o$jscomp$4$$, $xOffset$$ = 0, $yOffset$$ = -32 * ($JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.state || 0);
        if ($stendhal$$.data.$sprites$.get($JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.$sprite$.filename).height) {
          null == $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.$animated$ && ($JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.$animated$ = 1 < $stendhal$$.data.$sprites$.get($JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.$sprite$.filename).width / 32);
          var $JSCompiler_inline_result$jscomp$205_JSCompiler_temp_const$jscomp$172$$ = $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.$animated$;
        } else {
          $JSCompiler_inline_result$jscomp$205_JSCompiler_temp_const$jscomp$172$$ = !1;
        }
        $JSCompiler_inline_result$jscomp$205_JSCompiler_temp_const$jscomp$172$$ && ($JSCompiler_StaticMethods_stepAnimation$$($JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$), $xOffset$$ = -(($JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.$sprite$.offsetX || 0) / 32 * 32));
        $JSCompiler_inline_result$jscomp$205_JSCompiler_temp_const$jscomp$172$$ = $e$jscomp$39$$.style;
        var $JSCompiler_inline_result$jscomp$173$$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/items/" + $o$jscomp$4$$["class"] + "/" + $o$jscomp$4$$.subclass + ".png").src;
        $JSCompiler_inline_result$jscomp$205_JSCompiler_temp_const$jscomp$172$$.backgroundImage = "url(" + $JSCompiler_inline_result$jscomp$173$$ + ")";
        $e$jscomp$39$$.style.backgroundPosition = $xOffset$$ + 1 + "px " + ($yOffset$$ + 1) + "px";
        $e$jscomp$39$$.textContent = $JSCompiler_StaticMethods_formatQuantity$$($o$jscomp$4$$);
        if (this.$v$) {
          $JSCompiler_StaticMethods_updateCursor$$($e$jscomp$39$$, $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$);
          if ("undefined" !== typeof $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$) {
            a: {
              if ("scroll" === $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$["class"] && $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.dest && ($JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$ = $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.dest.split(","), 2 < $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$.length)) {
                $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$ = $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$[0] + " " + $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$[1] + "," + $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$[2];
                break a;
              }
              $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$ = "";
            }
          } else {
            $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$ = "";
          }
          $e$jscomp$39$$.title = $JSCompiler_temp$jscomp$1003_dest$jscomp$inline_1022_item$jscomp$8$$;
        }
        $e$jscomp$39$$.$dataItem$ = $o$jscomp$4$$;
        $cnt_e$91$$++;
      }
    }
  }
  for ($i$90_myobject$$ = $cnt_e$91$$; $i$90_myobject$$ < this.size; $i$90_myobject$$++) {
    $cnt_e$91$$ = this.parentElement.querySelector("#" + this.slot + this.$B$ + $i$90_myobject$$), $cnt_e$91$$.style.backgroundImage = this.$C$ ? "url(" + $stendhal$$.$paths$.$gui$ + "/" + this.$C$ + ")" : "none", $cnt_e$91$$.textContent = "", this.$v$ && ($JSCompiler_StaticMethods_updateCursor$$($cnt_e$91$$), $cnt_e$91$$.title = ""), $cnt_e$91$$.$dataItem$ = void 0;
  }
  this.$v$ = !1;
};
$JSCompiler_prototypeAlias$$.$onDragStart$ = function($event$jscomp$38$$) {
  var $img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$ = this.$A$ || $marauroa$$.$me$;
  var $JSCompiler_inline_result$jscomp$185$$ = $stendhal$$.$ui$.$touch$.$touchEngaged$;
  if (!$img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$[this.slot] || $JSCompiler_inline_result$jscomp$185$$) {
    $event$jscomp$38$$.preventDefault();
  } else {
    var $target$jscomp$104$$;
    $event$jscomp$38$$ instanceof DragEvent ? $target$jscomp$104$$ = $event$jscomp$38$$.target : $target$jscomp$104$$ = ($event$jscomp$38$$.touches[0] || $event$jscomp$38$$.targetTouches[0] || $event$jscomp$38$$.changedTouches[0]).target;
    ($img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$ = $img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$[this.slot].$getByIndex$($target$jscomp$104$$.id.slice(this.slot.length + this.$B$.length))) ? ($stendhal$$.$ui$.$heldItem$ = {path:$JSCompiler_StaticMethods_getIdPath$$($img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$), $zone$:$marauroa$$.$currentZoneName$, slot:this.slot, $quantity$:$img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$.hasOwnProperty("quantity") ? $img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$.quantity : 
    1}, $img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$ = $JSCompiler_StaticMethods_getAreaOf$$($stendhal$$.data.$sprites$, $stendhal$$.data.$sprites$.get($img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$.$sprite$.filename), 32, 32), $event$jscomp$38$$ instanceof DragEvent && $event$jscomp$38$$.dataTransfer ? $event$jscomp$38$$.dataTransfer.setDragImage($img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$, 0, 0) : $JSCompiler_StaticMethods_isTouchEvent$$($event$jscomp$38$$) && ($stendhal$$.$ui$.$touch$.$held$ = 
    {image:$img$jscomp$2_item$jscomp$9_myobject$jscomp$1$$, offsetX:document.getElementById("gamewindow").offsetWidth - 32, offsetY:0})) : $event$jscomp$38$$.preventDefault();
  }
};
$JSCompiler_prototypeAlias$$.$onDragOver$ = function($event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$) {
  $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$.preventDefault();
  $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$ instanceof DragEvent && $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$.dataTransfer && ($event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$.dataTransfer.dropEffect = "move");
  $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$ = $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$.target.id;
  $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$.includes(".") && ($event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$ = $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$.split("."), $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$ = $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$[$event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$.length - 1], isNaN(parseInt($event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$, 10)) || (this.$D$ = $event$jscomp$39_id$jscomp$15_idx$jscomp$5_tmp$jscomp$4$$));
  return !1;
};
$JSCompiler_prototypeAlias$$.$onDrop$ = function($event$jscomp$40$$) {
  var $myobject$jscomp$2_quantity$jscomp$1_touch_held$$ = this.$A$ || $marauroa$$.$me$;
  if ($stendhal$$.$ui$.$heldItem$) {
    var $action$jscomp$10$$ = {source_path:$stendhal$$.$ui$.$heldItem$.path};
    $stendhal$$.$ui$.$heldItem$.slot === this.slot ? ($action$jscomp$10$$.type = "reorder", $action$jscomp$10$$.new_position = this.$D$ || "" + (this.size - 1)) : ($action$jscomp$10$$.type = "equip", $action$jscomp$10$$.target_path = "[" + $myobject$jscomp$2_quantity$jscomp$1_touch_held$$.id + "\t" + this.slot + "]", $action$jscomp$10$$.zone = $stendhal$$.$ui$.$heldItem$.$zone$);
    $myobject$jscomp$2_quantity$jscomp$1_touch_held$$ = $stendhal$$.$ui$.$heldItem$.$quantity$;
    $stendhal$$.$ui$.$heldItem$ = void 0;
    $myobject$jscomp$2_quantity$jscomp$1_touch_held$$ = $JSCompiler_StaticMethods_holdingItem$$() && 1 < $myobject$jscomp$2_quantity$jscomp$1_touch_held$$;
    if ($event$jscomp$40$$ instanceof DragEvent && $event$jscomp$40$$.ctrlKey || $myobject$jscomp$2_quantity$jscomp$1_touch_held$$) {
      var $pos$jscomp$1$$ = $JSCompiler_StaticMethods_extractPosition$$($event$jscomp$40$$);
      $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Quantity", new $module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$.$DropQuantitySelectorDialog$($action$jscomp$10$$, $myobject$jscomp$2_quantity$jscomp$1_touch_held$$), $pos$jscomp$1$$.pageX - 50, $pos$jscomp$1$$.pageY - 25);
    } else {
      $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$10$$);
    }
  }
  $event$jscomp$40$$.stopPropagation();
  $event$jscomp$40$$.preventDefault();
};
$JSCompiler_prototypeAlias$$.$isRightClick$ = function($event$jscomp$42$$) {
  return $event$jscomp$42$$.which ? 3 === $event$jscomp$42$$.which : 2 === $event$jscomp$42$$.button;
};
$JSCompiler_prototypeAlias$$.$onMouseDown$ = function() {
  this.$F$ = this.$timestampMouseDown$;
  this.$timestampMouseDown$ = +new Date();
};
$JSCompiler_prototypeAlias$$.$onMouseUp$ = function($evt$jscomp$33$$) {
  $evt$jscomp$33$$ instanceof MouseEvent && $evt$jscomp$33$$.preventDefault();
  var $event$jscomp$44$$ = $JSCompiler_StaticMethods_extractPosition$$($evt$jscomp$33$$);
  if ($event$jscomp$44$$.target.$dataItem$) {
    var $long_touch$$ = $JSCompiler_StaticMethods_isLongTouch$$($evt$jscomp$33$$), $append$jscomp$1_context_action$$ = $evt$jscomp$33$$ instanceof MouseEvent && this.$isRightClick$($evt$jscomp$33$$) || $long_touch$$;
    if (this.$G$ && !$append$jscomp$1_context_action$$) {
      $marauroa$$.$clientFramework$.$sendAction$({type:"equip", source_path:$JSCompiler_StaticMethods_getIdPath$$($event$jscomp$44$$.target.$dataItem$), target_path:"[" + $marauroa$$.$me$.id + "\tbag]", clicked:"", zone:$marauroa$$.$currentZoneName$});
      return;
    }
    if (this.$isRightClick$($event$jscomp$44$$) || $long_touch$$) {
      $append$jscomp$1_context_action$$ = [];
      if (window.TouchEvent && $evt$jscomp$33$$ instanceof TouchEvent && $long_touch$$) {
        var $tmp$jscomp$5$$ = this;
        $append$jscomp$1_context_action$$.push({title:"Hold", action:function() {
          $tmp$jscomp$5$$.$onDragStart$($evt$jscomp$33$$);
        }});
      }
      $stendhal$$.$ui$.$actionContextMenu$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Action", new $module$build$ts$ui$dialog$ActionContextMenu$default$$.$ActionContextMenu$($event$jscomp$44$$.target.$dataItem$, $append$jscomp$1_context_action$$), $event$jscomp$44$$.pageX - 50, $event$jscomp$44$$.pageY - 5));
    } else {
      $stendhal$$.$ui$.$heldItem$ || (!$JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "action.item.doubleclick") || 300 >= this.$timestampMouseDown$ - this.$F$) && $marauroa$$.$clientFramework$.$sendAction$({type:"use", target_path:$JSCompiler_StaticMethods_getIdPath$$($event$jscomp$44$$.target.$dataItem$), zone:$marauroa$$.$currentZoneName$});
    }
  }
  $JSCompiler_StaticMethods_unsetHeldItem$$();
  document.getElementById("gamewindow").focus();
};
$JSCompiler_prototypeAlias$$.$onMouseEnter$ = function() {
};
$JSCompiler_prototypeAlias$$.$onTouchStart$ = function($evt$jscomp$36_pos$jscomp$2$$) {
  $evt$jscomp$36_pos$jscomp$2$$ = $JSCompiler_StaticMethods_extractPosition$$($evt$jscomp$36_pos$jscomp$2$$);
  $stendhal$$.$ui$.$touch$.$onTouchStart$($evt$jscomp$36_pos$jscomp$2$$.pageX, $evt$jscomp$36_pos$jscomp$2$$.pageY);
};
$JSCompiler_prototypeAlias$$.$onTouchEnd$ = function($evt$jscomp$37$$) {
  $stendhal$$.$ui$.$touch$.$onTouchEnd$();
  $JSCompiler_StaticMethods_isLongTouch$$($evt$jscomp$37$$) && !$stendhal$$.$ui$.$touch$.$held$ ? this.$onMouseUp$($evt$jscomp$37$$) : $stendhal$$.$ui$.$touch$.$held$ && ($evt$jscomp$37$$.preventDefault(), this.$onDrop$($evt$jscomp$37$$), $JSCompiler_StaticMethods_unsetHeldItem$$());
  $JSCompiler_StaticMethods_unsetOrigin$$();
};
function $JSCompiler_StaticMethods_updateCursor$$($target$jscomp$105$$, $item$jscomp$10$$) {
  $item$jscomp$10$$ ? $target$jscomp$105$$.style.cursor = $item$jscomp$10$$.$getCursor$(0, 0) : $target$jscomp$105$$.style.cursor = "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/normal.png) 1 3, auto";
}
;var $module$build$ts$ui$component$ItemInventoryComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$ItemInventoryComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$ = void 0;
$module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$ = function($object$jscomp$7$$, $slot$jscomp$4$$, $sizeX$$, $sizeY$$, $quickPickup$jscomp$1$$, $defaultImage$jscomp$1$$) {
  var $$jscomp$super$this$jscomp$7$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "iteminventory-template", !0) || this;
  $$jscomp$super$this$jscomp$7$$.slot = $slot$jscomp$4$$;
  $$jscomp$super$this$jscomp$7$$.$D$ = 0;
  $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.counter++;
  $$jscomp$super$this$jscomp$7$$.$B$ = "-" + $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.counter + "-";
  $$jscomp$super$this$jscomp$7$$.$componentElement$.classList.add("inventorypopup_" + $sizeX$$);
  $quickPickup$jscomp$1$$ && $$jscomp$super$this$jscomp$7$$.$componentElement$.classList.add("quickPickup");
  $$jscomp$super$this$jscomp$7$$.$A$($sizeX$$, $sizeY$$);
  $$jscomp$super$this$jscomp$7$$.$v$ = new $module$build$ts$ui$component$ItemContainerImplementation$default$$.$ItemContainerImplementation$($$jscomp$super$this$jscomp$7$$.$componentElement$, $slot$jscomp$4$$, $sizeX$$ * $sizeY$$, $object$jscomp$7$$, $$jscomp$super$this$jscomp$7$$.$B$, $quickPickup$jscomp$1$$, $defaultImage$jscomp$1$$);
  $stendhal$$.$ui$.$equip$.add($$jscomp$super$this$jscomp$7$$.$v$);
  return $$jscomp$super$this$jscomp$7$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.$A$ = function($sizeX$jscomp$1$$, $sizeY$jscomp$1$$) {
  this.$componentElement$.classList.remove("inventorypopup_" + this.$D$);
  this.$componentElement$.classList.add("inventorypopup_" + $sizeX$jscomp$1$$);
  this.$D$ = $sizeX$jscomp$1$$;
  for (var $html$jscomp$3$$ = "", $i$jscomp$57$$ = 0; $i$jscomp$57$$ < $sizeX$jscomp$1$$ * $sizeY$jscomp$1$$; $i$jscomp$57$$++) {
    $html$jscomp$3$$ += "<div id='" + this.slot + this.$B$ + $i$jscomp$57$$ + "' class='itemSlot'></div>";
  }
  this.$componentElement$.innerHTML = $html$jscomp$3$$;
};
$module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.update = function() {
  this.$v$.update();
};
$module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.$markDirty$ = function() {
  this.$v$.$markDirty$();
};
$module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.$onParentClose$ = function() {
  var $idx$jscomp$6$$ = $stendhal$$.$ui$.$equip$.indexOf(this.$v$);
  console.log($stendhal$$.$ui$.$equip$.$getInventory$(), $idx$jscomp$6$$);
  if (0 > $idx$jscomp$6$$) {
    console.log("Cannot cleanup unknown itemContainerImplementation");
  } else {
    var $JSCompiler_StaticMethods_removeIndex$self$jscomp$inline_359$$ = $stendhal$$.$ui$.$equip$;
    -1 < $idx$jscomp$6$$ && $idx$jscomp$6$$ < $JSCompiler_StaticMethods_removeIndex$self$jscomp$inline_359$$.$v$.length && $JSCompiler_StaticMethods_removeIndex$self$jscomp$inline_359$$.$v$.splice($idx$jscomp$6$$, 1);
  }
};
$module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.counter = 0;
var $module$build$ts$ui$dialog$TradeDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$TradeDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$ = void 0;
$module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$ = function() {
  var $$jscomp$super$this$jscomp$8$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "tradedialog-template") || this;
  $JSCompiler_StaticMethods_registerComponent$$($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$TradeDialog$, $$jscomp$super$this$jscomp$8$$);
  $$jscomp$super$this$jscomp$8$$.refresh();
  $$jscomp$super$this$jscomp$8$$.$A$ = new $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$($module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$.$v$, "trade", 2, 2, !1, void 0);
  $$jscomp$super$this$jscomp$8$$.$v$ = new $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$($module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$.$v$, "trade", 2, 2, !1, void 0);
  $JSCompiler_StaticMethods_JSC$1844_createHtml$$($$jscomp$super$this$jscomp$8$$);
  return $$jscomp$super$this$jscomp$8$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
function $JSCompiler_StaticMethods_showState$$($element$jscomp$10$$, $state$$) {
  $element$jscomp$10$$.className = "trade-" + $state$$;
  switch($state$$) {
    case "NO_ACTIVE_TRADE":
      $element$jscomp$10$$.innerText = "Inactive";
      break;
    case "MAKING_OFFERS":
      $element$jscomp$10$$.innerText = "Changing";
      break;
    case "LOCKED":
      $element$jscomp$10$$.innerText = "Offered";
      break;
    case "DEAL_WAITING_FOR_OTHER_DEAL":
      $element$jscomp$10$$.innerText = "ACCEPTED";
  }
}
function $JSCompiler_StaticMethods_JSC$1844_createHtml$$($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$) {
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$, "#trade-partner-items").append($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$.$A$.$componentElement$);
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$, "#trade-my-items").append($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$.$v$.$componentElement$);
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$, "#trade-accept").addEventListener("click", function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"trade", action:"deal", zone:$marauroa$$.$currentZoneName$});
  });
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$, "#trade-offer").addEventListener("click", function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"trade", action:"lock", zone:$marauroa$$.$currentZoneName$});
  });
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_JSC$1844_createHtml$self$$, "#trade-cancel").addEventListener("click", function() {
    $JSCompiler_StaticMethods_JSC$1844_createHtml$self$$.$componentElement$.dispatchEvent(new Event("close"));
  });
}
$module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$.prototype.$onParentClose$ = function() {
  this.$A$.$onParentClose$();
  this.$v$.$onParentClose$();
  $JSCompiler_StaticMethods_unregisterComponent$$(this);
  $marauroa$$.$clientFramework$.$sendAction$({type:"trade", action:"cancel", zone:$marauroa$$.$currentZoneName$});
};
$module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$.$v$ = {};
var $module$build$ts$event$TradeEvent$default$$ = {};
Object.defineProperty($module$build$ts$event$TradeEvent$default$$, "__esModule", {value:!0});
$module$build$ts$event$TradeEvent$default$$.$TradeEvent$ = void 0;
$module$build$ts$event$TradeEvent$default$$.$TradeEvent$ = function() {
  return $module$build$ts$event$RPEvent$default$$.$RPEvent$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$event$TradeEvent$default$$.$TradeEvent$, $module$build$ts$event$RPEvent$default$$.$RPEvent$);
$module$build$ts$event$TradeEvent$default$$.$TradeEvent$.prototype.$execute$ = function() {
  var $JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$ = $module$build$ts$ui$UI$default$$.$ui$.get($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$TradeDialog$);
  if ("NO_ACTIVE_TRADE" !== this.user_trade_state && !$JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$) {
    var $dstate$jscomp$1_partner$jscomp$inline_371$$ = $JSCompiler_StaticMethods_getWindowState$$("trade");
    $JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$ = new $module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$();
    (new $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$("Trade", $JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$, $dstate$jscomp$1_partner$jscomp$inline_371$$.x, $dstate$jscomp$1_partner$jscomp$inline_371$$.y)).$windowId$ = "trade";
  }
  if ($JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$) {
    $dstate$jscomp$1_partner$jscomp$inline_371$$ = $marauroa$$.$currentZone$[this.partner_id];
    var $myState$jscomp$inline_372$$ = this.user_trade_state, $partnerState$jscomp$inline_373$$ = this.partner_trade_state, $me$jscomp$inline_374$$ = $marauroa$$.$me$;
    $dstate$jscomp$1_partner$jscomp$inline_371$$ && ($JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$, "#trade-partner-name").innerText = $dstate$jscomp$1_partner$jscomp$inline_371$$._name);
    $JSCompiler_StaticMethods_showState$$($JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$, "#trade-partner-status"), $partnerState$jscomp$inline_373$$);
    $JSCompiler_StaticMethods_showState$$($JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$, "#trade-my-status"), $myState$jscomp$inline_372$$);
    $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$, "#trade-offer").disabled = "MAKING_OFFERS" !== $myState$jscomp$inline_372$$;
    $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$, "#trade-accept").disabled = !("LOCKED" === $myState$jscomp$inline_372$$ && ("LOCKED" === $partnerState$jscomp$inline_373$$ || "DEAL_WAITING_FOR_OTHER_DEAL" === $partnerState$jscomp$inline_373$$));
    $JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$.$A$.$v$.$A$ = $dstate$jscomp$1_partner$jscomp$inline_371$$ ? $dstate$jscomp$1_partner$jscomp$inline_371$$ : $module$build$ts$ui$dialog$TradeDialog$default$$.$TradeDialog$.$v$;
    $JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$.$v$.$v$.$A$ = $me$jscomp$inline_374$$;
    "TRADE_COMPLETED" === $myState$jscomp$inline_372$$ && $JSCompiler_StaticMethods_updateTradeState$self$jscomp$inline_370_dialog$$.$componentElement$.dispatchEvent(new Event("close"));
  }
};
var $module$build$ts$util$SoundId$default$$ = {};
Object.defineProperty($module$build$ts$util$SoundId$default$$, "__esModule", {value:!0});
$module$build$ts$util$SoundId$default$$.$SoundId$ = void 0;
$module$build$ts$util$SoundId$default$$.$SoundId$ = {level_up:"player/tadaa", level_down:void 0, stat_up:"player/stat_up-01", stat_down:void 0, achievement:"player/yay", commerce:"coins-01", commerce2:"cha-ching", heal:"heal-01"};
var $module$build$ts$EventRegistry$default$$ = {};
Object.defineProperty($module$build$ts$EventRegistry$default$$, "__esModule", {value:!0});
$module$build$ts$EventRegistry$default$$.$EventRegistry$ = void 0;
$module$build$ts$EventRegistry$default$$.$EventRegistry$ = function() {
  this.$v$ = !1;
};
$module$build$ts$EventRegistry$default$$.$EventRegistry$.get = function() {
  $module$build$ts$EventRegistry$default$$.$EventRegistry$.$v$ || ($module$build$ts$EventRegistry$default$$.$EventRegistry$.$v$ = new $module$build$ts$EventRegistry$default$$.$EventRegistry$());
  return $module$build$ts$EventRegistry$default$$.$EventRegistry$.$v$;
};
$module$build$ts$EventRegistry$default$$.$EventRegistry$.prototype.$init$ = function() {
  this.$v$ ? console.warn("tried to re-initialize EventRegistry") : (this.$v$ = !0, this.register("chat_options", new $module$build$ts$event$ChatOptionsEvent$default$$.$ChatOptionsEvent$()), this.register("examine", new $module$build$ts$event$ExamineEvent$default$$.$ExamineEvent$()), this.register("group_change_event", new $module$build$ts$event$GroupChangeEvent$default$$.$GroupChangeEvent$()), this.register("group_invite_event", new $module$build$ts$event$GroupInviteEvent$default$$.$GroupInviteEvent$()), 
  this.register("progress_status_event", new $module$build$ts$event$ProgressStatusEvent$default$$.$ProgressStatusEvent$()), this.register("trade_state_change_event", new $module$build$ts$event$TradeEvent$default$$.$TradeEvent$()), this.register("attack", {$execute$:function($entity$jscomp$10$$) {
    var $target$jscomp$107_weapon$jscomp$inline_387$$ = $JSCompiler_StaticMethods_getAttackTarget$$($entity$jscomp$10$$);
    if ($target$jscomp$107_weapon$jscomp$inline_387$$) {
      if (this.hasOwnProperty("hit")) {
        if (0 !== parseInt(this.damage, 10)) {
          $target$jscomp$107_weapon$jscomp$inline_387$$.$attackResult$ = $JSCompiler_StaticMethods_createResultIcon$$($stendhal$$.$paths$.$sprites$ + "/combat/hitted.png");
          var $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$ = "attack-melee-01 attack-melee-02 attack-melee-03 attack-melee-04 attack-melee-05 attack-melee-06 attack-melee-07".split(" ");
          $JSCompiler_StaticMethods_playLocalizedEffect$$($module$build$ts$entity$RPEntity$default$$.$RPEntity$.$v$, $target$jscomp$107_weapon$jscomp$inline_387$$._x, $target$jscomp$107_weapon$jscomp$inline_387$$._y, 20, 3, $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$[Math.floor(Math.random() * Math.floor($JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$.length))], 
          1);
        } else {
          $target$jscomp$107_weapon$jscomp$inline_387$$.$attackResult$ = $JSCompiler_StaticMethods_createResultIcon$$($stendhal$$.$paths$.$sprites$ + "/combat/blocked.png"), $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$ = ["clang-metallic-1", "clang-dull-1"], $JSCompiler_StaticMethods_playLocalizedEffect$$($module$build$ts$entity$RPEntity$default$$.$RPEntity$.$v$, $target$jscomp$107_weapon$jscomp$inline_387$$._x, 
          $target$jscomp$107_weapon$jscomp$inline_387$$._y, 20, 3, $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$[Math.floor(Math.random() * Math.floor($JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$.length))], 1);
        }
      } else {
        $target$jscomp$107_weapon$jscomp$inline_387$$.$attackResult$ = $JSCompiler_StaticMethods_createResultIcon$$($stendhal$$.$paths$.$sprites$ + "/combat/missed.png");
      }
      $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$ = parseInt(this.type, 10);
      var $path$jscomp$inline_1032_ranged$jscomp$inline_386$$ = this.hasOwnProperty("ranged");
      $target$jscomp$107_weapon$jscomp$inline_387$$ = this.weapon;
      $path$jscomp$inline_1032_ranged$jscomp$inline_386$$ || "ranged" !== $target$jscomp$107_weapon$jscomp$inline_387$$ || ($target$jscomp$107_weapon$jscomp$inline_387$$ = void 0);
      $path$jscomp$inline_1032_ranged$jscomp$inline_386$$ ? $entity$jscomp$10$$.$attackSprite$ = new $module$build$ts$sprite$action$RangedAttackSprite$default$$.$RangedAttackSprite$($entity$jscomp$10$$, $JSCompiler_StaticMethods_getAttackTarget$$($entity$jscomp$10$$), $module$build$ts$util$Nature$default$$.$Nature$.$v$[$JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$].color, 
      $target$jscomp$107_weapon$jscomp$inline_387$$) : ("undefined" === typeof $target$jscomp$107_weapon$jscomp$inline_387$$ && ($target$jscomp$107_weapon$jscomp$inline_387$$ = "blade_strike"), "blade_strike" === $target$jscomp$107_weapon$jscomp$inline_387$$ && 0 == $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$ && ($target$jscomp$107_weapon$jscomp$inline_387$$ += "_cut"), 
      $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$ = $module$build$ts$util$Nature$default$$.$Nature$.$v$[$JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$], $path$jscomp$inline_1032_ranged$jscomp$inline_386$$ = $stendhal$$.$paths$.$sprites$ + 
      "/combat/" + $target$jscomp$107_weapon$jscomp$inline_387$$, "undefined" !== typeof $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$.$v$ && ($path$jscomp$inline_1032_ranged$jscomp$inline_386$$ += "_" + $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$.$v$), 
      $JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$ = $path$jscomp$inline_1032_ranged$jscomp$inline_386$$ + ".png", $target$jscomp$107_weapon$jscomp$inline_387$$.startsWith("blade_strike") ? $entity$jscomp$10$$.$attackSprite$ = new $module$build$ts$sprite$action$BarehandAttackSprite$default$$.$BarehandAttackSprite$($entity$jscomp$10$$, $stendhal$$.data.$sprites$.get($JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$)) : 
      $entity$jscomp$10$$.$attackSprite$ = new $module$build$ts$sprite$action$MeleeAttackSprite$default$$.$MeleeAttackSprite$($entity$jscomp$10$$, $JSCompiler_StaticMethods_getRotated$$($JSCompiler_StaticMethods_getWeaponPath$self$jscomp$inline_1030_JSCompiler_inline_result$jscomp$1001_nature$jscomp$inline_385_sounds$jscomp$inline_377_sounds$jscomp$inline_380$$, 90 * ($entity$jscomp$10$$.dir - 1))));
    }
  }}), this.register("bestiary", {$execute$:function() {
    if (this.hasOwnProperty("enemies")) {
      var $header$jscomp$2$$ = ["Bestiary:", '"???" = unknown'], $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$ = this.enemies.includes("(rare)"), $hasAbnormal_layout$$ = this.enemies.includes("(abnormal)");
      if ($build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$ || $hasAbnormal_layout$$) {
        var $col1_subheader$$ = "";
        $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$ ? ($col1_subheader$$ += '"rare"', $hasAbnormal_layout$$ && ($col1_subheader$$ += ' and "abnormal"')) : $col1_subheader$$ += '"abnormal"';
        $header$jscomp$2$$[1] = $col1_subheader$$ + " creatures not required for achievements";
      }
      $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$ = function() {
        return $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.apply(this, arguments) || this;
      };
      $$jscomp$inherits$$($build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
      $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$ = new $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$("empty-div-template");
      $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$.$cid$ = "bestiary";
      $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$.$componentElement$.classList.add("bestiary");
      $hasAbnormal_layout$$ = document.createElement("div");
      $hasAbnormal_layout$$.className = "horizontalgroup stretchgroup";
      $col1_subheader$$ = document.createElement("div");
      var $col2$$ = document.createElement("div"), $col3$$ = document.createElement("div");
      $col1_subheader$$.className = "verticalgroup stretchgroup";
      $col2$$.className = "verticalgroup stretchgroup";
      $col3$$.className = "verticalgroup stretchgroup";
      var $$jscomp$iter$20_t1$$ = document.createElement("div"), $$jscomp$key$e_info_t2$$ = document.createElement("div"), $solo_t3$$ = document.createElement("div");
      $$jscomp$iter$20_t1$$.classList.add("shopcol");
      $$jscomp$key$e_info_t2$$.classList.add("shopcol");
      $solo_t3$$.classList.add("shopcol");
      $$jscomp$iter$20_t1$$.textContent = "Name";
      $$jscomp$key$e_info_t2$$.textContent = "Solo";
      $solo_t3$$.textContent = "Shared";
      $col1_subheader$$.appendChild($$jscomp$iter$20_t1$$);
      $col2$$.appendChild($$jscomp$key$e_info_t2$$);
      $col3$$.appendChild($solo_t3$$);
      $$jscomp$iter$20_t1$$ = $$jscomp$makeIterator$$(this.enemies.split(";"));
      for ($$jscomp$key$e_info_t2$$ = $$jscomp$iter$20_t1$$.next(); !$$jscomp$key$e_info_t2$$.done; $$jscomp$key$e_info_t2$$ = $$jscomp$iter$20_t1$$.next()) {
        $$jscomp$key$e_info_t2$$ = $$jscomp$key$e_info_t2$$.value.split(",");
        var $shared$$ = $solo_t3$$ = "-";
        "true" == $$jscomp$key$e_info_t2$$[1] && ($solo_t3$$ = "\u2714");
        "true" == $$jscomp$key$e_info_t2$$[2] && ($shared$$ = "\u2714");
        var $l1$$ = document.createElement("div"), $l2$$ = document.createElement("div"), $l3$$ = document.createElement("div");
        $l1$$.classList.add("shopcol");
        $l2$$.classList.add("shopcol");
        $l3$$.classList.add("shopcol");
        $l1$$.textContent = $$jscomp$key$e_info_t2$$[0];
        $l2$$.textContent = $solo_t3$$;
        $l3$$.textContent = $shared$$;
        $col1_subheader$$.appendChild($l1$$);
        $col2$$.appendChild($l2$$);
        $col3$$.appendChild($l3$$);
      }
      $hasAbnormal_layout$$.appendChild($col1_subheader$$);
      $hasAbnormal_layout$$.appendChild($col2$$);
      $hasAbnormal_layout$$.appendChild($col3$$);
      $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$.$componentElement$.appendChild($hasAbnormal_layout$$);
      $stendhal$$.$ui$.$globalInternalWindow$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$($header$jscomp$2$$.join(" "), $build$ts$EventRegistry$classdecl$var0_content$jscomp$2_hasRare$$, 20, 20));
    } else {
      console.log('ERROR: event does not have "enemies" attribute');
    }
  }}), this.register("global_visual_effect", {$execute$:function() {
  }}), this.register("image_event", {$execute$:function($rpobject$jscomp$3$$) {
    console.log("image_event", this, $rpobject$jscomp$3$$);
  }}), this.register("player_logged_on", {$execute$:function() {
  }}), this.register("player_logged_out", {$execute$:function() {
  }}), this.register("private_text", {$soundTextEvents$:{privmsg:!0, support:!0, tutorial:!0}, $execute$:function() {
    var $ttype$$ = this.texttype.toLowerCase(), $msg$jscomp$17_notif$$ = this.text.replace(/\\r\\n/g, "\n").replace(/\\r/g, "\n"), $profile$jscomp$2$$;
    this.hasOwnProperty("profile") ? $profile$jscomp$2$$ = this.profile : "tutorial" === $ttype$$ && ($profile$jscomp$2$$ = "floattingladynpc");
    "server" === $ttype$$ && $msg$jscomp$17_notif$$.includes("\n") ? $module$build$ts$util$Chat$default$$.$Chat$.log($ttype$$, $msg$jscomp$17_notif$$.split("\n"), void 0, $profile$jscomp$2$$) : $module$build$ts$util$Chat$default$$.$Chat$.log($ttype$$, $msg$jscomp$17_notif$$, void 0, $profile$jscomp$2$$, "scene_setting" !== $ttype$$);
    ($msg$jscomp$17_notif$$ = $stendhal$$.$config$.get("event.pvtmsg.sound")) && this.$soundTextEvents$[$ttype$$] && $JSCompiler_StaticMethods_playGlobalizedEffect$$($stendhal$$.$ui$.$soundMan$, $msg$jscomp$17_notif$$);
  }}), this.register("reached_achievement", {$execute$:function() {
    var $JSCompiler_StaticMethods_addAchievementNotif$self$jscomp$inline_396$$ = $stendhal$$.$ui$.$gamewindow$, $banner$jscomp$inline_397$$ = new $module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$(this.category, this.title, this.description);
    $JSCompiler_StaticMethods_addAchievementNotif$self$jscomp$inline_396$$.$v$.push($banner$jscomp$inline_397$$);
    $banner$jscomp$inline_397$$.$onAdded$($JSCompiler_StaticMethods_addAchievementNotif$self$jscomp$inline_396$$.$ctx$);
  }}), this.register("show_item_list", {$execute$:function() {
    function $build$ts$EventRegistry$classdecl$var1$$() {
      return $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.apply(this, arguments) || this;
    }
    var $title$jscomp$15$$ = "Items", $caption$jscomp$1_itemList$$ = "", $$jscomp$iter$21_items$jscomp$3$$ = [];
    this.hasOwnProperty("title") && ($title$jscomp$15$$ = this.title);
    this.hasOwnProperty("caption") && ($caption$jscomp$1_itemList$$ = this.caption);
    if (this.hasOwnProperty("content")) {
      for (var $content$jscomp$3_obj$jscomp$39$$ in this.content) {
        if (this.content.hasOwnProperty($content$jscomp$3_obj$jscomp$39$$)) {
          var $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$ = this.content[$content$jscomp$3_obj$jscomp$39$$].a;
          $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$ = {$clazz$:$$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$["class"], $subclass$:$$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.subclass, $img$:$$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$["class"] + "/" + $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.subclass + ".png", $price$:$$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.price, $desc$:$$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.description_info};
          $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.$price$.startsWith("-") && ($$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.$price$ = $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.$price$.substr(1));
          $$jscomp$iter$21_items$jscomp$3$$.push($$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$);
        }
      }
    }
    $$jscomp$inherits$$($build$ts$EventRegistry$classdecl$var1$$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
    $content$jscomp$3_obj$jscomp$39$$ = new $build$ts$EventRegistry$classdecl$var1$$("empty-div-template");
    $content$jscomp$3_obj$jscomp$39$$.$componentElement$.classList.add("shopsign");
    $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$ = document.createElement("div");
    $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.className = "horizontalgroup shopcaption";
    $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.textContent = $caption$jscomp$1_itemList$$ + "\nItem\t-\tPrice\t-\tDescription";
    $content$jscomp$3_obj$jscomp$39$$.$componentElement$.appendChild($$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$);
    $caption$jscomp$1_itemList$$ = document.createElement("div");
    $caption$jscomp$1_itemList$$.className = "shoplist";
    $content$jscomp$3_obj$jscomp$39$$.$componentElement$.appendChild($caption$jscomp$1_itemList$$);
    $$jscomp$iter$21_items$jscomp$3$$ = $$jscomp$makeIterator$$($$jscomp$iter$21_items$jscomp$3$$);
    for ($$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$ = $$jscomp$iter$21_items$jscomp$3$$.next(); !$$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.done; $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$ = $$jscomp$iter$21_items$jscomp$3$$.next()) {
      $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$ = $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.value;
      var $row$$ = document.createElement("div");
      $row$$.className = "horizontalgroup shoprow";
      var $desc_img$jscomp$3_price$$ = document.createElement("div");
      $desc_img$jscomp$3_price$$.className = "shopcol";
      $desc_img$jscomp$3_price$$.appendChild($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/items/" + $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.$img$));
      $row$$.appendChild($desc_img$jscomp$3_price$$);
      $desc_img$jscomp$3_price$$ = document.createElement("div");
      $desc_img$jscomp$3_price$$.className = "shopcol";
      $desc_img$jscomp$3_price$$.textContent = $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.$price$;
      $row$$.appendChild($desc_img$jscomp$3_price$$);
      $desc_img$jscomp$3_price$$ = document.createElement("div");
      $desc_img$jscomp$3_price$$.className = "shopcol shopcolr";
      $desc_img$jscomp$3_price$$.textContent = $$jscomp$key$i_captionElement_data$jscomp$81_i$92_i$jscomp$58$$.$desc$;
      $row$$.appendChild($desc_img$jscomp$3_price$$);
      $caption$jscomp$1_itemList$$.appendChild($row$$);
    }
    $stendhal$$.$ui$.$globalInternalWindow$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$($title$jscomp$15$$, $content$jscomp$3_obj$jscomp$39$$, 20, 20));
  }}), this.register("show_outfit_list", {$execute$:function() {
    function $build$ts$EventRegistry$classdecl$var2$$() {
      return $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.apply(this, arguments) || this;
    }
    var $title$jscomp$16$$ = "Outfits", $caption$jscomp$2_itemList$jscomp$1$$ = "", $$jscomp$iter$23_outfits$$ = [];
    this.hasOwnProperty("title") && ($title$jscomp$16$$ = this.title);
    this.hasOwnProperty("caption") && ($caption$jscomp$2_itemList$jscomp$1$$ = this.caption);
    if (this.hasOwnProperty("outfits")) {
      for (var $$jscomp$iter$22_content$jscomp$4$$ = $$jscomp$makeIterator$$(this.outfits.split(":")), $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = $$jscomp$iter$22_content$jscomp$4$$.next(); !$$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.done; $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = $$jscomp$iter$22_content$jscomp$4$$.next()) {
        $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.value, $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.split(";"), 2 < $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.length && $$jscomp$iter$23_outfits$$.push([$$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$[0], $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$[1], $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$[2]]);
      }
    }
    this.hasOwnProperty("show_base");
    $$jscomp$inherits$$($build$ts$EventRegistry$classdecl$var2$$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
    $$jscomp$iter$22_content$jscomp$4$$ = new $build$ts$EventRegistry$classdecl$var2$$("empty-div-template");
    $$jscomp$iter$22_content$jscomp$4$$.$componentElement$.classList.add("shopsign");
    $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = document.createElement("div");
    $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.className = "horizontalgroup shopcaption";
    $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.textContent = $caption$jscomp$2_itemList$jscomp$1$$;
    $$jscomp$iter$22_content$jscomp$4$$.$componentElement$.appendChild($$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$);
    $caption$jscomp$2_itemList$jscomp$1$$ = document.createElement("div");
    $caption$jscomp$2_itemList$jscomp$1$$.className = "shoplist";
    $$jscomp$iter$22_content$jscomp$4$$.$componentElement$.appendChild($caption$jscomp$2_itemList$jscomp$1$$);
    $$jscomp$iter$23_outfits$$ = $$jscomp$makeIterator$$($$jscomp$iter$23_outfits$$);
    for ($$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = $$jscomp$iter$23_outfits$$.next(); !$$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.done; $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = $$jscomp$iter$23_outfits$$.next()) {
      $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$ = $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$.value;
      var $row$jscomp$1$$ = document.createElement("div");
      $row$jscomp$1$$.className = "horizontalgroup shoprow";
      $row$jscomp$1$$.textContent = $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$[0] + ": " + $$jscomp$key$o_captionElement$jscomp$1_o$93_o$jscomp$5$$[2];
      $caption$jscomp$2_itemList$jscomp$1$$.appendChild($row$jscomp$1$$);
    }
    $stendhal$$.$ui$.$globalInternalWindow$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$($title$jscomp$16$$, $$jscomp$iter$22_content$jscomp$4$$, 20, 20));
  }}), this.register("sound_event", {$execute$:function($rpobject$jscomp$10$$) {
    var $volume$jscomp$7$$ = 1;
    this.hasOwnProperty("volume") && ($volume$jscomp$7$$ *= parseInt(this.volume, 10) / 100);
    var $radius$jscomp$10$$ = parseInt(this.radius, 10), $sound$jscomp$2$$ = this.sound, $sound_id$$ = this.sound_id;
    $sound_id$$ && ($sound$jscomp$2$$ = $module$build$ts$util$SoundId$default$$.$SoundId$[$sound_id$$]);
    $JSCompiler_StaticMethods_playLocalizedEffect$$($stendhal$$.$ui$.$soundMan$, $rpobject$jscomp$10$$._x, $rpobject$jscomp$10$$._y, $radius$jscomp$10$$, this.layer, $sound$jscomp$2$$, $volume$jscomp$7$$);
  }}), this.register("text", {$execute$:function($rpobject$jscomp$11$$) {
    this.hasOwnProperty("range") ? $rpobject$jscomp$11$$.$say$(this.text, this.range) : $rpobject$jscomp$11$$.$say$(this.text);
  }}), this.register("transition_graph", {$execute$:function() {
  }}), this.register("view_change", {$execute$:function() {
  }}));
};
$module$build$ts$EventRegistry$default$$.$EventRegistry$.prototype.register = function($_type$$, $_event$$) {
  $marauroa$$.$rpeventFactory$[$_type$$] = $_event$$ instanceof $module$build$ts$event$RPEvent$default$$.$RPEvent$ ? $_event$$ : $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, $_event$$);
};
var $module$build$ts$action$SlashAction$default$$ = {};
Object.defineProperty($module$build$ts$action$SlashAction$default$$, "__esModule", {value:!0});
$module$build$ts$action$SlashAction$default$$.$SlashAction$ = void 0;
$module$build$ts$action$SlashAction$default$$.$SlashAction$ = function() {
};
var $module$build$ts$ui$component$ShowFloatingWindowComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$ShowFloatingWindowComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$ShowFloatingWindowComponent$default$$.$ShowFloatingWindowComponent$ = void 0;
$module$build$ts$ui$component$ShowFloatingWindowComponent$default$$.$ShowFloatingWindowComponent$ = function($uiComponentEnum$$, $title$jscomp$17$$, $x$jscomp$104$$, $y$jscomp$84$$) {
  var $$jscomp$super$this$jscomp$9$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "showfloatingwindow-template") || this;
  $$jscomp$super$this$jscomp$9$$.$v$ = $uiComponentEnum$$;
  $$jscomp$super$this$jscomp$9$$.title = $title$jscomp$17$$;
  $$jscomp$super$this$jscomp$9$$.x = $x$jscomp$104$$;
  $$jscomp$super$this$jscomp$9$$.y = $y$jscomp$84$$;
  $$jscomp$super$this$jscomp$9$$.$componentElement$.innerText = $title$jscomp$17$$;
  $$jscomp$super$this$jscomp$9$$.$componentElement$.addEventListener("click", function() {
    var $_a$jscomp$inline_400$$, $_b$jscomp$inline_401$$, $component$jscomp$inline_402$$ = $module$build$ts$ui$UI$default$$.$ui$.get($$jscomp$super$this$jscomp$9$$.$v$);
    $component$jscomp$inline_402$$ && ((null === ($_a$jscomp$inline_400$$ = $component$jscomp$inline_402$$.$parentComponent$) || void 0 === $_a$jscomp$inline_400$$ ? 0 : $_a$jscomp$inline_400$$.$isOpen$()) ? null === ($_b$jscomp$inline_401$$ = $component$jscomp$inline_402$$.$parentComponent$) || void 0 === $_b$jscomp$inline_401$$ ? void 0 : $_b$jscomp$inline_401$$.close() : new $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$($$jscomp$super$this$jscomp$9$$.title, $component$jscomp$inline_402$$, 
    $$jscomp$super$this$jscomp$9$$.x, $$jscomp$super$this$jscomp$9$$.y));
  });
  return $$jscomp$super$this$jscomp$9$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$ShowFloatingWindowComponent$default$$.$ShowFloatingWindowComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
var $module$build$ts$action$DebugAction$default$$ = {};
Object.defineProperty($module$build$ts$action$DebugAction$default$$, "__esModule", {value:!0});
$module$build$ts$action$DebugAction$default$$.$DebugAction$ = void 0;
$module$build$ts$action$DebugAction$default$$.$DebugAction$ = function() {
  var $$jscomp$super$this$jscomp$10$$ = $module$build$ts$action$SlashAction$default$$.$SlashAction$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$10$$.$minParams$ = 0;
  $$jscomp$super$this$jscomp$10$$.$maxParams$ = 0;
  $$jscomp$super$this$jscomp$10$$.$v$ = !1;
  return $$jscomp$super$this$jscomp$10$$;
};
$$jscomp$inherits$$($module$build$ts$action$DebugAction$default$$.$DebugAction$, $module$build$ts$action$SlashAction$default$$.$SlashAction$);
$module$build$ts$action$DebugAction$default$$.$DebugAction$.prototype.$execute$ = function($_type$jscomp$1_weather$jscomp$inline_409$$, $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$) {
  if (1 > $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$.length) {
    $module$build$ts$util$Chat$default$$.$Chat$.$v$("error", "Expected parameters"), $JSCompiler_StaticMethods_showUsage$$();
  } else {
    if (-1 < ["help", "?"].indexOf($params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$[0])) {
      $JSCompiler_StaticMethods_showUsage$$();
    } else {
      if ("ui" === $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$[0]) {
        console.log($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$);
        for (var $i$jscomp$inline_406$$ in $module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$) {
          console.log("in", $i$jscomp$inline_406$$);
        }
        "pop" === $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$[1] && ($JSCompiler_StaticMethods_uiFloatComponent$$(this, 1, "Left panel", 10, 10), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 2, "Right panel", 500, 10), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 0, "Top panel", 200, 10), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 3, "Bottom panel", 10, 500), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 108, "Map", 10, 50), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 
        110, "Stats", 10, 190), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 102, "Buddies", 10, 250), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 106, $marauroa$$.$me$._name, 500, 50), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 101, "Bag", 500, 200), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 107, "Keyring", 500, 300), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 103, "Chat", 100, 500), $JSCompiler_StaticMethods_uiFloatComponent$$(this, 104, "Chat log", 100, 560), 
        this.$v$ = !0);
      } else {
        if ("weather" === $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$[0]) {
          a: {
            if ($_type$jscomp$1_weather$jscomp$inline_409$$ = $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$[1], $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$ = ["Usage:", "  /debug weather [<name>]"], $_type$jscomp$1_weather$jscomp$inline_409$$ && -1 < ["help", "?"].indexOf($_type$jscomp$1_weather$jscomp$inline_409$$)) {
              $module$build$ts$util$Chat$default$$.$Chat$.log("client", $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$);
            } else {
              $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.weather") || $module$build$ts$util$Chat$default$$.$Chat$.$v$("warning", "Weather is disabled.");
              if ($_type$jscomp$1_weather$jscomp$inline_409$$ && ($_type$jscomp$1_weather$jscomp$inline_409$$ = $_type$jscomp$1_weather$jscomp$inline_409$$.replace(/ /g, "_"), $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$ = $stendhal$$.$paths$.$weather$ + "/" + $_type$jscomp$1_weather$jscomp$inline_409$$ + ".png", !$stendhal$$.data.$sprites$.images[$params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$])) {
                $module$build$ts$util$Chat$default$$.$Chat$.$v$("error", "unknown weather: " + $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$);
                break a;
              }
              $module$build$ts$SingletonRepo$default$$.$singletons$.$C$().update($_type$jscomp$1_weather$jscomp$inline_409$$);
            }
          }
        } else {
          "log" === $params$jscomp$1_usage$jscomp$inline_410_wfilename$jscomp$inline_411$$[0] && ($module$build$ts$util$Chat$default$$.$Chat$.$D$ = !0);
        }
      }
    }
  }
  return !0;
};
function $JSCompiler_StaticMethods_showUsage$$() {
  $module$build$ts$util$Chat$default$$.$Chat$.log("client", ["Usage:", "  /debug log", "  /debug ui [pop]", "  /debug weather [<name>]"]);
}
function $JSCompiler_StaticMethods_uiFloatComponent$$($JSCompiler_StaticMethods_uiFloatComponent$self$$, $uiComponentEnum$jscomp$1$$, $title$jscomp$18$$, $x$jscomp$105$$, $y$jscomp$85$$) {
  var $component$jscomp$3$$ = $module$build$ts$ui$UI$default$$.$ui$.get($uiComponentEnum$jscomp$1$$);
  $component$jscomp$3$$ && ($component$jscomp$3$$.$componentElement$.dispatchEvent(new Event("close")), $component$jscomp$3$$.$componentElement$.remove(), new $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$($title$jscomp$18$$, $component$jscomp$3$$, $x$jscomp$105$$, $y$jscomp$85$$), $JSCompiler_StaticMethods_uiFloatComponent$self$$.$v$ || $module$build$ts$ui$UI$default$$.$ui$.get(3).add(new $module$build$ts$ui$component$ShowFloatingWindowComponent$default$$.$ShowFloatingWindowComponent$($uiComponentEnum$jscomp$1$$, 
  $title$jscomp$18$$, $x$jscomp$105$$, $y$jscomp$85$$)));
}
;var $module$build$ts$action$OpenWebsiteAction$default$$ = {};
Object.defineProperty($module$build$ts$action$OpenWebsiteAction$default$$, "__esModule", {value:!0});
$module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$ = void 0;
$module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$ = function($url$jscomp$23$$) {
  var $$jscomp$super$this$jscomp$11$$ = $module$build$ts$action$SlashAction$default$$.$SlashAction$.call(this) || this;
  $$jscomp$super$this$jscomp$11$$.url = $url$jscomp$23$$;
  $$jscomp$super$this$jscomp$11$$.$minParams$ = 0;
  $$jscomp$super$this$jscomp$11$$.$maxParams$ = 0;
  return $$jscomp$super$this$jscomp$11$$;
};
$$jscomp$inherits$$($module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$, $module$build$ts$action$SlashAction$default$$.$SlashAction$);
$module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$.prototype.$execute$ = function() {
  window.location.href = this.url;
  return !0;
};
var $module$build$ts$ui$dialog$SettingsDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$SettingsDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$SettingsDialog$default$$.$SettingsDialog$ = void 0;
$module$build$ts$ui$dialog$SettingsDialog$default$$.$SettingsDialog$ = function() {
  var $$jscomp$super$this$jscomp$12$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "settingsdialog-template") || this;
  var $clog$$ = $module$build$ts$ui$UI$default$$.$ui$.get(104);
  $$jscomp$super$this$jscomp$12$$.$A$ = {txtjoystickx:$stendhal$$.$config$.get("ui.joystick.center.x"), txtjoysticky:$stendhal$$.$config$.get("ui.joystick.center.y")};
  $$jscomp$super$this$jscomp$12$$.$B$ = {"gamescreen.blood":$stendhal$$.$config$.get("gamescreen.blood"),};
  var $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$ = $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_light", "gamescreen.lighting", "Lighting effects are enabled", "Lighting effects are disabled");
  $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.disabled = !0;
  $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.parentElement.title = "Lighting effects not currently supported";
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_weather", "gamescreen.weather", "Weather is enabled", "Weather is disabled", function() {
    $clog$$ && $JSCompiler_StaticMethods_addLine$$($clog$$, "client", "Weather changes will take effect after you change maps.");
  }).parentElement.title = "Weather effects not currently supported";
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_blood", "gamescreen.blood", "Gory images are enabled", "Gory images are disabled");
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_nonude", "gamescreen.nonude", "Naked entities have undergarments", "Naked entities are not covered");
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_shadows", "gamescreen.shadows", "Shadows are enabled", "Shadows are disabled");
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_speechcr", "gamescreen.speech.creature", "Creature speech bubbles are enabled", "Creature speech bubbles are disabled");
  var $player_stats$$ = $module$build$ts$ui$UI$default$$.$ui$.get(110), $chk_charname$$ = $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_charname", "ui.stats.charname", void 0, void 0, function() {
    $JSCompiler_StaticMethods_enableCharName$$($chk_charname$$.checked);
  }), $chk_hpbar$$ = $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_hpbar", "ui.stats.hpbar", void 0, void 0, function() {
    $JSCompiler_StaticMethods_enableBar$$($player_stats$$, "hp", $chk_hpbar$$.checked);
  });
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_dblclick", "action.item.doubleclick", "Items are used/consumed with double click/touch", "Items are used/consumed with single click/touch", function() {
    $module$build$ts$ui$UI$default$$.$ui$.get(106).$markDirty$();
    for (var $$jscomp$iter$24$$ = $$jscomp$makeIterator$$([101, 107]), $$jscomp$key$cid$$ = $$jscomp$iter$24$$.next(); !$$jscomp$key$cid$$.done; $$jscomp$key$cid$$ = $$jscomp$iter$24$$.next()) {
      $module$build$ts$ui$UI$default$$.$ui$.get($$jscomp$key$cid$$.value).$markDirty$();
    }
  });
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_chestqp", "action.inventory.quickpickup", "Click tranfers items from chests and corpses to player inventory", "Click executes default action on items in chests and corpses");
  var $chk_movecont$$ = $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_movecont", "input.movecont", "Player will continue to walk after changing areas", "Player will stop after changing areas", function() {
    var $action$jscomp$14$$ = {type:"move.continuous"};
    $chk_movecont$$.checked && ($action$jscomp$14$$["move.continuous"] = "");
    $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$14$$);
  });
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_pvtsnd", "event.pvtmsg.sound", "Private message audio notifications enabled", "Private message audio notifications disabled", void 0, "ui/notify_up", "null").checked = "ui/notify_up" === $stendhal$$.$config$.get("event.pvtmsg.sound");
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_clickindicator", "input.click.indicator", "Displaying clicks", "Not displaying clicks").disabled = !0;
  $JSCompiler_StaticMethods_createCheckBox$$($$jscomp$super$this$jscomp$12$$, "chk_pathfinding", "client.pathfinding", "Pathfinding on ground enabled", "Pathfinding on ground disabled");
  var $themes$$ = {};
  $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$ = $$jscomp$makeIterator$$(Object.keys($stendhal$$.$config$.$themes$.map));
  for (var $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$ = $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.next(); !$$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$.done; $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$ = $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.next()) {
    $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$ = $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$.value, $themes$$[$$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$] = "wood" === $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$ ? $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$ + " (default)" : $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$;
  }
  var $sel_theme$$ = $JSCompiler_StaticMethods_createSelect$$($$jscomp$super$this$jscomp$12$$, "selecttheme", $themes$$, Object.keys($themes$$).indexOf($JSCompiler_StaticMethods_getTheme$$($stendhal$$.$config$)));
  $sel_theme$$.addEventListener("change", function() {
    var $value$jscomp$inline_414$$ = Object.keys($themes$$)[$sel_theme$$.selectedIndex];
    $stendhal$$.$config$.set("ui.theme", $value$jscomp$inline_414$$);
    $JSCompiler_StaticMethods_refreshTheme$$();
  });
  var $fonts$$ = Object.keys($stendhal$$.$config$.fonts), $sel_fontbody$$ = $JSCompiler_StaticMethods_createFontSelect$$($$jscomp$super$this$jscomp$12$$, "selfontbody", $fonts$$.indexOf($stendhal$$.$config$.get("ui.font.body")));
  $sel_fontbody$$.addEventListener("change", function() {
    var $new_font$$ = $fonts$$[$sel_fontbody$$.selectedIndex];
    $stendhal$$.$config$.set("ui.font.body", $new_font$$);
    document.body.style.setProperty("font-family", $new_font$$);
  });
  var $sel_fontchat$$ = $JSCompiler_StaticMethods_createFontSelect$$($$jscomp$super$this$jscomp$12$$, "selfontchat", $fonts$$.indexOf($stendhal$$.$config$.get("ui.font.chat")));
  $sel_fontchat$$.addEventListener("change", function() {
    $stendhal$$.$config$.set("ui.font.chat", $fonts$$[$sel_fontchat$$.selectedIndex]);
    $clog$$ && $clog$$.refresh();
  });
  var $sel_fonttlog$$ = $JSCompiler_StaticMethods_createFontSelect$$($$jscomp$super$this$jscomp$12$$, "selfonttlog", $fonts$$.indexOf($stendhal$$.$config$.get("ui.font.tlog")));
  $sel_fonttlog$$.addEventListener("change", function() {
    $stendhal$$.$config$.set("ui.font.tlog", $fonts$$[$sel_fonttlog$$.selectedIndex]);
    var $tlog$$ = $module$build$ts$ui$UI$default$$.$ui$.get($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$TravelLogDialog$);
    $tlog$$ && $tlog$$.refresh();
  });
  var $txt_chatopts$$ = $JSCompiler_StaticMethods_createTextInput$$($$jscomp$super$this$jscomp$12$$, "txtchatopts", $stendhal$$.$config$.get("chat.custom_keywords"), "Comma-separated list accessible from the chat options dialog");
  $txt_chatopts$$.addEventListener("change", function() {
    $stendhal$$.$config$.set("chat.custom_keywords", $txt_chatopts$$.value);
  });
  var $js_orienters$$ = [], $js_styles$$ = {none:"none", joystick:"joystick", dpad:"direction pad",};
  $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$ = Object.keys($js_styles$$).indexOf($stendhal$$.$config$.get("ui.joystick"));
  0 > $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$ && ($$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$ = 0);
  var $sel_joystick$$ = $JSCompiler_StaticMethods_createSelect$$($$jscomp$super$this$jscomp$12$$, "seljoystick", $js_styles$$, $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$);
  $sel_joystick$$.addEventListener("change", function() {
    $stendhal$$.$config$.set("ui.joystick", Object.keys($js_styles$$)[$sel_joystick$$.selectedIndex]);
    $JSCompiler_StaticMethods_updateJoystick$$();
    for (var $$jscomp$iter$26$$ = $$jscomp$makeIterator$$($js_orienters$$), $$jscomp$key$orienter$$ = $$jscomp$iter$26$$.next(); !$$jscomp$key$orienter$$.done; $$jscomp$key$orienter$$ = $$jscomp$iter$26$$.next()) {
      $$jscomp$key$orienter$$.value.disabled = 1 > $sel_joystick$$.selectedIndex;
    }
  });
  $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$ = {};
  $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$ = $$jscomp$makeIterator$$(["x", "y"]);
  for (var $$jscomp$key$orient$$ = $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$.next(); !$$jscomp$key$orient$$.done; $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$ = {$$jscomp$loop$prop$orient$113$:$$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$orient$113$, $$jscomp$loop$prop$input_temp$114$:$$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$input_temp$114$}, $$jscomp$key$orient$$ = $$jscomp$iter$27_$jscomp$key$t_t$jscomp$2$$.next()) {
    $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$orient$113$ = $$jscomp$key$orient$$.value, $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$input_temp$114$ = $JSCompiler_StaticMethods_createNumberInput$$($$jscomp$super$this$jscomp$12$$, "txtjoystick" + $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$orient$113$, parseInt($$jscomp$super$this$jscomp$12$$.$A$["txtjoystick" + $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$orient$113$], 
    10), "Joystick position on " + $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$orient$113$.toUpperCase() + " axis"), $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$input_temp$114$.addEventListener("input", function($$jscomp$loop$112$jscomp$1$$) {
      return function() {
        $stendhal$$.$config$.set("ui.joystick.center." + $$jscomp$loop$112$jscomp$1$$.$$jscomp$loop$prop$orient$113$, $$jscomp$loop$112$jscomp$1$$.$$jscomp$loop$prop$input_temp$114$.value || 0);
        $JSCompiler_StaticMethods_updateJoystick$$();
      };
    }($$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$)), $$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$input_temp$114$.disabled = 1 > $sel_joystick$$.selectedIndex, $js_orienters$$.push($$jscomp$iter$25_$jscomp$loop$112_chk_light_js_idx$$.$$jscomp$loop$prop$input_temp$114$);
  }
  $$jscomp$super$this$jscomp$12$$.$v$ = $JSCompiler_StaticMethods_createButton$$($$jscomp$super$this$jscomp$12$$, "btn_config_reload", "Reloads page if required by changes");
  $$jscomp$super$this$jscomp$12$$.$v$.disabled = !0;
  $$jscomp$super$this$jscomp$12$$.$v$.addEventListener("click", function() {
    $$jscomp$super$this$jscomp$12$$.close();
    location.reload();
  });
  $JSCompiler_StaticMethods_createButton$$($$jscomp$super$this$jscomp$12$$, "btn_config_close", "Close this dialog without reloading page").addEventListener("click", function() {
    $$jscomp$super$this$jscomp$12$$.close();
  });
  $$jscomp$super$this$jscomp$12$$.$v$.parentElement.style.setProperty("padding-top", "15px");
  return $$jscomp$super$this$jscomp$12$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$SettingsDialog$default$$.$SettingsDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
$module$build$ts$ui$dialog$SettingsDialog$default$$.$SettingsDialog$.prototype.refresh = function() {
  for (var $reloadRequired$$ = !1, $$jscomp$iter$28$$ = $$jscomp$makeIterator$$(Object.keys(this.$B$)), $$jscomp$key$key_key$jscomp$63$$ = $$jscomp$iter$28$$.next(); !$$jscomp$key$key_key$jscomp$63$$.done; $$jscomp$key$key_key$jscomp$63$$ = $$jscomp$iter$28$$.next()) {
    if ($$jscomp$key$key_key$jscomp$63$$ = $$jscomp$key$key_key$jscomp$63$$.value, $stendhal$$.$config$.get($$jscomp$key$key_key$jscomp$63$$) !== this.$B$[$$jscomp$key$key_key$jscomp$63$$]) {
      $reloadRequired$$ = !0;
      break;
    }
  }
  this.$v$.disabled = !$reloadRequired$$;
};
function $JSCompiler_StaticMethods_createCheckBox$$($JSCompiler_StaticMethods_createCheckBox$self$$, $id$jscomp$17$$, $setid$$, $ttpos$$, $ttneg$$, $action$jscomp$15$$, $von$$, $voff$$) {
  $ttpos$$ = void 0 === $ttpos$$ ? "" : $ttpos$$;
  $ttneg$$ = void 0 === $ttneg$$ ? "" : $ttneg$$;
  var $chk$$ = $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_createCheckBox$self$$, "input[type=checkbox][id=" + $id$jscomp$17$$ + "]");
  $chk$$.checked = $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, $setid$$);
  var $tt$$ = new $CheckTooltip$$module$build$ts$ui$dialog$SettingsDialog$$($ttpos$$, $ttneg$$);
  $chk$$.parentElement.title = $chk$$.checked ? $tt$$.$A$ : $tt$$.$v$;
  $chk$$.addEventListener("change", function() {
    $chk$$.checked && "undefined" !== typeof $von$$ ? $stendhal$$.$config$.set($setid$$, $von$$) : $chk$$.checked || "undefined" === typeof $voff$$ ? $stendhal$$.$config$.set($setid$$, $chk$$.checked) : $stendhal$$.$config$.set($setid$$, $voff$$);
    $chk$$.parentElement.title = $chk$$.checked ? $tt$$.$A$ : $tt$$.$v$;
    $action$jscomp$15$$ && $action$jscomp$15$$();
    $JSCompiler_StaticMethods_createCheckBox$self$$.refresh();
  });
  return $chk$$;
}
function $JSCompiler_StaticMethods_createButton$$($JSCompiler_StaticMethods_createButton$self_button$jscomp$1$$, $id$jscomp$18$$, $tooltip$jscomp$1$$) {
  $JSCompiler_StaticMethods_createButton$self_button$jscomp$1$$ = $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_createButton$self_button$jscomp$1$$, "button[id=" + $id$jscomp$18$$ + "]");
  $tooltip$jscomp$1$$ && ($JSCompiler_StaticMethods_createButton$self_button$jscomp$1$$.title = $tooltip$jscomp$1$$);
  return $JSCompiler_StaticMethods_createButton$self_button$jscomp$1$$;
}
function $JSCompiler_StaticMethods_createSelect$$($JSCompiler_StaticMethods_createSelect$self_sel$$, $$jscomp$iter$29_id$jscomp$19$$, $options$jscomp$47$$, $idx$jscomp$7$$, $tooltip$jscomp$2$$) {
  $JSCompiler_StaticMethods_createSelect$self_sel$$ = $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_createSelect$self_sel$$, "select[id=" + $$jscomp$iter$29_id$jscomp$19$$ + "]");
  $JSCompiler_StaticMethods_createSelect$self_sel$$.style.setProperty("width", "9em");
  $JSCompiler_StaticMethods_createSelect$self_sel$$.parentElement.style.setProperty("margin-right", "0");
  $JSCompiler_StaticMethods_createSelect$self_sel$$.parentElement.style.setProperty("margin-left", "auto");
  $JSCompiler_StaticMethods_createSelect$self_sel$$.parentElement.style.setProperty("padding-bottom", "5px");
  $$jscomp$iter$29_id$jscomp$19$$ = $$jscomp$makeIterator$$(Object.keys($options$jscomp$47$$));
  for (var $$jscomp$key$key$jscomp$1_key$jscomp$64$$ = $$jscomp$iter$29_id$jscomp$19$$.next(); !$$jscomp$key$key$jscomp$1_key$jscomp$64$$.done; $$jscomp$key$key$jscomp$1_key$jscomp$64$$ = $$jscomp$iter$29_id$jscomp$19$$.next()) {
    $$jscomp$key$key$jscomp$1_key$jscomp$64$$ = $$jscomp$key$key$jscomp$1_key$jscomp$64$$.value;
    var $opt$$ = document.createElement("option");
    $opt$$.value = $$jscomp$key$key$jscomp$1_key$jscomp$64$$;
    $opt$$.textContent = $options$jscomp$47$$[$$jscomp$key$key$jscomp$1_key$jscomp$64$$];
    $JSCompiler_StaticMethods_createSelect$self_sel$$.appendChild($opt$$);
  }
  $JSCompiler_StaticMethods_createSelect$self_sel$$.selectedIndex = $idx$jscomp$7$$;
  $tooltip$jscomp$2$$ && ($JSCompiler_StaticMethods_createSelect$self_sel$$.title = $tooltip$jscomp$2$$);
  return $JSCompiler_StaticMethods_createSelect$self_sel$$;
}
function $JSCompiler_StaticMethods_createFontSelect$$($JSCompiler_StaticMethods_createFontSelect$self$$, $id$jscomp$20$$, $idx$jscomp$8$$) {
  for (var $options$jscomp$48$$ = {}, $$jscomp$iter$30$$ = $$jscomp$makeIterator$$(Object.keys($stendhal$$.$config$.fonts)), $$jscomp$key$key$jscomp$2_key$jscomp$65$$ = $$jscomp$iter$30$$.next(); !$$jscomp$key$key$jscomp$2_key$jscomp$65$$.done; $$jscomp$key$key$jscomp$2_key$jscomp$65$$ = $$jscomp$iter$30$$.next()) {
    $$jscomp$key$key$jscomp$2_key$jscomp$65$$ = $$jscomp$key$key$jscomp$2_key$jscomp$65$$.value;
    var $value$jscomp$106$$ = $stendhal$$.$config$.fonts[$$jscomp$key$key$jscomp$2_key$jscomp$65$$];
    "" === $value$jscomp$106$$ && ($value$jscomp$106$$ = $$jscomp$key$key$jscomp$2_key$jscomp$65$$);
    $options$jscomp$48$$[$$jscomp$key$key$jscomp$2_key$jscomp$65$$] = $value$jscomp$106$$;
  }
  return $JSCompiler_StaticMethods_createSelect$$($JSCompiler_StaticMethods_createFontSelect$self$$, $id$jscomp$20$$, $options$jscomp$48$$, $idx$jscomp$8$$, void 0);
}
function $JSCompiler_StaticMethods_createTextInput$$($JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$, $id$jscomp$21$$, $value$jscomp$107$$, $tooltip$jscomp$4$$, $type$jscomp$161$$) {
  $value$jscomp$107$$ = void 0 === $value$jscomp$107$$ ? "" : $value$jscomp$107$$;
  $JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$ = $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$, "input[type=" + (void 0 === $type$jscomp$161$$ ? "text" : $type$jscomp$161$$) + "][id=" + $id$jscomp$21$$ + "]");
  $JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$.style.setProperty("width", "9em");
  $JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$.parentElement.style.setProperty("margin-right", "0");
  $JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$.parentElement.style.setProperty("margin-left", "auto");
  $JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$.parentElement.style.setProperty("padding-bottom", "5px");
  $JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$.value = $value$jscomp$107$$;
  $tooltip$jscomp$4$$ && ($JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$.title = $tooltip$jscomp$4$$);
  return $JSCompiler_StaticMethods_createTextInput$self_input$jscomp$13$$;
}
function $JSCompiler_StaticMethods_createNumberInput$$($JSCompiler_StaticMethods_createNumberInput$self$$, $id$jscomp$22$$, $value$jscomp$108$$, $tooltip$jscomp$5$$) {
  var $input$jscomp$14$$ = $JSCompiler_StaticMethods_createTextInput$$($JSCompiler_StaticMethods_createNumberInput$self$$, $id$jscomp$22$$, "" + (void 0 === $value$jscomp$108$$ ? 0 : $value$jscomp$108$$), $tooltip$jscomp$5$$, "number");
  $input$jscomp$14$$.addEventListener("input", function($e$jscomp$50_new_char$$) {
    $e$jscomp$50_new_char$$ = $e$jscomp$50_new_char$$.data;
    var $new_digit$$ = Number($e$jscomp$50_new_char$$);
    void 0 != $e$jscomp$50_new_char$$ && "" === $e$jscomp$50_new_char$$.replace(/ |\t/g, "") || Number.isNaN($new_digit$$) ? $input$jscomp$14$$.value = $JSCompiler_StaticMethods_createNumberInput$self$$.$A$[$input$jscomp$14$$.id] : ($input$jscomp$14$$.value = "" + parseInt($input$jscomp$14$$.value.replace(/ |\t/g, ""), 10), $JSCompiler_StaticMethods_createNumberInput$self$$.$A$[$input$jscomp$14$$.id] = $input$jscomp$14$$.value);
  });
  return $input$jscomp$14$$;
}
function $CheckTooltip$$module$build$ts$ui$dialog$SettingsDialog$$($e$jscomp$51$$, $d$jscomp$32$$) {
  this.$A$ = $e$jscomp$51$$;
  this.$v$ = $d$jscomp$32$$;
}
;var $module$build$ts$action$SettingsAction$default$$ = {};
Object.defineProperty($module$build$ts$action$SettingsAction$default$$, "__esModule", {value:!0});
$module$build$ts$action$SettingsAction$default$$.$SettingsAction$ = void 0;
$module$build$ts$action$SettingsAction$default$$.$SettingsAction$ = function() {
  var $$jscomp$super$this$jscomp$13$$ = $module$build$ts$action$SlashAction$default$$.$SlashAction$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$13$$.$minParams$ = 0;
  $$jscomp$super$this$jscomp$13$$.$maxParams$ = 0;
  return $$jscomp$super$this$jscomp$13$$;
};
$$jscomp$inherits$$($module$build$ts$action$SettingsAction$default$$.$SettingsAction$, $module$build$ts$action$SlashAction$default$$.$SlashAction$);
$module$build$ts$action$SettingsAction$default$$.$SettingsAction$.prototype.$execute$ = function() {
  var $dialog$jscomp$1_wstate$$ = $JSCompiler_StaticMethods_getWindowState$$("menu"), $offset$jscomp$28$$ = $JSCompiler_StaticMethods_getPageOffset$$(), $content$jscomp$5$$ = new $module$build$ts$ui$dialog$SettingsDialog$default$$.$SettingsDialog$();
  $dialog$jscomp$1_wstate$$ = $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Settings", $content$jscomp$5$$, $dialog$jscomp$1_wstate$$.x - $offset$jscomp$28$$.x, $dialog$jscomp$1_wstate$$.y - $offset$jscomp$28$$.y);
  $dialog$jscomp$1_wstate$$.$windowId$ = "menu";
  $content$jscomp$5$$.frame = $dialog$jscomp$1_wstate$$;
  return !0;
};
var $module$build$ts$SlashActionRepo$default$$ = {};
Object.defineProperty($module$build$ts$SlashActionRepo$default$$, "__esModule", {value:!0});
$module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$ = void 0;
$module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$ = function() {
  var $$jscomp$this$jscomp$11$$ = this;
  this.add = {$execute$:function($type$jscomp$162$$, $params$jscomp$3$$) {
    if (null == $params$jscomp$3$$) {
      return !1;
    }
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"addbuddy", target:$params$jscomp$3$$[0]});
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this.adminnote = {$execute$:function($type$jscomp$163$$, $params$jscomp$4$$, $remainder$jscomp$1$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:$type$jscomp$163$$, target:$params$jscomp$4$$[0], note:$remainder$jscomp$1$$});
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this.adminlevel = {$execute$:function($action$jscomp$18_type$jscomp$164$$, $params$jscomp$5$$) {
    $action$jscomp$18_type$jscomp$164$$ = {type:$action$jscomp$18_type$jscomp$164$$, target:$params$jscomp$5$$[0],};
    2 <= $params$jscomp$5$$.length && ($action$jscomp$18_type$jscomp$164$$.newlevel = $params$jscomp$5$$[1]);
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$18_type$jscomp$164$$);
    return !0;
  }, $minParams$:1, $maxParams$:2};
  this.alter = {$execute$:function($type$jscomp$165$$, $params$jscomp$6$$, $remainder$jscomp$3$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:$type$jscomp$165$$, target:$params$jscomp$6$$[0], stat:$params$jscomp$6$$[1], mode:$params$jscomp$6$$[2], value:$remainder$jscomp$3$$});
    return !0;
  }, $minParams$:3, $maxParams$:3};
  this.altercreature = {$execute$:function($type$jscomp$166$$, $params$jscomp$7$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"altercreature", target:$params$jscomp$7$$[0], text:$params$jscomp$7$$[1]});
    return !0;
  }, $minParams$:2, $maxParams$:2};
  this.alterkill = {$execute$:function($target$jscomp$108_type$jscomp$167$$, $creature_params$jscomp$8$$, $action$jscomp$21_remainder$jscomp$5$$) {
    $target$jscomp$108_type$jscomp$167$$ = $creature_params$jscomp$8$$[0];
    var $killtype$$ = $creature_params$jscomp$8$$[1], $count$jscomp$39$$ = $creature_params$jscomp$8$$[2];
    $creature_params$jscomp$8$$ = null;
    null != $action$jscomp$21_remainder$jscomp$5$$ && "" != $action$jscomp$21_remainder$jscomp$5$$ && ($creature_params$jscomp$8$$ = $action$jscomp$21_remainder$jscomp$5$$);
    $action$jscomp$21_remainder$jscomp$5$$ = {type:"alterkill", target:$target$jscomp$108_type$jscomp$167$$, killtype:$killtype$$, count:$count$jscomp$39$$};
    null != $creature_params$jscomp$8$$ && ($action$jscomp$21_remainder$jscomp$5$$.creature = $creature_params$jscomp$8$$);
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$21_remainder$jscomp$5$$);
    return !0;
  }, $minParams$:3, $maxParams$:3};
  this.alterquest = {$execute$:function($action$jscomp$22_type$jscomp$168$$, $p$jscomp$inline_423_params$jscomp$9$$, $remainder$jscomp$6$$) {
    $action$jscomp$22_type$jscomp$168$$ = {type:"alterquest", target:$p$jscomp$inline_423_params$jscomp$9$$[0], name:$p$jscomp$inline_423_params$jscomp$9$$[1]};
    if (null != $p$jscomp$inline_423_params$jscomp$9$$[2]) {
      $p$jscomp$inline_423_params$jscomp$9$$ = $p$jscomp$inline_423_params$jscomp$9$$[2];
      if ($p$jscomp$inline_423_params$jscomp$9$$.includes('"') && $remainder$jscomp$6$$.includes('"')) {
        var $endQuote$jscomp$inline_425$$ = !1, $paramEnd$jscomp$inline_426$$ = 0, $$jscomp$inline_428_arr$jscomp$inline_427$$ = Array.from($remainder$jscomp$6$$);
        $$jscomp$inline_428_arr$jscomp$inline_427$$ = $$jscomp$makeIterator$$($$jscomp$inline_428_arr$jscomp$inline_427$$);
        for (var $$jscomp$inline_429_c$jscomp$inline_430$$ = $$jscomp$inline_428_arr$jscomp$inline_427$$.next(); !$$jscomp$inline_429_c$jscomp$inline_430$$.done; $$jscomp$inline_429_c$jscomp$inline_430$$ = $$jscomp$inline_428_arr$jscomp$inline_427$$.next()) {
          $$jscomp$inline_429_c$jscomp$inline_430$$ = $$jscomp$inline_429_c$jscomp$inline_430$$.value;
          if (" " === $$jscomp$inline_429_c$jscomp$inline_430$$ && $endQuote$jscomp$inline_425$$) {
            break;
          } else {
            '"' === $$jscomp$inline_429_c$jscomp$inline_430$$ && ($endQuote$jscomp$inline_425$$ = !$endQuote$jscomp$inline_425$$);
          }
          $paramEnd$jscomp$inline_426$$++;
        }
        $p$jscomp$inline_423_params$jscomp$9$$ = ($p$jscomp$inline_423_params$jscomp$9$$ + " " + $remainder$jscomp$6$$.substring(0, $paramEnd$jscomp$inline_426$$ + 1)).replace(/"/g, "").trim();
      }
      $action$jscomp$22_type$jscomp$168$$.state = $p$jscomp$inline_423_params$jscomp$9$$;
    }
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$22_type$jscomp$168$$);
    return !0;
  }, $minParams$:2, $maxParams$:3};
  this.answer = {$execute$:function($type$jscomp$169$$, $params$jscomp$10$$, $remainder$jscomp$7$$) {
    if (null == $remainder$jscomp$7$$ || "" == $remainder$jscomp$7$$) {
      return !1;
    }
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"answer", text:$remainder$jscomp$7$$});
    return !0;
  }, $minParams$:1, $maxParams$:0};
  this.away = {$execute$:function($action$jscomp$24_type$jscomp$170$$, $params$jscomp$11$$, $remainder$jscomp$8$$) {
    $action$jscomp$24_type$jscomp$170$$ = {type:"away",};
    0 != $remainder$jscomp$8$$.length && ($action$jscomp$24_type$jscomp$170$$.message = $remainder$jscomp$8$$);
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$24_type$jscomp$170$$);
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.ban = {$execute$:function($type$jscomp$171$$, $params$jscomp$12$$, $remainder$jscomp$9$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"ban", target:$params$jscomp$12$$[0], hours:$params$jscomp$12$$[1], reason:$remainder$jscomp$9$$});
    return !0;
  }, $minParams$:2, $maxParams$:2};
  this.chat = {$execute$:function($type$jscomp$172$$, $params$jscomp$13$$, $remainder$jscomp$10$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:$type$jscomp$172$$, text:$remainder$jscomp$10$$});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.clear = {$execute$:function() {
    $module$build$ts$ui$UI$default$$.$ui$.get(104).clear();
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.debug = new $module$build$ts$action$DebugAction$default$$.$DebugAction$();
  this.drop = {$execute$:function($name$jscomp$77_type$jscomp$174$$, $action$jscomp$27_params$jscomp$15$$, $remainder$jscomp$12$$) {
    console.log($name$jscomp$77_type$jscomp$174$$, $action$jscomp$27_params$jscomp$15$$, $remainder$jscomp$12$$);
    $name$jscomp$77_type$jscomp$174$$ = $remainder$jscomp$12$$;
    var $quantity$jscomp$2$$ = parseInt($action$jscomp$27_params$jscomp$15$$[0], 10);
    console.log($name$jscomp$77_type$jscomp$174$$, $quantity$jscomp$2$$);
    isNaN($quantity$jscomp$2$$) && ($name$jscomp$77_type$jscomp$174$$ = ($action$jscomp$27_params$jscomp$15$$[0] + " " + $remainder$jscomp$12$$).trim(), $quantity$jscomp$2$$ = 0);
    console.log($name$jscomp$77_type$jscomp$174$$, $quantity$jscomp$2$$);
    $action$jscomp$27_params$jscomp$15$$ = {type:"drop", source_name:$name$jscomp$77_type$jscomp$174$$, quantity:"" + $quantity$jscomp$2$$, x:"" + $marauroa$$.$me$.x, y:"" + $marauroa$$.$me$.y};
    console.log($action$jscomp$27_params$jscomp$15$$);
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$27_params$jscomp$15$$);
    return !0;
  }, $minParams$:0, $maxParams$:1};
  this.emojilist = {$execute$:function() {
    var $emojilist$$ = [].concat($$jscomp$arrayFromIterable$$($module$build$ts$SingletonRepo$default$$.$singletons$.$B$().$emojilist$)).sort(), $idx$jscomp$9$$;
    for ($idx$jscomp$9$$ in $emojilist$$) {
      $emojilist$$[$idx$jscomp$9$$] = "&nbsp;&nbsp;- :" + $emojilist$$[$idx$jscomp$9$$] + ":";
    }
    $emojilist$$.splice(0, 0, $emojilist$$.length + " emojis available:");
    $module$build$ts$util$Chat$default$$.$Chat$.log("client", $emojilist$$);
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.gag = {$execute$:function($type$jscomp$176$$, $params$jscomp$17$$, $remainder$jscomp$14$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"gag", target:$params$jscomp$17$$[0], minutes:$params$jscomp$17$$[1], reason:$remainder$jscomp$14$$});
    return !0;
  }, $minParams$:2, $maxParams$:2};
  this.group = {$execute$:function($type$jscomp$177$$, $params$jscomp$18$$, $remainder$jscomp$15$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"group_management", action:$params$jscomp$18$$[0], params:$remainder$jscomp$15$$});
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this.grumpy = {$execute$:function($action$jscomp$30_type$jscomp$178$$, $params$jscomp$19$$, $remainder$jscomp$16$$) {
    $action$jscomp$30_type$jscomp$178$$ = {type:"grumpy",};
    0 != $remainder$jscomp$16$$.length && ($action$jscomp$30_type$jscomp$178$$.reason = $remainder$jscomp$16$$);
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$30_type$jscomp$178$$);
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.help = {$execute$:function() {
    for (var $msg$jscomp$18$$ = "For a detailed reference, visit #https://stendhalgame.org/wiki/Stendhal_Manual;Here are the most-used commands:;* CHATTING:;- /me <action> \tShow a message about what you are doing.;- /tell <player> <message> \tSend a private message to #player.;- /answer <message>;\t\tSend a private message to the last player who sent a message to you.;- // <message>\tSend a private message to the last player you sent a message to.;- /storemessage <player> <message> \t\tStore a private message to deliver for an offline #player.;- /who \tList all players currently online.;- /where <player> \tShow the current location of #player.;- /sentence <text> \tSet message on stendhalgame.org profile page and what players see when using #Look.;* SUPPORT:;- /support <message> \t\tAsk an administrator for help.;- /faq \t\tOpen Stendhal FAQs wiki page in browser.;* ITEM MANIPULATION:;- /markscroll <text> \t\tMark your empty scroll and add a #text label.;* BUDDIES AND ENEMIES:;- /add <player> \tAdd #player to your buddy list.;- /remove <player>;\t\tRemove #player from your buddy list.;- /ignore <player> [minutes|*|- [reason...]] \t\tAdd #player to your ignore list.;- /ignore \tFind out who is on your ignore list.;- /unignore <player> \t\tRemove #player from your ignore list.;* STATUS:;- /away <message> \t\tSet an away message.;- /away \tRemove away status.;- /grumpy <message> \t\tSet a message to ignore all non-buddies.;- /grumpy \tRemove grumpy status.;- /name <pet> <name> \t\tGive a name to your pet.;- /profile [name] \tOpens a player profile page on stendhalgame.org.;* PLAYER CONTROL:;- /walk \tToggles autowalk on/off.;- /stopwalk \tTurns autowalk off.;- /movecont <on|off> \tToggle continuous movement (allows players to continue walking after map change or teleport without releasing direction key).;* CLIENT SETTINGS:;- /mute \tMute or unmute the sounds.;- /volume \tLists or sets the volume for sound and music.;* MISC:;- /info \t\tFind out what the current server time is.;- /clear \tClear chat log.;- /help \tShow help information.;- /removedetail \tRemove the detail layer (e.g. balloon, umbrella, etc.) from character.;- /emojilist \tList available emojis.".split(";"), 
    $i$jscomp$60$$ = 0; $i$jscomp$60$$ < $msg$jscomp$18$$.length; $i$jscomp$60$$++) {
      $module$build$ts$util$Chat$default$$.$Chat$.log("info", $msg$jscomp$18$$[$i$jscomp$60$$]);
    }
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.gmhelp = {$execute$:function($msg$jscomp$19_type$jscomp$180$$, $i$jscomp$61_params$jscomp$21$$) {
    if (null == $i$jscomp$61_params$jscomp$21$$[0]) {
      $msg$jscomp$19_type$jscomp$180$$ = 'For a detailed reference, visit #https://stendhalgame.org/wiki/Stendhal:Administration{Here are the most-used GM commands:{* GENERAL:{- /gmhelp [alter|script|support]{\t\tFor more info about alter, script or the supportanswer shortcuts.{- /adminnote <player> <note>{\t\tLogs a note about #player.{- /inspect <player>{\t\tShow complete details of #player.{- /inspectkill <player> <creature>{\t\tShow creature kill counts of #player for #creature.{- /inspectquest <player> [<quest_slot>]{\t\tShow the state of quest for #player.{- /script <scriptname>{\t\tLoad (or reload) a script on the server. See #/gmhelp #script for details.{* CHATTING:{- /supportanswer <player> <message>{\t\tReplies to a support question. Replace #message with $faq, $faqsocial, $ignore, $faqpvp, $wiki, $knownbug, $bugstracker, $rules, $notsupport or $spam shortcuts if desired.{- /tellall <message>{\t\tSend a private message to all logged-in players.{* PLAYER CONTROL:{- /teleportto <name>{\t\tTeleport yourself near the specified player or NPC.{- /teleclickmode \tMakes you teleport to the location you double click.{- /ghostmode \tMakes yourself invisible and intangible.{- /invisible \tToggles whether or not you are invisible to creatures.{* ENTITY MANIPULATION:{- /adminlevel <player> [<newlevel>]{\t\tDisplay or set the adminlevel of the specified #player.{- /jail <player> <minutes> <reason>{\t\tImprisons #player for a given length of time.{- /gag <player> <minutes> <reason>{\t\tGags #player for a given length of time (player is unable to send messages to anyone).{- /ban <character> <hours> <reason>{\t\tBans the account of the character from logging onto the game server or website for the specified amount of hours (-1 till end of time).{- /teleport <player> <zone> <x> <y>{\t\tTeleport #player to the given location.{- /alter <player> <attrib> <mode> <value>{\t\tAlter stat #attrib of #player by the given amount; #mode can be ADD, SUB, SET or UNSET. See #/gmhelp #alter for details.{- /altercreature <id> name;atk;def;hp;xp{\t\tChange values of the creature. Use #- as a placeholder to keep default value. Useful in raids.{- /alterkill <player> <type> <count> <creature>{\t\tChange number of #creature killed #type ("solo" or "shared") to #count for #player.{- /alterquest <player> <questslot> <value>{\t\tUpdate the #questslot for #player to be #value.{- /summon <creature|item> [x] [y]{- /summon <stackable item> [quantity]{- /summon <stackable item> <x> <y> [quantity]{\t\tSummon the specified item or creature at co-ordinates #x, #y in the current zone.{- /summonat <player> <slot> [amount] <item>{\t\tSummon the specified item into the specified slot of <player>; <amount> defaults to 1 if not specified.{- /destroy <entity> \tDestroy an entity completely.{* MISC:{- /jailreport [<player>]{\t\tList the jailed players and their sentences.'.split("{");
    } else {
      if (1 == $i$jscomp$61_params$jscomp$21$$.length && null != $i$jscomp$61_params$jscomp$21$$[0]) {
        if ("alter" == $i$jscomp$61_params$jscomp$21$$[0]) {
          $msg$jscomp$19_type$jscomp$180$$ = "/alter <player> <attrib> <mode> <value>{\t\tAlter stat <attrib> of <player> by the given amount; <mode> can be ADD, SUB, SET or UNSET.{\t\t- Examples of <attrib>: atk, def, base_hp, hp, atk_xp, def_xp, xp, outfit{\t\t- When modifying 'outfit', you should use SET mode and provide an 8-digit number; the first 2 digits are the 'hair' setting, then 'head', 'outfit', then 'body'{\t\t  For example: #'/alter testplayer outfit set 12109901'{\t\t  This will make <testplayer> look like danter".split("{");
        } else {
          if ("script" == $i$jscomp$61_params$jscomp$21$$[0]) {
            $msg$jscomp$19_type$jscomp$180$$ = "usage: /script [-list|-load|-unload|-execute] [params];\t-list : shows available scripts. In this mode can be given one optional parameter for filenames filtering, with using well-known wildcards for filenames ('*' and '?', for example \"*.class\" for java-only scripts).;\t-load : load script with first parameter's filename.;\t-unload : unload script with first parameter's filename from server;\t-execute : run selected script.;;All scripts are ran using: /script scriptname [params]. After running a script you can remove any traces of it with /script -unload scriptname, this would remove any summoned creatures, for example. It's good practise to do this after summoning creatures for a raid using scripts.;#/script #AdminMaker.class : For test servers only, summons an adminmaker to aid testing.;#/script #AdminSign.class #zone #x #y #text : Makes an AdminSign in zone at (x,y) with text. To put it next to you do /script AdminSign.class - - - text.;#/script #AlterQuest.class #player #questname #state : Update the quest for a player to be in a certain state. Omit #state to remove the quest.;#/script #DeepInspect.class #player : Deep inspects a player and all his/her items.;#/script #DropPlayerItems.class #player #[amount] #item : Drop the specified amount of items from the player if they are equipped in the bag or body.;#/script #EntitySearch.class #nonrespawn : Shows the locations of all creatures that don't respawn, for example creatures that were summoned by a GM, deathmatch creatures, etc.;#/script #FixDM.class #player : sets a player's DeathMatch slot to victory status.;#/script #ListNPCs.class : lists all npcs and their position.;#/script #LogoutPlayer.class #player : kicks a player from the game.;#/script #NPCShout.class #npc #text : NPC shouts text.;#/script #NPCShoutZone.class #npc #zone #text : NPC shouts text to players in given zone. Use - in place of zone to make it your current zone.;#/script #Plague.class #1 #creature : summon a plague of raid creatures around you.;#/script #WhereWho.class : Lists where all the online players are;#/script #Maria.class : Summons Maria, who sells food&drinks. Don't forget to -unload her after you're done.;#/script #ServerReset.class : use only in a real emergency to shut down server. If possible please warn the players to logout and give them some time. It kills the server the hard way.;#/script #ResetSlot.class #player #slot : Resets the named slot such as !kills or !quests. Useful for debugging.".split(";");
          } else {
            return !1;
          }
        }
      } else {
        return !1;
      }
    }
    for ($i$jscomp$61_params$jscomp$21$$ = 0; $i$jscomp$61_params$jscomp$21$$ < $msg$jscomp$19_type$jscomp$180$$.length; $i$jscomp$61_params$jscomp$21$$++) {
      $module$build$ts$util$Chat$default$$.$Chat$.log("info", $msg$jscomp$19_type$jscomp$180$$[$i$jscomp$61_params$jscomp$21$$]);
    }
    return !0;
  }, $minParams$:0, $maxParams$:1};
  this.ignore = {$execute$:function($action$jscomp$31_type$jscomp$181$$, $duration$jscomp$1_params$jscomp$22$$, $remainder$jscomp$19$$) {
    $action$jscomp$31_type$jscomp$181$$ = {type:"ignore"};
    if (null == $duration$jscomp$1_params$jscomp$22$$[0]) {
      $action$jscomp$31_type$jscomp$181$$.list = "1";
    } else {
      $action$jscomp$31_type$jscomp$181$$.target = $duration$jscomp$1_params$jscomp$22$$[0];
      $duration$jscomp$1_params$jscomp$22$$ = $duration$jscomp$1_params$jscomp$22$$[1];
      if (null != $duration$jscomp$1_params$jscomp$22$$ && "*" != $duration$jscomp$1_params$jscomp$22$$ && "-" != $duration$jscomp$1_params$jscomp$22$$) {
        if (isNaN(parseInt($duration$jscomp$1_params$jscomp$22$$, 10))) {
          return !1;
        }
        $action$jscomp$31_type$jscomp$181$$.duration = $duration$jscomp$1_params$jscomp$22$$;
      }
      0 != $remainder$jscomp$19$$.length && ($action$jscomp$31_type$jscomp$181$$.reason = $remainder$jscomp$19$$);
    }
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$31_type$jscomp$181$$);
    return !0;
  }, $minParams$:0, $maxParams$:2};
  this.inspectkill = {$execute$:function($creature$jscomp$1_type$jscomp$182$$, $params$jscomp$23_target$jscomp$109$$, $action$jscomp$32_remainder$jscomp$20$$) {
    $params$jscomp$23_target$jscomp$109$$ = $params$jscomp$23_target$jscomp$109$$[0];
    $creature$jscomp$1_type$jscomp$182$$ = null;
    null != $action$jscomp$32_remainder$jscomp$20$$ && "" != $action$jscomp$32_remainder$jscomp$20$$ && ($creature$jscomp$1_type$jscomp$182$$ = $action$jscomp$32_remainder$jscomp$20$$);
    $action$jscomp$32_remainder$jscomp$20$$ = {type:"inspectkill", target:$params$jscomp$23_target$jscomp$109$$};
    null != $creature$jscomp$1_type$jscomp$182$$ && ($action$jscomp$32_remainder$jscomp$20$$.creature = $creature$jscomp$1_type$jscomp$182$$);
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$32_remainder$jscomp$20$$);
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this.inspectquest = {$execute$:function($action$jscomp$33_type$jscomp$183$$, $params$jscomp$24$$) {
    $action$jscomp$33_type$jscomp$183$$ = {type:"inspectquest", target:$params$jscomp$24$$[0]};
    1 < $params$jscomp$24$$.length && ($action$jscomp$33_type$jscomp$183$$.quest_slot = $params$jscomp$24$$[1]);
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$33_type$jscomp$183$$);
    return !0;
  }, $minParams$:1, $maxParams$:2};
  this.jail = {$execute$:function($type$jscomp$184$$, $params$jscomp$25$$, $remainder$jscomp$22$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"jail", target:$params$jscomp$25$$[0], minutes:$params$jscomp$25$$[1], reason:$remainder$jscomp$22$$});
    return !0;
  }, $minParams$:2, $maxParams$:2};
  this.me = {$execute$:function($type$jscomp$185$$, $params$jscomp$26$$, $remainder$jscomp$23$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"emote", text:$remainder$jscomp$23$$});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.movecont = {$execute$:function() {
    var $enable$$ = !$JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "input.movecont"), $action$jscomp$36$$ = {type:"move.continuous",};
    $enable$$ && ($action$jscomp$36$$["move.continuous"] = "");
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$36$$);
    $stendhal$$.$config$.set("input.movecont", $enable$$);
    $module$build$ts$util$Chat$default$$.$Chat$.log("info", "Continuous movement " + ($enable$$ ? "enabled" : "disabled") + ".");
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.tell = this.msg = {$execute$:function($type$jscomp$187$$, $params$jscomp$28$$, $remainder$jscomp$25$$) {
    $$jscomp$this$jscomp$11$$.$v$ = $params$jscomp$28$$[0];
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"tell", target:$params$jscomp$28$$[0], text:$remainder$jscomp$25$$});
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this["/"] = {$execute$:function($type$jscomp$188$$, $params$jscomp$29$$, $remainder$jscomp$26$$) {
    "undefined" != typeof $$jscomp$this$jscomp$11$$.$v$ && $$jscomp$this$jscomp$11$$.$sendAction$({type:"tell", target:$$jscomp$this$jscomp$11$$.$v$, text:$remainder$jscomp$26$$});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.mute = {$execute$:function() {
    $stendhal$$.$main$.$toggleSound$();
    $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "ui.sound") ? $module$build$ts$util$Chat$default$$.$Chat$.log("info", "Sounds are now on.") : $module$build$ts$util$Chat$default$$.$Chat$.log("info", "Sounds are now off.");
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.p = {$execute$:function($type$jscomp$190$$, $params$jscomp$31$$, $remainder$jscomp$28$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"group_message", text:$remainder$jscomp$28$$});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.progressstatus = {$execute$:function($action$jscomp$40_type$jscomp$191$$, $params$jscomp$32$$, $remainder$jscomp$29$$) {
    $action$jscomp$40_type$jscomp$191$$ = {type:$action$jscomp$40_type$jscomp$191$$};
    0 < $remainder$jscomp$29$$.length && (-1 < $remainder$jscomp$29$$.indexOf("Open Quests") ? ($action$jscomp$40_type$jscomp$191$$.progress_type = "Open Quests", $remainder$jscomp$29$$ = $remainder$jscomp$29$$.substring(12)) : -1 < $remainder$jscomp$29$$.indexOf("Completed Quests") ? ($action$jscomp$40_type$jscomp$191$$.progress_type = "Completed Quests", $remainder$jscomp$29$$ = $remainder$jscomp$29$$.substring(17)) : -1 < $remainder$jscomp$29$$.indexOf("Production") && ($action$jscomp$40_type$jscomp$191$$.progress_type = 
    "Production", $remainder$jscomp$29$$ = $remainder$jscomp$29$$.substring(11)), $remainder$jscomp$29$$ && ($action$jscomp$40_type$jscomp$191$$.item = $remainder$jscomp$29$$));
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$40_type$jscomp$191$$);
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.remove = {$execute$:function($type$jscomp$192$$, $params$jscomp$33$$) {
    if (null == $params$jscomp$33$$) {
      return !1;
    }
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"removebuddy", target:$params$jscomp$33$$[0]});
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this.screenshot = {$execute$:function() {
    var $JSCompiler_StaticMethods_createScreenshot$self$jscomp$inline_432_uri$jscomp$inline_433$$ = $stendhal$$.$ui$.$gamewindow$;
    $module$build$ts$util$Chat$default$$.$Chat$.log("client", "creating screenshot ...");
    $JSCompiler_StaticMethods_createScreenshot$self$jscomp$inline_432_uri$jscomp$inline_433$$ = $JSCompiler_StaticMethods_createScreenshot$self$jscomp$inline_432_uri$jscomp$inline_433$$.$ctx$.canvas.toDataURL("image/png");
    var $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$ = new Date(), $JSCompiler_object_inline_yyyy_132$jscomp$inline_435$$ = "" + $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.getFullYear(), $JSCompiler_object_inline_mm_133$jscomp$inline_436$$ = ("00" + ($JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.getMonth() + 1)).slice(-2), $JSCompiler_object_inline_dd_134$jscomp$inline_437$$ = 
    ("00" + $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.getDate()).slice(-2), $JSCompiler_object_inline_HH_135$jscomp$inline_438$$ = ("00" + $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.getHours()).slice(-2), $JSCompiler_object_inline_MM_136$jscomp$inline_439$$ = ("00" + $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.getMinutes()).slice(-2), 
    $JSCompiler_object_inline_SS_137$jscomp$inline_440$$ = ("00" + $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.getSeconds()).slice(-2);
    for ($JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$ = "" + $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.getMilliseconds(); 3 > $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.length;) {
      $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$ = "0" + $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$;
    }
    $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$ = document.createElement("a");
    $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.download = "stendhal-" + $JSCompiler_object_inline_yyyy_132$jscomp$inline_435$$ + "." + $JSCompiler_object_inline_mm_133$jscomp$inline_436$$ + "." + $JSCompiler_object_inline_dd_134$jscomp$inline_437$$ + "-" + $JSCompiler_object_inline_HH_135$jscomp$inline_438$$ + "." + $JSCompiler_object_inline_MM_136$jscomp$inline_439$$ + "." + $JSCompiler_object_inline_SS_137$jscomp$inline_440$$ + ".png";
    $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.target = "_blank";
    $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.href = $JSCompiler_StaticMethods_createScreenshot$self$jscomp$inline_432_uri$jscomp$inline_433$$;
    $JSCompiler_object_inline_ms_138$jscomp$inline_441_anchor$jscomp$inline_442_d$jscomp$inline_434$$.click();
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.sentence = {$execute$:function($type$jscomp$194$$, $params$jscomp$35$$, $remainder$jscomp$32$$) {
    if (null == $params$jscomp$35$$) {
      return !1;
    }
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"sentence", value:$remainder$jscomp$32$$});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.settings = new $module$build$ts$action$SettingsAction$default$$.$SettingsAction$();
  this.stopwalk = {$execute$:function() {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"walk", mode:"stop"});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.volume = {$execute$:function($$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$, $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$) {
    $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ = $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$[0];
    $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ = $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$[1];
    if ("undefined" === typeof $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$) {
      for ($$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ = ["master"].concat($$jscomp$arrayFromIterable$$($stendhal$$.$ui$.$soundMan$.$layers$)), $module$build$ts$util$Chat$default$$.$Chat$.log("info", "Please use /volume <layer> <value> to adjust the volume."), $module$build$ts$util$Chat$default$$.$Chat$.log("client", '<layer> is one of "' + $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$.join('", "') + '"'), $module$build$ts$util$Chat$default$$.$Chat$.log("client", "<value> is a number in the range 0 to 100."), 
      $module$build$ts$util$Chat$default$$.$Chat$.log("client", "Current volume levels:"), $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ = $$jscomp$makeIterator$$($$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$), $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ = $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$.next(); !$$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$.done; $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ = 
      $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$.next()) {
        $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ = $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$.value, $module$build$ts$util$Chat$default$$.$Chat$.log("client", "&nbsp;&nbsp;- " + $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ + 
        " -> " + 100 * $JSCompiler_StaticMethods_getVolume$$($$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$));
      }
    } else {
      if ("undefined" !== typeof $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$) {
        if (!/^\d+$/.test($$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$)) {
          return $module$build$ts$util$Chat$default$$.$Chat$.log("error", "Value must be a number."), !0;
        }
        var $$jscomp$inline_450_vol$jscomp$inline_447$$ = parseInt($$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$, 10) / 100;
        $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ = $stendhal$$.$ui$.$soundMan$;
        var $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$ = $JSCompiler_StaticMethods_getFloat$$($stendhal$$.$config$, "ui.sound." + $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ + ".volume");
        if ("undefined" === typeof $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$ || "" === $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$) {
          $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ = !1;
        } else {
          $stendhal$$.$config$.set("ui.sound." + $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ + ".volume", $JSCompiler_StaticMethods_normVolume$$($$jscomp$inline_450_vol$jscomp$inline_447$$));
          $$jscomp$inline_450_vol$jscomp$inline_447$$ = $$jscomp$makeIterator$$("master" === $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ ? $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$.$layers$ : [$$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$]);
          for ($$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$ = $$jscomp$inline_450_vol$jscomp$inline_447$$.next(); !$$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$.done; $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$ = $$jscomp$inline_450_vol$jscomp$inline_447$$.next()) {
            if ($$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$ = $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$.active[$$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$.value], "undefined" !== typeof $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$) {
              $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$ = $$jscomp$makeIterator$$($$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$);
              for (var $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$ = $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$.next(); !$$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.done; $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$ = $$jscomp$inline_451_$jscomp$inline_453_layersounds$jscomp$inline_452_oldvol$jscomp$inline_449$$.next()) {
                $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$ = $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.value, "number" === typeof $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.$radius$ && "number" === typeof $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.x && "number" === typeof $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.y ? $JSCompiler_StaticMethods_adjustForDistance$$($$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$, 
                $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$, $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.$radius$, $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.x, $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.y, $marauroa$$.$me$._x, $marauroa$$.$me$._y) : $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.volume = $JSCompiler_StaticMethods_normVolume$$($JSCompiler_StaticMethods_getAdjustedVolume$$($$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$, 
                $$jscomp$key$snd$jscomp$inline_454_snd$jscomp$inline_455$$.$basevolume$));
              }
            }
          }
          $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ = !0;
        }
        $$jscomp$key$l$jscomp$1_JSCompiler_StaticMethods_setVolume$self$jscomp$inline_448_JSCompiler_inline_result$jscomp$153_l$jscomp$16_params$jscomp$37_vol$jscomp$3$$ ? $module$build$ts$util$Chat$default$$.$Chat$.log("client", 'Channel "' + $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ + '" volume set to ' + 100 * $JSCompiler_StaticMethods_getVolume$$($$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$) + ".") : $module$build$ts$util$Chat$default$$.$Chat$.log("error", 'Unknown layer "' + 
        $$jscomp$iter$31_layername$jscomp$9_layers_type$jscomp$196$$ + '".');
      } else {
        $module$build$ts$util$Chat$default$$.$Chat$.log("error", "Please use /volume for help.");
      }
    }
    return !0;
  }, $minParams$:0, $maxParams$:2};
  this.summon = {$execute$:function($type$jscomp$197$$, $creature$jscomp$2_params$jscomp$38$$) {
    for (var $x$jscomp$106$$ = null, $y$jscomp$86$$ = null, $quantity$jscomp$3$$ = null, $nameBuilder$$ = [], $idx$jscomp$10$$ = 0; $idx$jscomp$10$$ < $creature$jscomp$2_params$jscomp$38$$.length; $idx$jscomp$10$$++) {
      var $str$jscomp$7$$ = $creature$jscomp$2_params$jscomp$38$$[$idx$jscomp$10$$];
      $str$jscomp$7$$.match("[0-9].*") ? null == $x$jscomp$106$$ ? $x$jscomp$106$$ = $str$jscomp$7$$ : null == $y$jscomp$86$$ ? $y$jscomp$86$$ = $str$jscomp$7$$ : null == $quantity$jscomp$3$$ ? $quantity$jscomp$3$$ = $str$jscomp$7$$ : $nameBuilder$$.push($str$jscomp$7$$) : $nameBuilder$$.push($str$jscomp$7$$);
    }
    null == $quantity$jscomp$3$$ && null == $y$jscomp$86$$ && null != $x$jscomp$106$$ && ($quantity$jscomp$3$$ = $x$jscomp$106$$, $x$jscomp$106$$ = null);
    $creature$jscomp$2_params$jscomp$38$$ = $nameBuilder$$.join(" ");
    if (null == $x$jscomp$106$$ || null == $y$jscomp$86$$) {
      $x$jscomp$106$$ = $marauroa$$.$me$.x.toString(), $y$jscomp$86$$ = $marauroa$$.$me$.y.toString();
    }
    $$jscomp$this$jscomp$11$$.$sendAction$({type:$type$jscomp$197$$, creature:$creature$jscomp$2_params$jscomp$38$$, x:$x$jscomp$106$$, y:$y$jscomp$86$$, quantity:$quantity$jscomp$3$$ || "0"});
    return !0;
  }, $minParams$:1, $maxParams$:-1};
  this.summonat = {$execute$:function($type$jscomp$198$$, $params$jscomp$39$$, $remainder$jscomp$36$$) {
    var $amount$$ = $params$jscomp$39$$[2];
    isNaN(parseInt($amount$$, 10)) && ($remainder$jscomp$36$$ ? $remainder$jscomp$36$$ = $amount$$ + " " + $remainder$jscomp$36$$ : $remainder$jscomp$36$$ = $amount$$, $amount$$ = "1");
    $$jscomp$this$jscomp$11$$.$sendAction$({type:$type$jscomp$198$$, target:$params$jscomp$39$$[0], slot:$params$jscomp$39$$[1], amount:$amount$$, item:$remainder$jscomp$36$$});
    return !0;
  }, $minParams$:3, $maxParams$:3};
  this.support = {$execute$:function($type$jscomp$199$$, $params$jscomp$40$$, $remainder$jscomp$37$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"support", text:$remainder$jscomp$37$$});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.supporta = this.supportanswer = {$execute$:function($type$jscomp$200$$, $params$jscomp$41$$, $remainder$jscomp$38$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"supportanswer", target:$params$jscomp$41$$[0], text:$remainder$jscomp$38$$});
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this.teleport = {$execute$:function($type$jscomp$201$$, $params$jscomp$42$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"teleport", target:$params$jscomp$42$$[0], zone:$params$jscomp$42$$[1], x:$params$jscomp$42$$[2], y:$params$jscomp$42$$[3]});
    return !0;
  }, $minParams$:4, $maxParams$:4};
  this.teleportto = {$execute$:function($type$jscomp$202$$, $params$jscomp$43$$, $remainder$jscomp$40$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"teleportto", target:$remainder$jscomp$40$$,});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.tellall = {$execute$:function($type$jscomp$203$$, $params$jscomp$44$$, $remainder$jscomp$41$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"tellall", text:$remainder$jscomp$41$$});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.walk = {$execute$:function() {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"walk"});
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.atlas = {$execute$:function() {
    window.location.href = "https://stendhalgame.org/world/atlas.html?me=" + $marauroa$$.$currentZoneName$ + "." + $marauroa$$.$me$.x + "." + $marauroa$$.$me$.y;
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.beginnersguide = {$execute$:function() {
    window.location.href = "https://stendhalgame.org/wiki/Stendhal_Beginner's_Guide";
    return !0;
  }, $minParams$:0, $maxParams$:0};
  this.characterselector = new $module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$("https://stendhalgame.org/account/mycharacters.html");
  this.faq = new $module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$("https://stendhalgame.org/wiki/Stendhal_FAQ");
  this.manual = new $module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$("https://stendhalgame.org/wiki/Stendhal_Manual/Controls_and_Game_Settings");
  this.profile = {$execute$:function($type$jscomp$207_url$jscomp$24$$, $params$jscomp$48$$) {
    $type$jscomp$207_url$jscomp$24$$ = "https://stendhalgame.org/character/";
    var $JSCompiler_temp$jscomp$162_name$jscomp$78$$;
    ($JSCompiler_temp$jscomp$162_name$jscomp$78$$ = $marauroa$$.$me$._name) || ($JSCompiler_temp$jscomp$162_name$jscomp$78$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$F$().$charname$ || "");
    0 < $params$jscomp$48$$.length && null != $params$jscomp$48$$[0] && ($JSCompiler_temp$jscomp$162_name$jscomp$78$$ = $params$jscomp$48$$[0]);
    if (!$JSCompiler_temp$jscomp$162_name$jscomp$78$$) {
      return console.warn("failed to get default character name!"), !0;
    }
    $type$jscomp$207_url$jscomp$24$$ += $JSCompiler_temp$jscomp$162_name$jscomp$78$$ + ".html";
    $module$build$ts$util$Chat$default$$.$Chat$.log("info", "Trying to open #" + $type$jscomp$207_url$jscomp$24$$ + " in your browser.");
    window.location.href = $type$jscomp$207_url$jscomp$24$$;
    return !0;
  }, $minParams$:0, $maxParams$:1};
  this.rules = new $module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$("https://stendhalgame.org/wiki/Stendhal_Rules");
  this.changepassword = new $module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$("https://stendhalgame.org/account/change-password.html");
  this.loginhistory = new $module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$("https://stendhalgame.org/account/history.html");
  this.halloffame = new $module$build$ts$action$OpenWebsiteAction$default$$.$OpenWebsiteAction$("https://stendhalgame.org/world/hall-of-fame/active_overview.html");
  this.storemessage = {$execute$:function($type$jscomp$208$$, $params$jscomp$49$$, $remainder$jscomp$46$$) {
    $$jscomp$this$jscomp$11$$.$sendAction$({type:"storemessage", target:$params$jscomp$49$$[0], text:$remainder$jscomp$46$$});
    return !0;
  }, $minParams$:1, $maxParams$:1};
  this._default = {$execute$:function($action$jscomp$53_type$jscomp$209$$, $params$jscomp$50$$, $remainder$jscomp$47$$) {
    $action$jscomp$53_type$jscomp$209$$ = {type:$action$jscomp$53_type$jscomp$209$$};
    typeof("undefined" != $params$jscomp$50$$[0]) && ($action$jscomp$53_type$jscomp$209$$.target = $params$jscomp$50$$[0], "" != $remainder$jscomp$47$$ && ($action$jscomp$53_type$jscomp$209$$.args = $remainder$jscomp$47$$));
    $$jscomp$this$jscomp$11$$.$sendAction$($action$jscomp$53_type$jscomp$209$$);
    return !0;
  }, $minParams$:0, $maxParams$:1};
};
$module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$.get = function() {
  $module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$.$v$ || ($module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$.$v$ = new $module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$());
  return $module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$.$v$;
};
$module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$.prototype.$sendAction$ = function($action$jscomp$54$$) {
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$54$$);
};
$module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$.prototype.$execute$ = function($array$jscomp$10_line$$) {
  $array$jscomp$10_line$$ = $array$jscomp$10_line$$.trim();
  $array$jscomp$10_line$$.startsWith("//") && !$array$jscomp$10_line$$.startsWith("// ") && ($array$jscomp$10_line$$ = "// " + $array$jscomp$10_line$$.substring(2));
  $array$jscomp$10_line$$ = $array$jscomp$10_line$$.split(" ");
  for (var $el_name$jscomp$79$$ in $array$jscomp$10_line$$) {
    $array$jscomp$10_line$$[$el_name$jscomp$79$$] = $array$jscomp$10_line$$[$el_name$jscomp$79$$].trim();
  }
  $array$jscomp$10_line$$ = $array$jscomp$10_line$$.filter(Boolean);
  if (0 == $array$jscomp$10_line$$.length) {
    return !1;
  }
  $el_name$jscomp$79$$ = $array$jscomp$10_line$$[0];
  "/" != $el_name$jscomp$79$$[0] ? $el_name$jscomp$79$$ = "/chat" : $array$jscomp$10_line$$.shift();
  $el_name$jscomp$79$$ = $el_name$jscomp$79$$.substr(1);
  var $action$jscomp$55$$ = "undefined" == typeof this[$el_name$jscomp$79$$] ? this._default : this[$el_name$jscomp$79$$];
  "where" == $el_name$jscomp$79$$ && 0 == $array$jscomp$10_line$$.length && ($array$jscomp$10_line$$[0] = $marauroa$$.$me$._name);
  if ($action$jscomp$55$$.$minParams$ <= $array$jscomp$10_line$$.length) {
    for (var $remainder$jscomp$48$$ = "", $i$jscomp$62$$ = $action$jscomp$55$$.$maxParams$; $i$jscomp$62$$ < $array$jscomp$10_line$$.length; $i$jscomp$62$$++) {
      $remainder$jscomp$48$$ = $remainder$jscomp$48$$ + $array$jscomp$10_line$$[$i$jscomp$62$$] + " ";
    }
    return $action$jscomp$55$$.$execute$($el_name$jscomp$79$$, $array$jscomp$10_line$$, $remainder$jscomp$48$$.trim());
  }
  $module$build$ts$util$Chat$default$$.$Chat$.log("error", "Missing arguments. Try /help");
  return !1;
};
var $module$build$ts$util$ConfigManager$default$$ = {};
Object.defineProperty($module$build$ts$util$ConfigManager$default$$, "__esModule", {value:!0});
$module$build$ts$util$ConfigManager$default$$.$ConfigManager$ = void 0;
$module$build$ts$util$ConfigManager$default$$.$ConfigManager$ = function() {
  this.$C$ = {"client.pathfinding":"true", "ui.sound":"false", "ui.sound.master.volume":"100", "ui.sound.ambient.volume":"100", "ui.sound.creature.volume":"100", "ui.sound.gui.volume":"100", "ui.sound.music.volume":"100", "ui.sound.sfx.volume":"100", "ui.font.body":"Carlito", "ui.font.chat":"Carlito", "ui.font.tlog":"Black Chancery", "ui.stats.charname":"true", "ui.stats.hpbar":"true", "ui.joystick":"none", "ui.joystick.center.x":"224", "ui.joystick.center.y":"384", "ui.window.chest":"160,370", "ui.window.corpse":"160,370", 
  "ui.window.menu":"150,20", "ui.window.outfit":"300,50", "ui.window.settings":"20,20", "ui.window.shortcuts":"20,20", "ui.window.trade":"200,100", "ui.window.travellog":"160,50", "gamescreen.blood":"true", "gamescreen.lighting":"true", "gamescreen.weather":"true", "gamescreen.nonude":"false", "gamescreen.shadows":"true", "gamescreen.speech.creature":"true", "input.click.indicator":"false", "input.movecont":"false", "action.item.doubleclick":"false", "action.inventory.quickpickup":"true", "event.pvtmsg.sound":"ui/notify_up", 
  "chat.custom_keywords":"",};
  this.$themes$ = {map:{aubergine:"panel_aubergine.png", brick:"panel_brick.png", honeycomb:"panel_honeycomb.png", leather:"panel_leather.png", metal:"panel_metal.png", parquet:"panel_wood_parquet.png", stone:"panel_stone.png", tile:"panel_aqua_tile.png", wood:"panel_wood_v.png", wood2:"panel_wood_h.png"}, $dark$:{aubergine:!0, brick:!0, leather:!0, metal:!0, parquet:!0, stone:!0, tile:!0, wood:!0, wood2:!0}};
  this.fonts = {"sans-serif":"system default", serif:"system default (serif)", Amaranth:"", "Black Chancery":"", Carlito:""};
  this.$v$ = window.localStorage;
  this.$A$ = {};
  this.$B$ = !1;
};
$module$build$ts$util$ConfigManager$default$$.$ConfigManager$.get = function() {
  $module$build$ts$util$ConfigManager$default$$.$ConfigManager$.$v$ || ($module$build$ts$util$ConfigManager$default$$.$ConfigManager$.$v$ = new $module$build$ts$util$ConfigManager$default$$.$ConfigManager$());
  return $module$build$ts$util$ConfigManager$default$$.$ConfigManager$.$v$;
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$util$ConfigManager$default$$.$ConfigManager$.prototype;
$JSCompiler_prototypeAlias$$.$init$ = function() {
  this.$B$ ? console.warn("tried to re-initialize ConfigManager") : this.$B$ = !0;
};
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$66$$, $value$jscomp$109$$) {
  "object" === typeof $value$jscomp$109$$ && ($value$jscomp$109$$ = JSON.stringify($value$jscomp$109$$));
  this.$v$.setItem($key$jscomp$66$$, $value$jscomp$109$$);
};
$JSCompiler_prototypeAlias$$.get = function($key$jscomp$67_ret$$) {
  $key$jscomp$67_ret$$ = this.$v$.getItem($key$jscomp$67_ret$$) || this.$C$[$key$jscomp$67_ret$$];
  return "null" === $key$jscomp$67_ret$$ ? null : $key$jscomp$67_ret$$;
};
function $JSCompiler_StaticMethods_getInt$$($JSCompiler_StaticMethods_getInt$self_value$jscomp$110$$, $key$jscomp$68$$, $dval$$) {
  $JSCompiler_StaticMethods_getInt$self_value$jscomp$110$$ = $JSCompiler_StaticMethods_getFloat$$($JSCompiler_StaticMethods_getInt$self_value$jscomp$110$$, $key$jscomp$68$$);
  if ("undefined" === typeof $JSCompiler_StaticMethods_getInt$self_value$jscomp$110$$) {
    if ("undefined" === typeof $dval$$) {
      return;
    }
    $JSCompiler_StaticMethods_getInt$self_value$jscomp$110$$ = $dval$$;
  }
  return Math.trunc($JSCompiler_StaticMethods_getInt$self_value$jscomp$110$$);
}
function $JSCompiler_StaticMethods_getFloat$$($JSCompiler_StaticMethods_getFloat$self_value$jscomp$111$$, $key$jscomp$69$$) {
  $JSCompiler_StaticMethods_getFloat$self_value$jscomp$111$$ = Number($JSCompiler_StaticMethods_getFloat$self_value$jscomp$111$$.get($key$jscomp$69$$));
  if (!Number.isNaN($JSCompiler_StaticMethods_getFloat$self_value$jscomp$111$$)) {
    return $JSCompiler_StaticMethods_getFloat$self_value$jscomp$111$$;
  }
}
function $JSCompiler_StaticMethods_getBoolean$$($JSCompiler_StaticMethods_getBoolean$self_value$jscomp$112$$, $key$jscomp$70$$) {
  return ($JSCompiler_StaticMethods_getBoolean$self_value$jscomp$112$$ = $JSCompiler_StaticMethods_getBoolean$self_value$jscomp$112$$.get($key$jscomp$70$$)) ? "true" === $JSCompiler_StaticMethods_getBoolean$self_value$jscomp$112$$.toLowerCase() : !1;
}
function $JSCompiler_StaticMethods_getObject$$() {
  var $value$jscomp$113$$ = $config$$module$build$ts$ui$component$ChatInputComponent$$.get("chat.history");
  if ($value$jscomp$113$$ && ($value$jscomp$113$$ = JSON.parse($value$jscomp$113$$), "object" === typeof $value$jscomp$113$$)) {
    return $value$jscomp$113$$;
  }
}
$JSCompiler_prototypeAlias$$.remove = function($key$jscomp$72$$) {
  this.$v$.removeItem($key$jscomp$72$$);
};
$JSCompiler_prototypeAlias$$.clear = function() {
  this.$v$.clear();
};
function $JSCompiler_StaticMethods_getWindowState$$($id$jscomp$24_tmp$jscomp$6$$) {
  var $JSCompiler_StaticMethods_getWindowState$self$$ = $stendhal$$.$config$, $state$jscomp$1$$ = {};
  $JSCompiler_StaticMethods_getWindowState$self$$.$A$.hasOwnProperty($id$jscomp$24_tmp$jscomp$6$$) ? $state$jscomp$1$$ = $JSCompiler_StaticMethods_getWindowState$self$$.$A$[$id$jscomp$24_tmp$jscomp$6$$] : ($id$jscomp$24_tmp$jscomp$6$$ = ($JSCompiler_StaticMethods_getWindowState$self$$.get("ui.window." + $id$jscomp$24_tmp$jscomp$6$$) || "0,0").split(","), $state$jscomp$1$$.x = parseInt($id$jscomp$24_tmp$jscomp$6$$[0], 10), $state$jscomp$1$$.y = parseInt($id$jscomp$24_tmp$jscomp$6$$[1], 10));
  return $state$jscomp$1$$;
}
function $JSCompiler_StaticMethods_getTheme$$($JSCompiler_StaticMethods_getTheme$self$$) {
  return $JSCompiler_StaticMethods_getTheme$self$$.get("ui.theme") || "wood";
}
function $JSCompiler_StaticMethods_applyTheme$$($JSCompiler_StaticMethods_applyTheme$self$$, $element$jscomp$11$$, $children$jscomp$2_idx$jscomp$11$$, $recurse$$, $updateBG$$) {
  $children$jscomp$2_idx$jscomp$11$$ = void 0 === $children$jscomp$2_idx$jscomp$11$$ ? !1 : $children$jscomp$2_idx$jscomp$11$$;
  $recurse$$ = void 0 === $recurse$$ ? !1 : $recurse$$;
  $updateBG$$ = void 0 === $updateBG$$ ? !1 : $updateBG$$;
  var $current$$ = $JSCompiler_StaticMethods_getTheme$$($JSCompiler_StaticMethods_applyTheme$self$$);
  $element$jscomp$11$$.style.setProperty("background", "url(" + $stendhal$$.$paths$.$gui$ + "/" + $JSCompiler_StaticMethods_applyTheme$self$$.$themes$.map[$current$$] + ")");
  var $color$jscomp$2$$ = "#000000", $colorbg$$ = "#ffffff";
  $JSCompiler_StaticMethods_applyTheme$self$$.$themes$.$dark$[$current$$] && ($color$jscomp$2$$ = "#ffffff", $colorbg$$ = "#000000");
  $element$jscomp$11$$.style.setProperty("color", $color$jscomp$2$$);
  $updateBG$$ && $element$jscomp$11$$.style.setProperty("background-color", $colorbg$$);
  if ($children$jscomp$2_idx$jscomp$11$$ && $element$jscomp$11$$.children) {
    for ($children$jscomp$2_idx$jscomp$11$$ = 0; $children$jscomp$2_idx$jscomp$11$$ < $element$jscomp$11$$.children.length; $children$jscomp$2_idx$jscomp$11$$++) {
      $JSCompiler_StaticMethods_applyTheme$$($JSCompiler_StaticMethods_applyTheme$self$$, $element$jscomp$11$$.children[$children$jscomp$2_idx$jscomp$11$$], $recurse$$, $recurse$$);
    }
  }
}
function $JSCompiler_StaticMethods_refreshTheme$$() {
  var $JSCompiler_StaticMethods_refreshTheme$self_buddyList$$ = $stendhal$$.$config$;
  var $current$jscomp$1_updateBG$jscomp$1$$ = void 0 === $current$jscomp$1_updateBG$jscomp$1$$ ? !1 : $current$jscomp$1_updateBG$jscomp$1$$;
  for (var $$jscomp$iter$33_rootStyle$$ = $$jscomp$makeIterator$$(Array.from(document.querySelectorAll(".background"))), $$jscomp$key$elem$$ = $$jscomp$iter$33_rootStyle$$.next(); !$$jscomp$key$elem$$.done; $$jscomp$key$elem$$ = $$jscomp$iter$33_rootStyle$$.next()) {
    $JSCompiler_StaticMethods_applyTheme$$($JSCompiler_StaticMethods_refreshTheme$self_buddyList$$, $$jscomp$key$elem$$.value, void 0, void 0, $current$jscomp$1_updateBG$jscomp$1$$);
  }
  $current$jscomp$1_updateBG$jscomp$1$$ = $JSCompiler_StaticMethods_getTheme$$($JSCompiler_StaticMethods_refreshTheme$self_buddyList$$);
  $$jscomp$iter$33_rootStyle$$ = document.documentElement.style;
  $$jscomp$iter$33_rootStyle$$.setProperty("--background-url", "url(" + $stendhal$$.$paths$.$gui$ + "/" + $JSCompiler_StaticMethods_refreshTheme$self_buddyList$$.$themes$.map[$current$jscomp$1_updateBG$jscomp$1$$] + ")");
  1 == $JSCompiler_StaticMethods_refreshTheme$self_buddyList$$.$themes$.$dark$[$JSCompiler_StaticMethods_getTheme$$($JSCompiler_StaticMethods_refreshTheme$self_buddyList$$)] ? ($$jscomp$iter$33_rootStyle$$.setProperty("--text-color", "#fff"), $$jscomp$iter$33_rootStyle$$.setProperty("--text-color-inactive", "#aaa"), $$jscomp$iter$33_rootStyle$$.setProperty("--text-color-online", "#0a0"), $$jscomp$iter$33_rootStyle$$.setProperty("--text-color-offline", "#aaa")) : ($$jscomp$iter$33_rootStyle$$.setProperty("--text-color", 
  "#000"), $$jscomp$iter$33_rootStyle$$.setProperty("--text-color-inactive", "#555"), $$jscomp$iter$33_rootStyle$$.setProperty("--text-color-online", "#070"), $$jscomp$iter$33_rootStyle$$.setProperty("--text-color-offline", "#777"));
  ($JSCompiler_StaticMethods_refreshTheme$self_buddyList$$ = $module$build$ts$ui$UI$default$$.$ui$.get(102)) && $JSCompiler_StaticMethods_refreshTheme$self_buddyList$$.update();
}
;var $module$build$ts$util$SessionManager$default$$ = {};
Object.defineProperty($module$build$ts$util$SessionManager$default$$, "__esModule", {value:!0});
$module$build$ts$util$SessionManager$default$$.$SessionManager$ = void 0;
$module$build$ts$util$SessionManager$default$$.$SessionManager$ = function() {
  this.$A$ = window.sessionStorage;
  this.$v$ = !1;
  this.$server_default$ = !0;
};
$module$build$ts$util$SessionManager$default$$.$SessionManager$.get = function() {
  $module$build$ts$util$SessionManager$default$$.$SessionManager$.$v$ || ($module$build$ts$util$SessionManager$default$$.$SessionManager$.$v$ = new $module$build$ts$util$SessionManager$default$$.$SessionManager$());
  return $module$build$ts$util$SessionManager$default$$.$SessionManager$.$v$;
};
$module$build$ts$util$SessionManager$default$$.$SessionManager$.prototype.$init$ = function($args$jscomp$4_server$$) {
  if (this.$v$) {
    console.warn("tried to re-initialize SessionManager");
  } else {
    this.$v$ = !0;
    var $charname$jscomp$3$$ = $args$jscomp$4_server$$.get("char") || $args$jscomp$4_server$$.get("character") || $args$jscomp$4_server$$.get("name");
    $charname$jscomp$3$$ && $JSCompiler_StaticMethods_setCharName$$(this, $charname$jscomp$3$$);
    ($args$jscomp$4_server$$ = $args$jscomp$4_server$$.get("server")) && "/testdata" === $stendhal$$.$paths$.data && (this.$server_default$ = "main" !== $args$jscomp$4_server$$);
  }
};
$module$build$ts$util$SessionManager$default$$.$SessionManager$.prototype.get = function($key$jscomp$73$$) {
  return this.$A$.getItem($key$jscomp$73$$);
};
$module$build$ts$util$SessionManager$default$$.$SessionManager$.prototype.set = function($key$jscomp$74$$, $value$jscomp$115$$) {
  this.$A$.setItem($key$jscomp$74$$, $value$jscomp$115$$);
};
function $JSCompiler_StaticMethods_setCharName$$($JSCompiler_StaticMethods_setCharName$self$$, $charname$jscomp$4$$) {
  $JSCompiler_StaticMethods_setCharName$self$$.$charname$ = $charname$jscomp$4$$;
  document.title = "Stendhal - " + $JSCompiler_StaticMethods_setCharName$self$$.$charname$;
  document.getElementById("charname").innerText = $JSCompiler_StaticMethods_setCharName$self$$.$charname$;
}
;var $module$build$ts$data$Paths$default$$ = {};
Object.defineProperty($module$build$ts$data$Paths$default$$, "__esModule", {value:!0});
$module$build$ts$data$Paths$default$$.$Paths$ = void 0;
$module$build$ts$data$Paths$default$$.$Paths$ = function() {
};
$module$build$ts$data$Paths$default$$.$Paths$.$A$ = function($ref$jscomp$2$$) {
  var $path$jscomp$6$$ = document.getElementsByTagName("html")[0].getAttribute($ref$jscomp$2$$);
  if (!$path$jscomp$6$$ || !$path$jscomp$6$$.startsWith("/")) {
    throw Error("Path reference " + $ref$jscomp$2$$ + " is not a relative path.");
  }
  return $path$jscomp$6$$;
};
$module$build$ts$data$Paths$default$$.$Paths$.data = $module$build$ts$data$Paths$default$$.$Paths$.$A$("data-data-path");
$module$build$ts$data$Paths$default$$.$Paths$.font = $module$build$ts$data$Paths$default$$.$Paths$.data + "/font";
$module$build$ts$data$Paths$default$$.$Paths$.$gui$ = $module$build$ts$data$Paths$default$$.$Paths$.data + "/gui";
$module$build$ts$data$Paths$default$$.$Paths$.$music$ = $module$build$ts$data$Paths$default$$.$Paths$.data + "/music";
$module$build$ts$data$Paths$default$$.$Paths$.$sounds$ = $module$build$ts$data$Paths$default$$.$Paths$.data + "/sounds";
$module$build$ts$data$Paths$default$$.$Paths$.$sprites$ = $module$build$ts$data$Paths$default$$.$Paths$.data + "/sprites";
$module$build$ts$data$Paths$default$$.$Paths$.$weather$ = $module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/weather";
$module$build$ts$data$Paths$default$$.$Paths$.$achievements$ = $module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/achievements";
$module$build$ts$data$Paths$default$$.$Paths$.$v$ = $module$build$ts$data$Paths$default$$.$Paths$.$A$("data-tileset-path");
$module$build$ts$data$Paths$default$$.$Paths$.$B$ = $module$build$ts$data$Paths$default$$.$Paths$.$A$("data-ws");
var $module$build$ts$util$JSONLoader$default$$ = {};
Object.defineProperty($module$build$ts$util$JSONLoader$default$$, "__esModule", {value:!0});
$module$build$ts$util$JSONLoader$default$$.$JSONLoader$ = void 0;
$module$build$ts$util$JSONLoader$default$$.$JSONLoader$ = function($onDataReady$$) {
  this.$v$ = $onDataReady$$;
};
$module$build$ts$util$JSONLoader$default$$.$JSONLoader$.prototype.load = function($path$jscomp$7$$) {
  var $$jscomp$this$jscomp$12$$ = this;
  fetch($path$jscomp$7$$, {headers:{"Content-Type":"application/json"}}).then(function($resp$$) {
    return $resp$$.json();
  }).then(function($data$jscomp$82$$) {
    $$jscomp$this$jscomp$12$$.data = $data$jscomp$82$$;
    "undefined" !== typeof $$jscomp$this$jscomp$12$$.$v$ && $$jscomp$this$jscomp$12$$.$v$();
  });
};
var $module$build$ts$data$TileStore$default$$ = {};
Object.defineProperty($module$build$ts$data$TileStore$default$$, "__esModule", {value:!0});
$module$build$ts$data$TileStore$default$$.$TileStore$ = void 0;
$module$build$ts$data$TileStore$default$$.$TileStore$ = function() {
};
$module$build$ts$data$TileStore$default$$.$TileStore$.get = function() {
  this.$v$ || (this.$v$ = new $module$build$ts$data$TileStore$default$$.$TileStore$());
  return this.$v$;
};
$module$build$ts$data$TileStore$default$$.$TileStore$.prototype.$init$ = function() {
  var $$jscomp$this$jscomp$13$$ = this;
  if (this.$landscapeMap$ && this.$weatherMap$) {
    console.warn("tried to re-initialize tile animations");
  } else {
    var $loader$$ = new $module$build$ts$util$JSONLoader$default$$.$JSONLoader$();
    $loader$$.$v$ = function() {
      $$jscomp$this$jscomp$13$$.$landscapeMap$ = $JSCompiler_StaticMethods_loadAnimations$$($loader$$.data.landscape, $module$build$ts$data$Paths$default$$.$Paths$.$v$ + "/");
      $$jscomp$this$jscomp$13$$.$weatherMap$ = $JSCompiler_StaticMethods_loadAnimations$$($loader$$.data.weather, $stendhal$$.$paths$.$weather$ + "/");
    };
    $loader$$.load($module$build$ts$data$Paths$default$$.$Paths$.$v$ + "/animation.json");
  }
};
function $JSCompiler_StaticMethods_loadAnimations$$($def$jscomp$1$$, $prefix$jscomp$2$$) {
  for (var $ani$$ = {}, $$jscomp$iter$36$$ = $$jscomp$makeIterator$$(Object.keys($def$jscomp$1$$)), $$jscomp$key$tsname_tsname$$ = $$jscomp$iter$36$$.next(); !$$jscomp$key$tsname_tsname$$.done; $$jscomp$key$tsname_tsname$$ = $$jscomp$iter$36$$.next()) {
    $$jscomp$key$tsname_tsname$$ = $$jscomp$key$tsname_tsname$$.value;
    for (var $entry$jscomp$11$$ = {}, $$jscomp$iter$35$$ = $$jscomp$makeIterator$$($def$jscomp$1$$[$$jscomp$key$tsname_tsname$$]), $$jscomp$key$li_id$jscomp$25$$ = $$jscomp$iter$35$$.next(); !$$jscomp$key$li_id$jscomp$25$$.done; $$jscomp$key$li_id$jscomp$25$$ = $$jscomp$iter$35$$.next()) {
      var $$jscomp$iter$34_li$jscomp$1$$ = $$jscomp$key$li_id$jscomp$25$$.value;
      $$jscomp$iter$34_li$jscomp$1$$ = $$jscomp$iter$34_li$jscomp$1$$.trim();
      $$jscomp$iter$34_li$jscomp$1$$ = $$jscomp$iter$34_li$jscomp$1$$.replace(/\t/g, " ");
      $$jscomp$iter$34_li$jscomp$1$$ = $$jscomp$iter$34_li$jscomp$1$$.replace(/  /g, " ");
      $$jscomp$iter$34_li$jscomp$1$$ = $$jscomp$iter$34_li$jscomp$1$$.split(" ");
      if (1 < $$jscomp$iter$34_li$jscomp$1$$.length) {
        $$jscomp$key$li_id$jscomp$25$$ = $$jscomp$iter$34_li$jscomp$1$$[0];
        var $delay_first_frame$$ = 500;
        if ($$jscomp$key$li_id$jscomp$25$$.includes("@")) {
          var $frames$jscomp$3_idtemp$$ = $$jscomp$key$li_id$jscomp$25$$.split("@");
          $$jscomp$key$li_id$jscomp$25$$ = $frames$jscomp$3_idtemp$$[0];
          $delay_first_frame$$ = 1 < $frames$jscomp$3_idtemp$$.length ? parseInt($frames$jscomp$3_idtemp$$[1], 10) : 500;
        }
        $frames$jscomp$3_idtemp$$ = [];
        var $delays$$ = [];
        $$jscomp$iter$34_li$jscomp$1$$ = $$jscomp$makeIterator$$($$jscomp$iter$34_li$jscomp$1$$[1].split(":"));
        for (var $$jscomp$key$frame_frame$jscomp$1_ftemp$$ = $$jscomp$iter$34_li$jscomp$1$$.next(); !$$jscomp$key$frame_frame$jscomp$1_ftemp$$.done; $$jscomp$key$frame_frame$jscomp$1_ftemp$$ = $$jscomp$iter$34_li$jscomp$1$$.next()) {
          $$jscomp$key$frame_frame$jscomp$1_ftemp$$ = $$jscomp$key$frame_frame$jscomp$1_ftemp$$.value, $$jscomp$key$frame_frame$jscomp$1_ftemp$$.includes("@") && ($$jscomp$key$frame_frame$jscomp$1_ftemp$$ = $$jscomp$key$frame_frame$jscomp$1_ftemp$$.split("@"), $delay_first_frame$$ = parseInt($$jscomp$key$frame_frame$jscomp$1_ftemp$$[1], 10), $$jscomp$key$frame_frame$jscomp$1_ftemp$$ = $$jscomp$key$frame_frame$jscomp$1_ftemp$$[0]), $$jscomp$key$frame_frame$jscomp$1_ftemp$$ = parseInt($$jscomp$key$frame_frame$jscomp$1_ftemp$$, 
          10), $frames$jscomp$3_idtemp$$.push($$jscomp$key$frame_frame$jscomp$1_ftemp$$), $delays$$.push($delay_first_frame$$);
        }
        $delay_first_frame$$ = $frames$jscomp$3_idtemp$$[0];
        "*" !== $$jscomp$key$li_id$jscomp$25$$ && ($delay_first_frame$$ = parseInt($$jscomp$key$li_id$jscomp$25$$, 10));
        $entry$jscomp$11$$[$delay_first_frame$$] = {frames:$frames$jscomp$3_idtemp$$, $delays$:$delays$$};
      }
    }
    $ani$$[$prefix$jscomp$2$$ + $$jscomp$key$tsname_tsname$$ + ".png"] = $entry$jscomp$11$$;
  }
  return $ani$$;
}
;var $module$build$ts$util$WeatherRenderer$default$$ = {};
Object.defineProperty($module$build$ts$util$WeatherRenderer$default$$, "__esModule", {value:!0});
$module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$ = void 0;
var $weatherLoops$$module$build$ts$util$WeatherRenderer$$ = {rain:!0, rain_heavy:!0, rain_light:!0};
$module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$ = function() {
  this.enabled = !0;
  this.$v$ = {};
  this.$D$ = this.$C$ = this.$B$ = this.$A$ = 0;
  this.$F$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$v$().$layers$.indexOf("ambient");
};
$module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$.get = function() {
  $module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$.$v$ || ($module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$.$v$ = new $module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$());
  return $module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$.$v$;
};
$module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$.prototype.update = function($weather$jscomp$1$$) {
  this.enabled = $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.weather");
  this.$A$ = 0;
  this.$B$ = Date.now();
  this.$v$ = {};
  var $soundMan$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$v$();
  this.audio && ($soundMan$$.stop(this.$F$, this.audio), this.audio = void 0);
  if ($weather$jscomp$1$$) {
    var $animationMap_img$jscomp$4_spriteH$$ = $stendhal$$.$paths$.$weather$ + "/" + $weather$jscomp$1$$ + ".png";
    this.$sprite$ = $stendhal$$.data.$sprites$.get($animationMap_img$jscomp$4_spriteH$$);
    $animationMap_img$jscomp$4_spriteH$$ = ($module$build$ts$data$TileStore$default$$.$TileStore$.get().$weatherMap$ || {})[$animationMap_img$jscomp$4_spriteH$$];
    if (this.$sprite$ && this.$sprite$.src) {
      if ($animationMap_img$jscomp$4_spriteH$$ && 0 == Object.keys($animationMap_img$jscomp$4_spriteH$$).length) {
        console.error("weather animation map for '" + $weather$jscomp$1$$ + "' is empty");
      } else {
        $animationMap_img$jscomp$4_spriteH$$ ? (this.$sprite$.frames = $animationMap_img$jscomp$4_spriteH$$[0].frames, this.$sprite$.$delays$ = $animationMap_img$jscomp$4_spriteH$$[0].$delays$) : this.$sprite$.frames = [0];
        $animationMap_img$jscomp$4_spriteH$$ = this.$sprite$.height;
        $animationMap_img$jscomp$4_spriteH$$ || ($animationMap_img$jscomp$4_spriteH$$ = 32, console.log("using failsafe sprite height: " + $animationMap_img$jscomp$4_spriteH$$));
        var $canvas$$ = document.getElementById("gamewindow");
        this.$C$ = Math.ceil($canvas$$.width / $animationMap_img$jscomp$4_spriteH$$);
        this.$D$ = Math.ceil($canvas$$.height / $animationMap_img$jscomp$4_spriteH$$);
        $weatherLoops$$module$build$ts$util$WeatherRenderer$$[$weather$jscomp$1$$] && (this.audio = $JSCompiler_StaticMethods_playGlobalizedEffect$$($soundMan$$, "weather/" + $weather$jscomp$1$$, this.$F$, 1.0, !0));
      }
    } else {
      console.error("weather sprite for '" + $weather$jscomp$1$$ + "' not found");
    }
  } else {
    this.$sprite$ = void 0;
  }
};
$module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$.prototype.$draw$ = function($ctx_elapsed$$) {
  if (this.enabled && this.$sprite$ && this.$sprite$.frames) {
    if (this.$C$ && this.$D$) {
      if (this.$sprite$.height) {
        for (var $cycleTime_delayComb$jscomp$inline_468_dim$$ = this.$sprite$.height, $idx$jscomp$inline_469_ix$$ = 0; $idx$jscomp$inline_469_ix$$ < this.$C$; $idx$jscomp$inline_469_ix$$++) {
          for (var $iy$$ = 0; $iy$$ < this.$D$; $iy$$++) {
            $ctx_elapsed$$.drawImage(this.$sprite$, this.$sprite$.frames[this.$A$] * $cycleTime_delayComb$jscomp$inline_468_dim$$, 0, $cycleTime_delayComb$jscomp$inline_468_dim$$, $cycleTime_delayComb$jscomp$inline_468_dim$$, $idx$jscomp$inline_469_ix$$ * $cycleTime_delayComb$jscomp$inline_468_dim$$ + $stendhal$$.$ui$.$gamewindow$.offsetX, $iy$$ * $cycleTime_delayComb$jscomp$inline_468_dim$$ + $stendhal$$.$ui$.$gamewindow$.offsetY, $cycleTime_delayComb$jscomp$inline_468_dim$$, $cycleTime_delayComb$jscomp$inline_468_dim$$);
          }
        }
        if (this.$sprite$.$delays$ && ($cycleTime_delayComb$jscomp$inline_468_dim$$ = Date.now(), $ctx_elapsed$$ = $cycleTime_delayComb$jscomp$inline_468_dim$$ - this.$B$, $ctx_elapsed$$ >= this.$sprite$.$delays$[this.$A$])) {
          this.$B$ = $cycleTime_delayComb$jscomp$inline_468_dim$$;
          $cycleTime_delayComb$jscomp$inline_468_dim$$ = 0;
          $idx$jscomp$inline_469_ix$$ = this.$A$;
          for ($idx$jscomp$inline_469_ix$$; $cycleTime_delayComb$jscomp$inline_468_dim$$ < $ctx_elapsed$$; $idx$jscomp$inline_469_ix$$++) {
            $cycleTime_delayComb$jscomp$inline_468_dim$$ += this.$sprite$.$delays$[$idx$jscomp$inline_469_ix$$], $idx$jscomp$inline_469_ix$$ + 1 >= this.$sprite$.$delays$.length && ($idx$jscomp$inline_469_ix$$ = -1);
          }
          this.$A$ = $idx$jscomp$inline_469_ix$$;
        }
      } else {
        this.$v$.$imgReady$ || (console.warn("waiting on image to load before drawing weather"), this.$v$.$imgReady$ = !0);
      }
    } else {
      this.$v$.$tiling$ || (console.warn("cannot tile weather animation"), this.$v$.$tiling$ = !0);
    }
  }
};
var $module$build$ts$data$CStatus$default$$ = {};
Object.defineProperty($module$build$ts$data$CStatus$default$$, "__esModule", {value:!0});
$module$build$ts$data$CStatus$default$$.$CStatus$ = void 0;
$module$build$ts$data$CStatus$default$$.$CStatus$ = function() {
  this.$v$ = !1;
};
$module$build$ts$data$CStatus$default$$.$CStatus$.get = function() {
  $module$build$ts$data$CStatus$default$$.$CStatus$.$v$ || ($module$build$ts$data$CStatus$default$$.$CStatus$.$v$ = new $module$build$ts$data$CStatus$default$$.$CStatus$());
  return $module$build$ts$data$CStatus$default$$.$CStatus$.$v$;
};
$module$build$ts$data$CStatus$default$$.$CStatus$.prototype.$init$ = function() {
  this.$v$ ? console.warn("tried to re-initialize CStatus") : (this.$v$ = !0, this.send());
};
$module$build$ts$data$CStatus$default$$.$CStatus$.prototype.send = function() {
  if ($marauroa$$.$me$) {
    var $action$jscomp$56$$ = {type:"cstatus", version:$stendhal$$.data.$build$.version, build:$stendhal$$.data.$build$.$build$, dist:$stendhal$$.data.$build$.$dist$, cid:$stendhal$$.data.cache.get("cid")};
    $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$56$$);
  } else {
    window.setTimeout($stendhal$$.data.$cstatus$.send, 1000);
  }
};
var $module$build$ts$data$CacheManager$default$$ = {};
Object.defineProperty($module$build$ts$data$CacheManager$default$$, "__esModule", {value:!0});
$module$build$ts$data$CacheManager$default$$.$CacheManager$ = void 0;
$module$build$ts$data$CacheManager$default$$.$CacheManager$ = function() {
};
$module$build$ts$data$CacheManager$default$$.$CacheManager$.get = function() {
  $module$build$ts$data$CacheManager$default$$.$CacheManager$.$v$ || ($module$build$ts$data$CacheManager$default$$.$CacheManager$.$v$ = new $module$build$ts$data$CacheManager$default$$.$CacheManager$());
  return $module$build$ts$data$CacheManager$default$$.$CacheManager$.$v$;
};
$module$build$ts$data$CacheManager$default$$.$CacheManager$.prototype.$init$ = function() {
  function $requestAllItems$$($store_storeName$$, $callback$jscomp$49$$) {
    var $cursorRequest_tx$jscomp$2$$ = $stendhal$$.data.cache.db.transaction($store_storeName$$, "readonly");
    $store_storeName$$ = $cursorRequest_tx$jscomp$2$$.objectStore($store_storeName$$);
    var $items$jscomp$4$$ = [];
    $cursorRequest_tx$jscomp$2$$.oncomplete = function() {
      $callback$jscomp$49$$($items$jscomp$4$$);
    };
    $cursorRequest_tx$jscomp$2$$ = $store_storeName$$.openCursor();
    $cursorRequest_tx$jscomp$2$$.onerror = function($error$jscomp$8$$) {
      console.log($error$jscomp$8$$);
    };
    $cursorRequest_tx$jscomp$2$$.onsuccess = function($cursor_evt$jscomp$39$$) {
      if ($cursor_evt$jscomp$39$$ = $cursor_evt$jscomp$39$$.target.result) {
        $items$jscomp$4$$.push($cursor_evt$jscomp$39$$.value), $cursor_evt$jscomp$39$$.continue();
      }
    };
  }
  var $cacheId$$ = null;
  try {
    $cacheId$$ = localStorage.getItem("cache.cid");
  } catch ($e$jscomp$52$$) {
  }
  $stendhal$$.data.cache.sync = {cid:$cacheId$$};
  if (!$cacheId$$ && window.indexedDB) {
    var $open$jscomp$2$$ = indexedDB.open("stendhal", 1);
    $open$jscomp$2$$.onupgradeneeded = function() {
      $open$jscomp$2$$.result.createObjectStore("cache", {keyPath:"key"});
    };
    $open$jscomp$2$$.onsuccess = function() {
      $stendhal$$.data.cache.db = $open$jscomp$2$$.result;
      $requestAllItems$$("cache", function($items$jscomp$5$$) {
        for (var $len$jscomp$7$$ = $items$jscomp$5$$.length, $i$jscomp$63$$ = 0; $i$jscomp$63$$ < $len$jscomp$7$$; $i$jscomp$63$$ += 1) {
          $stendhal$$.data.cache.sync[$items$jscomp$5$$[$i$jscomp$63$$].key] = $items$jscomp$5$$[$i$jscomp$63$$].data;
          try {
            $cacheId$$ = localStorage.setItem("cache.cid", $items$jscomp$5$$[$i$jscomp$63$$].data);
          } catch ($e$94$$) {
          }
        }
        $stendhal$$.data.cache.sync.cid || $stendhal$$.data.cache.put("cid", (1e48 * Math.random()).toString(36));
      });
    };
    $open$jscomp$2$$.onerror = function() {
      $stendhal$$.data.cache.put("cid", (1e48 * Math.random()).toString(36));
    };
  }
};
$module$build$ts$data$CacheManager$default$$.$CacheManager$.prototype.get = function($key$jscomp$75$$) {
  return $stendhal$$.data.cache.sync[$key$jscomp$75$$];
};
$module$build$ts$data$CacheManager$default$$.$CacheManager$.prototype.put = function($key$jscomp$76$$, $value$jscomp$116$$) {
  if ($stendhal$$.data.cache.sync[$key$jscomp$76$$] !== $value$jscomp$116$$) {
    $stendhal$$.data.cache.sync[$key$jscomp$76$$] = $value$jscomp$116$$;
    try {
      localStorage.setItem("cache." + $key$jscomp$76$$, $value$jscomp$116$$);
    } catch ($e$jscomp$53$$) {
      console.log($e$jscomp$53$$);
    }
  }
};
var $module$build$ts$data$EmojiStore$default$$ = {};
Object.defineProperty($module$build$ts$data$EmojiStore$default$$, "__esModule", {value:!0});
$module$build$ts$data$EmojiStore$default$$.$EmojiStore$ = void 0;
$module$build$ts$data$EmojiStore$default$$.$EmojiStore$ = function() {
  this.$emojilist$ = [];
  this.$v$ = {};
};
$module$build$ts$data$EmojiStore$default$$.$EmojiStore$.get = function() {
  $module$build$ts$data$EmojiStore$default$$.$EmojiStore$.$v$ || ($module$build$ts$data$EmojiStore$default$$.$EmojiStore$.$v$ = new $module$build$ts$data$EmojiStore$default$$.$EmojiStore$());
  return $module$build$ts$data$EmojiStore$default$$.$EmojiStore$.$v$;
};
$module$build$ts$data$EmojiStore$default$$.$EmojiStore$.prototype.$init$ = function() {
  var $$jscomp$this$jscomp$14$$ = this, $loader$jscomp$1$$ = new $module$build$ts$util$JSONLoader$default$$.$JSONLoader$();
  $loader$jscomp$1$$.$v$ = function() {
    $$jscomp$this$jscomp$14$$.$emojilist$ = $loader$jscomp$1$$.data.emojilist;
    $$jscomp$this$jscomp$14$$.$v$ = $loader$jscomp$1$$.data.emojimap;
  };
  $loader$jscomp$1$$.load($module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/emoji/emojis.json");
};
$module$build$ts$data$EmojiStore$default$$.$EmojiStore$.prototype.create = function($JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$) {
  $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$ = $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$.replace(/\\\\/g, "\\");
  var $name$jscomp$inline_1042$$ = this.$v$[$JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$];
  if (!$name$jscomp$inline_1042$$ && $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$.startsWith(":") && $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$.endsWith(":")) {
    var $name$jscomp$inline_1155$$ = $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$ = $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$.substr(0, $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$.length - 1).substr(1);
    $name$jscomp$inline_1155$$.startsWith(":") && $name$jscomp$inline_1155$$.endsWith(":") && ($name$jscomp$inline_1155$$ = $name$jscomp$inline_1155$$.substr(1, $name$jscomp$inline_1155$$.length - 2));
    -1 < this.$emojilist$.indexOf($name$jscomp$inline_1155$$) && ($name$jscomp$inline_1042$$ = $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$);
  }
  $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$ = ($JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$ = $name$jscomp$inline_1042$$) ? $module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/emoji/" + $JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$ + ".png" : void 0;
  if ($JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$) {
    return $stendhal$$.data.$sprites$.get($JSCompiler_inline_result$jscomp$166_JSCompiler_inline_result$jscomp$991_text$jscomp$12_text$jscomp$inline_1041$$);
  }
};
var $module$build$ts$data$GroupManager$default$$ = {};
Object.defineProperty($module$build$ts$data$GroupManager$default$$, "__esModule", {value:!0});
$module$build$ts$data$GroupManager$default$$.$GroupManager$ = void 0;
$module$build$ts$data$GroupManager$default$$.$GroupManager$ = function() {
  this.$members$ = [];
  this.$leader$ = this.$lootmode$ = "";
  this.count = 0;
};
$module$build$ts$data$GroupManager$default$$.$GroupManager$.get = function() {
  $module$build$ts$data$GroupManager$default$$.$GroupManager$.$v$ || ($module$build$ts$data$GroupManager$default$$.$GroupManager$.$v$ = new $module$build$ts$data$GroupManager$default$$.$GroupManager$());
  return $module$build$ts$data$GroupManager$default$$.$GroupManager$.$v$;
};
$module$build$ts$data$GroupManager$default$$.$GroupManager$.prototype.$updateGroupStatus$ = function($memberArray_members$$, $leader$$, $lootmode$$) {
  this.count = 0;
  if ($memberArray_members$$) {
    $memberArray_members$$ = $memberArray_members$$.substring(1, $memberArray_members$$.length - 1).split("\t");
    $stendhal$$.data.group.$members$ = {};
    for (var $i$jscomp$64$$ = 0; $i$jscomp$64$$ < $memberArray_members$$.length; $i$jscomp$64$$++) {
      $stendhal$$.data.group.$members$[$memberArray_members$$[$i$jscomp$64$$]] = !0, this.count++;
    }
    $stendhal$$.data.group.$leader$ = $leader$$;
    $stendhal$$.data.group.$lootmode$ = $lootmode$$;
  } else {
    $stendhal$$.data.group.$members$ = [], $stendhal$$.data.group.$leader$ = "", $stendhal$$.data.group.$lootmode$ = "";
  }
};
var $module$build$ts$landscape$CombinedTileset$default$$ = {};
Object.defineProperty($module$build$ts$landscape$CombinedTileset$default$$, "__esModule", {value:!0});
$module$build$ts$landscape$CombinedTileset$default$$.$CombinedTileset$ = void 0;
$module$build$ts$landscape$CombinedTileset$default$$.$CombinedTileset$ = function($numberOfTiles$$, $combinedLayers$$) {
  this.$combinedLayers$ = $combinedLayers$$;
  this.$tilesPerRow$ = Math.ceil(Math.sqrt($numberOfTiles$$));
  this.canvas = document.createElement("canvas");
  this.canvas.width = $stendhal$$.data.map.$tileWidth$ * this.$tilesPerRow$;
  this.canvas.height = $stendhal$$.data.map.$tileHeight$ * this.$tilesPerRow$;
  this.$ctx$ = this.canvas.getContext("2d");
};
var $module$build$ts$util$MapOfSets$default$$ = {};
Object.defineProperty($module$build$ts$util$MapOfSets$default$$, "__esModule", {value:!0});
$module$build$ts$util$MapOfSets$default$$.$MapOfSets$ = void 0;
$module$build$ts$util$MapOfSets$default$$.$MapOfSets$ = function() {
  return $$jscomp$construct$$(Map, arguments, this.constructor);
};
$$jscomp$inherits$$($module$build$ts$util$MapOfSets$default$$.$MapOfSets$, Map);
$module$build$ts$util$MapOfSets$default$$.$MapOfSets$.prototype.add = function($key$jscomp$77$$, $value$jscomp$117$$) {
  var $set$jscomp$2$$ = this.get($key$jscomp$77$$);
  void 0 === $set$jscomp$2$$ && ($set$jscomp$2$$ = new Set(), this.set($key$jscomp$77$$, $set$jscomp$2$$));
  $set$jscomp$2$$.add($value$jscomp$117$$);
};
var $module$build$ts$landscape$CombinedTilesetImageLoader$default$$ = {};
Object.defineProperty($module$build$ts$landscape$CombinedTilesetImageLoader$default$$, "__esModule", {value:!0});
$module$build$ts$landscape$CombinedTilesetImageLoader$default$$.$CombinedTilesetImageLoader$ = void 0;
$module$build$ts$landscape$CombinedTilesetImageLoader$default$$.$CombinedTilesetImageLoader$ = function($map$jscomp$6$$, $indexToCombinedTiles$$, $combinedTileset$$) {
  this.map = $map$jscomp$6$$;
  this.$B$ = $indexToCombinedTiles$$;
  this.$combinedTileset$ = $combinedTileset$$;
  this.$A$ = [];
  this.$animations$ = {};
  this.$C$ = $module$build$ts$data$TileStore$default$$.$TileStore$.get().$landscapeMap$ || {};
};
function $JSCompiler_StaticMethods_loadTileset$$($JSCompiler_StaticMethods_loadTileset$self$$, $tileset$$) {
  var $img$jscomp$5$$ = document.createElement("img");
  $img$jscomp$5$$.onload = function() {
    var $firstGid$jscomp$inline_479_gid$jscomp$inline_482$$ = $JSCompiler_StaticMethods_loadTileset$self$$.map.$firstgids$[$tileset$$], $image$jscomp$inline_480_lastGid$jscomp$inline_481$$ = $JSCompiler_StaticMethods_loadTileset$self$$.$A$[$tileset$$];
    for ($image$jscomp$inline_480_lastGid$jscomp$inline_481$$ = $firstGid$jscomp$inline_479_gid$jscomp$inline_482$$ + $image$jscomp$inline_480_lastGid$jscomp$inline_481$$.width / $JSCompiler_StaticMethods_loadTileset$self$$.map.$tileWidth$ * $image$jscomp$inline_480_lastGid$jscomp$inline_481$$.height / $JSCompiler_StaticMethods_loadTileset$self$$.map.$tileHeight$; $firstGid$jscomp$inline_479_gid$jscomp$inline_482$$ <= $image$jscomp$inline_480_lastGid$jscomp$inline_481$$; $firstGid$jscomp$inline_479_gid$jscomp$inline_482$$++) {
      var $$jscomp$inline_484_indexes$jscomp$inline_483$$ = $JSCompiler_StaticMethods_loadTileset$self$$.$v$.get($firstGid$jscomp$inline_479_gid$jscomp$inline_482$$);
      if (void 0 !== $$jscomp$inline_484_indexes$jscomp$inline_483$$) {
        $$jscomp$inline_484_indexes$jscomp$inline_483$$ = $$jscomp$makeIterator$$($$jscomp$inline_484_indexes$jscomp$inline_483$$);
        for (var $$jscomp$inline_485_pixelX$jscomp$inline_1047$$ = $$jscomp$inline_484_indexes$jscomp$inline_483$$.next(); !$$jscomp$inline_485_pixelX$jscomp$inline_1047$$.done; $$jscomp$inline_485_pixelX$jscomp$inline_1047$$ = $$jscomp$inline_484_indexes$jscomp$inline_483$$.next()) {
          var $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$ = $JSCompiler_StaticMethods_loadTileset$self$$, $index$jscomp$inline_1045_pixelY$jscomp$inline_1048$$ = $$jscomp$inline_485_pixelX$jscomp$inline_1047$$.value, $$jscomp$inline_1049_tiles$jscomp$inline_1046$$ = $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.$B$.get($index$jscomp$inline_1045_pixelY$jscomp$inline_1048$$);
          $$jscomp$inline_485_pixelX$jscomp$inline_1047$$ = $index$jscomp$inline_1045_pixelY$jscomp$inline_1048$$ % $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.$combinedTileset$.$tilesPerRow$ * $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.map.$tileWidth$;
          $index$jscomp$inline_1045_pixelY$jscomp$inline_1048$$ = Math.floor($index$jscomp$inline_1045_pixelY$jscomp$inline_1048$$ / $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.$combinedTileset$.$tilesPerRow$) * $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.map.$tileHeight$;
          $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.$combinedTileset$.$ctx$.clearRect($$jscomp$inline_485_pixelX$jscomp$inline_1047$$, $index$jscomp$inline_1045_pixelY$jscomp$inline_1048$$, $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.map.$tileWidth$, $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.map.$tileHeight$);
          $$jscomp$inline_1049_tiles$jscomp$inline_1046$$ = $$jscomp$makeIterator$$($$jscomp$inline_1049_tiles$jscomp$inline_1046$$);
          for (var $$jscomp$key$tile$jscomp$inline_1050_tile$jscomp$inline_1051$$ = $$jscomp$inline_1049_tiles$jscomp$inline_1046$$.next(); !$$jscomp$key$tile$jscomp$inline_1050_tile$jscomp$inline_1051$$.done; $$jscomp$key$tile$jscomp$inline_1050_tile$jscomp$inline_1051$$ = $$jscomp$inline_1049_tiles$jscomp$inline_1046$$.next()) {
            $$jscomp$key$tile$jscomp$inline_1050_tile$jscomp$inline_1051$$ = $$jscomp$key$tile$jscomp$inline_1050_tile$jscomp$inline_1051$$.value;
            var $gid$jscomp$inline_1052$$ = $$jscomp$key$tile$jscomp$inline_1050_tile$jscomp$inline_1051$$ & 536870911, $tileset$jscomp$inline_1053$$ = $JSCompiler_StaticMethods_getTilesetForGid$$($JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.map, $gid$jscomp$inline_1052$$), $image$jscomp$inline_1054$$ = $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.$A$[$tileset$jscomp$inline_1053$$];
            $image$jscomp$inline_1054$$ && $image$jscomp$inline_1054$$.height && $JSCompiler_StaticMethods_JSC$1949_drawTile$$($JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$, $$jscomp$inline_485_pixelX$jscomp$inline_1047$$, $index$jscomp$inline_1045_pixelY$jscomp$inline_1048$$, $image$jscomp$inline_1054$$, $gid$jscomp$inline_1052$$ - $JSCompiler_StaticMethods_drawCombinedTileAtIndex$self$jscomp$inline_1044$$.map.$firstgids$[$tileset$jscomp$inline_1053$$], $$jscomp$key$tile$jscomp$inline_1050_tile$jscomp$inline_1051$$ & 
            3758096384);
          }
        }
      }
    }
  };
  var $animation_tsname$jscomp$1$$ = $JSCompiler_StaticMethods_loadTileset$self$$.map.$tilesetFilenames$[$tileset$$];
  $img$jscomp$5$$.src = $animation_tsname$jscomp$1$$ + "?v=" + $stendhal$$.data.$build$.version;
  $JSCompiler_StaticMethods_loadTileset$self$$.$C$ && ($animation_tsname$jscomp$1$$ = $JSCompiler_StaticMethods_loadTileset$self$$.$C$[$animation_tsname$jscomp$1$$]) && ($JSCompiler_StaticMethods_loadTileset$self$$.$animations$[$tileset$$] = $animation_tsname$jscomp$1$$);
  $JSCompiler_StaticMethods_loadTileset$self$$.$A$[$tileset$$] = $img$jscomp$5$$;
}
function $JSCompiler_StaticMethods_JSC$1949_drawTile$$($$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$, $pixelX$jscomp$1_restore$$, $pixelY$jscomp$1$$, $$jscomp$key$args_tilesetImage$$, $tileIndexInTileset$jscomp$1$$, $flip$jscomp$1$$) {
  var $tilesPerRow$$ = Math.floor($$jscomp$key$args_tilesetImage$$.width / $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileWidth$), $ctx$jscomp$1$$ = $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.$combinedTileset$.$ctx$;
  if (0 === $flip$jscomp$1$$) {
    $ctx$jscomp$1$$.drawImage($$jscomp$key$args_tilesetImage$$, $tileIndexInTileset$jscomp$1$$ % $tilesPerRow$$ * $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileWidth$, Math.floor($tileIndexInTileset$jscomp$1$$ / $tilesPerRow$$) * $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileHeight$, $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileWidth$, $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileHeight$, 
    $pixelX$jscomp$1_restore$$, $pixelY$jscomp$1$$, $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileWidth$, $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileHeight$);
  } else {
    for ($ctx$jscomp$1$$.translate($pixelX$jscomp$1_restore$$, $pixelY$jscomp$1$$), $pixelX$jscomp$1_restore$$ = [[1, 0, 0, 1, -$pixelX$jscomp$1_restore$$, -$pixelY$jscomp$1$$]], 0 !== ($flip$jscomp$1$$ & 2147483648) && ($ctx$jscomp$1$$.transform(-1, 0, 0, 1, 0, 0), $ctx$jscomp$1$$.translate(-$$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileWidth$, 0), $pixelX$jscomp$1_restore$$.push([-1, 0, 0, 1, 0, 0]), $pixelX$jscomp$1_restore$$.push([1, 0, 0, 1, $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileWidth$, 
    0])), 0 !== ($flip$jscomp$1$$ & 1073741824) && ($ctx$jscomp$1$$.transform(1, 0, 0, -1, 0, 0), $ctx$jscomp$1$$.translate(0, -$$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileWidth$), $pixelX$jscomp$1_restore$$.push([1, 0, 0, -1, 0, 0]), $pixelX$jscomp$1_restore$$.push([1, 0, 0, 1, 0, $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.map.$tileHeight$])), 0 !== ($flip$jscomp$1$$ & 536870912) && ($ctx$jscomp$1$$.transform(0, 1, 1, 0, 0, 0), $pixelX$jscomp$1_restore$$.push([0, 
    1, 1, 0, 0, 0])), $JSCompiler_StaticMethods_JSC$1949_drawTile$$($$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$, 0, 0, $$jscomp$key$args_tilesetImage$$, $tileIndexInTileset$jscomp$1$$, 0), $pixelX$jscomp$1_restore$$.reverse(), $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$ = $$jscomp$makeIterator$$($pixelX$jscomp$1_restore$$), $$jscomp$key$args_tilesetImage$$ = $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.next(); !$$jscomp$key$args_tilesetImage$$.done; $$jscomp$key$args_tilesetImage$$ = 
    $$jscomp$iter$42_JSCompiler_StaticMethods_JSC$1949_drawTile$self$$.next()) {
      $ctx$jscomp$1$$.transform.apply($ctx$jscomp$1$$, $$jscomp$key$args_tilesetImage$$.value);
    }
  }
}
$module$build$ts$landscape$CombinedTilesetImageLoader$default$$.$CombinedTilesetImageLoader$.prototype.load = function() {
  console.log("CombinedTilesetImageLoader.load()");
  this.$v$ = new $module$build$ts$util$MapOfSets$default$$.$MapOfSets$();
  for (var $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$ = $$jscomp$makeIterator$$(this.$B$.entries()), $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$ = $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$.next(); !$$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$.done; $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$ = 
  $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$.next()) {
    var $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$ = $$jscomp$makeIterator$$($$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$.value);
    $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$ = $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$.next().value;
    $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$ = $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$.next().value;
    $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$ = $$jscomp$makeIterator$$($$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$);
    for (var $$jscomp$inline_494$$ = $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$.next(); !$$jscomp$inline_494$$.done; $$jscomp$inline_494$$ = $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$.next()) {
      this.$v$.add($$jscomp$inline_494$$.value & 536870911, $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$);
    }
  }
  $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$ = this.$v$.keys();
  $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$ = new Set();
  $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$ = $$jscomp$makeIterator$$($$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$);
  for ($$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$ = $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$.next(); !$$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$.done; $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$ = $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$.next()) {
    $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$.add($JSCompiler_StaticMethods_getTilesetForGid$$(this.map, $$jscomp$inline_490_$jscomp$inline_493_$jscomp$inline_500_combinedTile$jscomp$inline_492$$.value));
  }
  $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$ = $$jscomp$makeIterator$$($$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$);
  for ($$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$ = $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$.next(); !$$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$.done; $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$ = $$jscomp$inline_488_$jscomp$iter$43_usedTilesets$jscomp$inline_498$$.next()) {
    $JSCompiler_StaticMethods_loadTileset$$(this, $$jscomp$inline_499_$jscomp$key$$jscomp$inline_489_$jscomp$key$tileset_gids$jscomp$inline_497_index$jscomp$inline_491$$.value);
  }
};
var $module$build$ts$landscape$CombinedTilesetFactory$default$$ = {};
Object.defineProperty($module$build$ts$landscape$CombinedTilesetFactory$default$$, "__esModule", {value:!0});
$module$build$ts$landscape$CombinedTilesetFactory$default$$.$CombinedTilesetFactory$ = void 0;
$module$build$ts$landscape$CombinedTilesetFactory$default$$.$CombinedTilesetFactory$ = function($map$jscomp$7$$) {
  this.map = $map$jscomp$7$$;
};
var $module$build$ts$landscape$LandscapeRenderer$default$$ = {};
Object.defineProperty($module$build$ts$landscape$LandscapeRenderer$default$$, "__esModule", {value:!0});
$module$build$ts$landscape$LandscapeRenderer$default$$.$LandscapeRenderer$ = void 0;
$module$build$ts$landscape$LandscapeRenderer$default$$.$LandscapeRenderer$ = function() {
};
function $JSCompiler_StaticMethods_drawLayer$$($canvas$jscomp$1_xMax$$, $layer$jscomp$10_layerNo$$, $tileOffsetX$$, $tileOffsetY_y$jscomp$90$$, $targetTileWidth$$, $targetTileHeight$$) {
  var $combinedTileset$jscomp$2$$ = $stendhal$$.data.map.$combinedTileset$;
  if ($combinedTileset$jscomp$2$$) {
    var $ctx$jscomp$2$$ = $canvas$jscomp$1_xMax$$.getContext("2d");
    $layer$jscomp$10_layerNo$$ = $combinedTileset$jscomp$2$$.$combinedLayers$[$layer$jscomp$10_layerNo$$];
    var $yMax$$ = Math.min($tileOffsetY_y$jscomp$90$$ + $canvas$jscomp$1_xMax$$.height / $targetTileHeight$$ + 1, $stendhal$$.data.map.$zoneSizeY$);
    for ($canvas$jscomp$1_xMax$$ = Math.min($tileOffsetX$$ + $canvas$jscomp$1_xMax$$.width / $targetTileWidth$$ + 1, $stendhal$$.data.map.$zoneSizeX$); $tileOffsetY_y$jscomp$90$$ < $yMax$$; $tileOffsetY_y$jscomp$90$$++) {
      for (var $x$jscomp$110$$ = $tileOffsetX$$; $x$jscomp$110$$ < $canvas$jscomp$1_xMax$$; $x$jscomp$110$$++) {
        var $index$jscomp$81$$ = $layer$jscomp$10_layerNo$$[$tileOffsetY_y$jscomp$90$$ * $stendhal$$.data.map.$zoneSizeX$ + $x$jscomp$110$$];
        if (-1 < $index$jscomp$81$$) {
          try {
            $ctx$jscomp$2$$.drawImage($combinedTileset$jscomp$2$$.canvas, $index$jscomp$81$$ % $combinedTileset$jscomp$2$$.$tilesPerRow$ * $stendhal$$.data.map.$tileWidth$, Math.floor($index$jscomp$81$$ / $combinedTileset$jscomp$2$$.$tilesPerRow$) * $stendhal$$.data.map.$tileHeight$, $stendhal$$.data.map.$tileWidth$, $stendhal$$.data.map.$tileHeight$, $x$jscomp$110$$ * $targetTileWidth$$, $tileOffsetY_y$jscomp$90$$ * $targetTileHeight$$, $targetTileWidth$$, $targetTileHeight$$);
          } catch ($e$jscomp$54$$) {
            console.error($e$jscomp$54$$);
          }
        }
      }
    }
  }
}
;var $module$build$ts$landscape$LandscapeRenderingStrategy$default$$ = {};
Object.defineProperty($module$build$ts$landscape$LandscapeRenderingStrategy$default$$, "__esModule", {value:!0});
$module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$CombinedTilesetRenderingStrategy$ = $module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$LandscapeRenderingStrategy$ = void 0;
$module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$LandscapeRenderingStrategy$ = function() {
};
$module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$CombinedTilesetRenderingStrategy$ = function() {
  return $module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$LandscapeRenderingStrategy$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$CombinedTilesetRenderingStrategy$, $module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$LandscapeRenderingStrategy$);
$module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$CombinedTilesetRenderingStrategy$.prototype.$v$ = function($combinedTilesetFactory_map$jscomp$8$$) {
  $combinedTilesetFactory_map$jscomp$8$$ = new $module$build$ts$landscape$CombinedTilesetFactory$default$$.$CombinedTilesetFactory$($combinedTilesetFactory_map$jscomp$8$$);
  for (var $JSCompiler_temp_const$jscomp$167$$ = $stendhal$$.data.map, $combinedTilesToIndex$jscomp$inline_503_combinedTileset$jscomp$inline_517$$ = new Map(), $indexToCombinedTiles$jscomp$inline_504$$ = new Map(), $combinedLayers$jscomp$inline_505$$ = [], $index$jscomp$inline_506$$ = 0, $group$jscomp$inline_507_x$jscomp$inline_508$$ = 0; $group$jscomp$inline_507_x$jscomp$inline_508$$ < $combinedTilesetFactory_map$jscomp$8$$.map.$layerGroupIndexes$.length; $group$jscomp$inline_507_x$jscomp$inline_508$$++) {
    $combinedLayers$jscomp$inline_505$$[$group$jscomp$inline_507_x$jscomp$inline_508$$] = [];
  }
  for ($group$jscomp$inline_507_x$jscomp$inline_508$$ = 0; $group$jscomp$inline_507_x$jscomp$inline_508$$ < $combinedTilesetFactory_map$jscomp$8$$.map.$zoneSizeX$; $group$jscomp$inline_507_x$jscomp$inline_508$$++) {
    for (var $y$jscomp$inline_509$$ = 0; $y$jscomp$inline_509$$ < $combinedTilesetFactory_map$jscomp$8$$.map.$zoneSizeY$; $y$jscomp$inline_509$$++) {
      for (var $group$95$jscomp$inline_510$$ = 0; $group$95$jscomp$inline_510$$ < $combinedTilesetFactory_map$jscomp$8$$.map.$layerGroupIndexes$.length; $group$95$jscomp$inline_510$$++) {
        for (var $combinedTile$jscomp$inline_511$$ = [], $$jscomp$inline_512_key$jscomp$inline_515$$ = $$jscomp$makeIterator$$($combinedTilesetFactory_map$jscomp$8$$.map.$layerGroupIndexes$[$group$95$jscomp$inline_510$$]), $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$ = $$jscomp$inline_512_key$jscomp$inline_515$$.next(); !$$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$.done; $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$ = $$jscomp$inline_512_key$jscomp$inline_515$$.next()) {
          $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$ = $combinedTilesetFactory_map$jscomp$8$$.map.$layers$[$$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$.value][$y$jscomp$inline_509$$ * $combinedTilesetFactory_map$jscomp$8$$.map.$zoneSizeX$ + $group$jscomp$inline_507_x$jscomp$inline_508$$], 0 < $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$ && $combinedTile$jscomp$inline_511$$.push($$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$);
        }
        0 === $combinedTile$jscomp$inline_511$$.length ? $combinedLayers$jscomp$inline_505$$[$group$95$jscomp$inline_510$$][$y$jscomp$inline_509$$ * $combinedTilesetFactory_map$jscomp$8$$.map.$zoneSizeX$ + $group$jscomp$inline_507_x$jscomp$inline_508$$] = -1 : ($$jscomp$inline_512_key$jscomp$inline_515$$ = JSON.stringify($combinedTile$jscomp$inline_511$$), $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$ = $combinedTilesToIndex$jscomp$inline_503_combinedTileset$jscomp$inline_517$$.get($$jscomp$inline_512_key$jscomp$inline_515$$), 
        void 0 === $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$ && ($$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$ = $index$jscomp$inline_506$$, $combinedTilesToIndex$jscomp$inline_503_combinedTileset$jscomp$inline_517$$.set($$jscomp$inline_512_key$jscomp$inline_515$$, $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$), $indexToCombinedTiles$jscomp$inline_504$$.set($$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$, $combinedTile$jscomp$inline_511$$), 
        $index$jscomp$inline_506$$++), $combinedLayers$jscomp$inline_505$$[$group$95$jscomp$inline_510$$][$y$jscomp$inline_509$$ * $combinedTilesetFactory_map$jscomp$8$$.map.$zoneSizeX$ + $group$jscomp$inline_507_x$jscomp$inline_508$$] = $$jscomp$inline_513_gid$jscomp$inline_514_value$jscomp$inline_516$$);
      }
    }
  }
  $combinedTilesToIndex$jscomp$inline_503_combinedTileset$jscomp$inline_517$$ = new $module$build$ts$landscape$CombinedTileset$default$$.$CombinedTileset$($combinedTilesToIndex$jscomp$inline_503_combinedTileset$jscomp$inline_517$$.size, $combinedLayers$jscomp$inline_505$$);
  (new $module$build$ts$landscape$CombinedTilesetImageLoader$default$$.$CombinedTilesetImageLoader$($combinedTilesetFactory_map$jscomp$8$$.map, $indexToCombinedTiles$jscomp$inline_504$$, $combinedTilesToIndex$jscomp$inline_503_combinedTileset$jscomp$inline_517$$)).load();
  $JSCompiler_temp_const$jscomp$167$$.$combinedTileset$ = $combinedTilesToIndex$jscomp$inline_503_combinedTileset$jscomp$inline_517$$;
};
$module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$CombinedTilesetRenderingStrategy$.prototype.$A$ = function() {
  document.getElementById("body").style.cursor = "auto";
};
$module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$CombinedTilesetRenderingStrategy$.prototype.$render$ = function($canvas$jscomp$2$$, $gamewindow$$, $tileOffsetX$jscomp$1$$, $tileOffsetY$jscomp$1$$, $targetTileWidth$jscomp$1$$, $targetTileHeight$jscomp$1$$) {
  new $module$build$ts$landscape$LandscapeRenderer$default$$.$LandscapeRenderer$();
  $JSCompiler_StaticMethods_drawLayer$$($canvas$jscomp$2$$, 0, $tileOffsetX$jscomp$1$$, $tileOffsetY$jscomp$1$$, $targetTileWidth$jscomp$1$$, $targetTileHeight$jscomp$1$$);
  $gamewindow$$.$drawEntities$();
  $JSCompiler_StaticMethods_drawLayer$$($canvas$jscomp$2$$, 1, $tileOffsetX$jscomp$1$$, $tileOffsetY$jscomp$1$$, $targetTileWidth$jscomp$1$$, $targetTileHeight$jscomp$1$$);
};
var $module$build$ts$data$ImagePreloader$default$$ = {};
Object.defineProperty($module$build$ts$data$ImagePreloader$default$$, "__esModule", {value:!0});
$module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$ = void 0;
$module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$ = function($images$$, $callback$jscomp$50_i$jscomp$65$$) {
  var $$jscomp$super$this$jscomp$14$$ = $$jscomp$construct$$(Function, [], this.constructor);
  $$jscomp$super$this$jscomp$14$$.$v$ = {};
  $$jscomp$super$this$jscomp$14$$.$G$ = $callback$jscomp$50_i$jscomp$65$$;
  $$jscomp$super$this$jscomp$14$$.$A$ = !1;
  $$jscomp$super$this$jscomp$14$$.$F$ = !1;
  $$jscomp$super$this$jscomp$14$$.$D$ = !1;
  $$jscomp$super$this$jscomp$14$$.$nLoaded$ = 0;
  $$jscomp$super$this$jscomp$14$$.$B$ = 0;
  $stendhal$$.data.map.$aImages$ = [];
  $$jscomp$super$this$jscomp$14$$.$H$ = $images$$.length;
  for ($callback$jscomp$50_i$jscomp$65$$ = 0; $callback$jscomp$50_i$jscomp$65$$ < $images$$.length; $callback$jscomp$50_i$jscomp$65$$++) {
    var $JSCompiler_StaticMethods_JSC$1965_preload$self$jscomp$inline_519$$ = $$jscomp$super$this$jscomp$14$$, $image$jscomp$inline_520$$ = $images$$[$callback$jscomp$50_i$jscomp$65$$], $oImage$jscomp$inline_521$$ = new Image();
    $stendhal$$.data.map.$aImages$.push($oImage$jscomp$inline_521$$);
    $oImage$jscomp$inline_521$$.onload = $module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$.prototype.onload;
    $oImage$jscomp$inline_521$$.onerror = $module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$.prototype.onerror;
    $oImage$jscomp$inline_521$$.onabort = $module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$.prototype.$C$;
    $oImage$jscomp$inline_521$$.$v$ = $JSCompiler_StaticMethods_JSC$1965_preload$self$jscomp$inline_519$$;
    $oImage$jscomp$inline_521$$.$A$ = !1;
    $oImage$jscomp$inline_521$$.src = $image$jscomp$inline_520$$;
  }
  return $$jscomp$super$this$jscomp$14$$;
};
$$jscomp$inherits$$($module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$, Function);
function $JSCompiler_StaticMethods_onComplete$$($JSCompiler_StaticMethods_onComplete$self$$) {
  $JSCompiler_StaticMethods_onComplete$self$$.$B$++;
  $JSCompiler_StaticMethods_onComplete$self$$.$B$ == $JSCompiler_StaticMethods_onComplete$self$$.$H$ && $JSCompiler_StaticMethods_onComplete$self$$.$G$();
}
$module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$.prototype.onload = function() {
  this.$A$ = !0;
  this.$v$.$nLoaded$++;
  $JSCompiler_StaticMethods_onComplete$$(this.$v$);
};
$module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$.prototype.onerror = function() {
  this.$F$ = !0;
  $JSCompiler_StaticMethods_onComplete$$(this.$v$);
  console.error("Error loading " + this.src);
};
$module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$.prototype.$C$ = function() {
  this.$D$ = !0;
  $JSCompiler_StaticMethods_onComplete$$(this.$v$);
  console.error("Loading " + this.src + " was aborted");
};
var $module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$ = {};
Object.defineProperty($module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$, "__esModule", {value:!0});
$module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$.$IndividualTilesetRenderingStrategy$ = void 0;
$module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$.$IndividualTilesetRenderingStrategy$ = function() {
  var $$jscomp$super$this$jscomp$15$$ = $module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$LandscapeRenderingStrategy$.call(this) || this;
  $$jscomp$super$this$jscomp$15$$.$targetTileWidth$ = 32;
  $$jscomp$super$this$jscomp$15$$.$targetTileHeight$ = 32;
  setTimeout(function() {
    $module$build$ts$util$Chat$default$$.$Chat$.log("client", "Using IndividualTilesetRenderingStrategy");
  }, 1000);
  return $$jscomp$super$this$jscomp$15$$;
};
$$jscomp$inherits$$($module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$.$IndividualTilesetRenderingStrategy$, $module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$LandscapeRenderingStrategy$);
$module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$.$IndividualTilesetRenderingStrategy$.prototype.$v$ = function() {
  console.log("Using IndividualTilesetRenderingStrategy.");
};
$module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$.$IndividualTilesetRenderingStrategy$.prototype.$A$ = function() {
  new $module$build$ts$data$ImagePreloader$default$$.$ImagePreloader$($stendhal$$.data.map.$tilesetFilenames$, function() {
    document.getElementById("body").style.cursor = "auto";
  });
};
$module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$.$IndividualTilesetRenderingStrategy$.prototype.$render$ = function($canvas$jscomp$3$$, $gamewindow$jscomp$1$$, $tileOffsetX$jscomp$2$$, $tileOffsetY$jscomp$2$$, $drawingLayer_targetTileWidth$jscomp$2$$, $name$jscomp$83_targetTileHeight$jscomp$2$$) {
  this.$targetTileWidth$ = $drawingLayer_targetTileWidth$jscomp$2$$;
  this.$targetTileHeight$ = $name$jscomp$83_targetTileHeight$jscomp$2$$;
  for ($drawingLayer_targetTileWidth$jscomp$2$$ = 0; $drawingLayer_targetTileWidth$jscomp$2$$ < $stendhal$$.data.map.$layers$.length; $drawingLayer_targetTileWidth$jscomp$2$$++) {
    $name$jscomp$83_targetTileHeight$jscomp$2$$ = $stendhal$$.data.map.$layerNames$[$drawingLayer_targetTileWidth$jscomp$2$$];
    if ("protection" !== $name$jscomp$83_targetTileHeight$jscomp$2$$ && "collision" !== $name$jscomp$83_targetTileHeight$jscomp$2$$ && "objects" !== $name$jscomp$83_targetTileHeight$jscomp$2$$ && "blend_ground" !== $name$jscomp$83_targetTileHeight$jscomp$2$$ && "blend_roof" !== $name$jscomp$83_targetTileHeight$jscomp$2$$) {
      for (var $tileOffsetX$jscomp$inline_526$$ = $tileOffsetX$jscomp$2$$, $tileOffsetY$jscomp$inline_527_y$jscomp$inline_532$$ = $tileOffsetY$jscomp$2$$, $layer$jscomp$inline_528$$ = $stendhal$$.data.map.$layers$[$drawingLayer_targetTileWidth$jscomp$2$$], $yMax$jscomp$inline_529$$ = Math.min($tileOffsetY$jscomp$inline_527_y$jscomp$inline_532$$ + $canvas$jscomp$3$$.height / this.$targetTileHeight$ + 1, $stendhal$$.data.map.$zoneSizeY$), $xMax$jscomp$inline_530$$ = Math.min($tileOffsetX$jscomp$inline_526$$ + 
      $canvas$jscomp$3$$.width / this.$targetTileWidth$ + 1, $stendhal$$.data.map.$zoneSizeX$), $ctx$jscomp$inline_531$$ = $canvas$jscomp$3$$.getContext("2d"); $tileOffsetY$jscomp$inline_527_y$jscomp$inline_532$$ < $yMax$jscomp$inline_529$$; $tileOffsetY$jscomp$inline_527_y$jscomp$inline_532$$++) {
        for (var $x$jscomp$inline_533$$ = $tileOffsetX$jscomp$inline_526$$; $x$jscomp$inline_533$$ < $xMax$jscomp$inline_530$$; $x$jscomp$inline_533$$++) {
          var $gid$jscomp$inline_534_idx$jscomp$inline_537$$ = $layer$jscomp$inline_528$$[$tileOffsetY$jscomp$inline_527_y$jscomp$inline_532$$ * $stendhal$$.data.map.$zoneSizeX$ + $x$jscomp$inline_533$$], $flip$jscomp$inline_535$$ = $gid$jscomp$inline_534_idx$jscomp$inline_537$$ & 3758096384;
          $gid$jscomp$inline_534_idx$jscomp$inline_537$$ &= 536870911;
          if (0 < $gid$jscomp$inline_534_idx$jscomp$inline_537$$) {
            var $tileset$jscomp$inline_536$$ = $JSCompiler_StaticMethods_getTilesetForGid$$($stendhal$$.data.map, $gid$jscomp$inline_534_idx$jscomp$inline_537$$);
            $gid$jscomp$inline_534_idx$jscomp$inline_537$$ -= $stendhal$$.data.map.$firstgids$[$tileset$jscomp$inline_536$$];
            try {
              0 < $stendhal$$.data.map.$aImages$[$tileset$jscomp$inline_536$$].height && $JSCompiler_StaticMethods_JSC$1970_drawTile$$(this, $ctx$jscomp$inline_531$$, $stendhal$$.data.map.$aImages$[$tileset$jscomp$inline_536$$], $gid$jscomp$inline_534_idx$jscomp$inline_537$$, $x$jscomp$inline_533$$, $tileOffsetY$jscomp$inline_527_y$jscomp$inline_532$$, $flip$jscomp$inline_535$$);
            } catch ($e$jscomp$inline_538$$) {
              console.error($e$jscomp$inline_538$$);
            }
          }
        }
      }
    }
    "2_object" === $name$jscomp$83_targetTileHeight$jscomp$2$$ && $gamewindow$jscomp$1$$.$drawEntities$();
  }
};
function $JSCompiler_StaticMethods_JSC$1970_drawTile$$($$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$, $ctx$jscomp$4$$, $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$, $idx$jscomp$14$$, $pixelX$jscomp$3_x$jscomp$112$$, $pixelY$jscomp$3_y$jscomp$92$$, $flip$jscomp$3$$) {
  $flip$jscomp$3$$ = void 0 === $flip$jscomp$3$$ ? 0 : $flip$jscomp$3$$;
  var $restore$jscomp$1_tilesPerRow$jscomp$1$$ = Math.floor($$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$.width / $stendhal$$.data.map.$tileWidth$);
  $pixelX$jscomp$3_x$jscomp$112$$ *= $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileWidth$;
  $pixelY$jscomp$3_y$jscomp$92$$ *= $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileHeight$;
  if (0 === $flip$jscomp$3$$) {
    $ctx$jscomp$4$$.drawImage($$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$, $idx$jscomp$14$$ % $restore$jscomp$1_tilesPerRow$jscomp$1$$ * $stendhal$$.data.map.$tileWidth$, Math.floor($idx$jscomp$14$$ / $restore$jscomp$1_tilesPerRow$jscomp$1$$) * $stendhal$$.data.map.$tileHeight$, $stendhal$$.data.map.$tileWidth$, $stendhal$$.data.map.$tileHeight$, $pixelX$jscomp$3_x$jscomp$112$$, $pixelY$jscomp$3_y$jscomp$92$$, $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileWidth$, 
    $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileHeight$);
  } else {
    for ($ctx$jscomp$4$$.translate($pixelX$jscomp$3_x$jscomp$112$$, $pixelY$jscomp$3_y$jscomp$92$$), $restore$jscomp$1_tilesPerRow$jscomp$1$$ = [[1, 0, 0, 1, -$pixelX$jscomp$3_x$jscomp$112$$, -$pixelY$jscomp$3_y$jscomp$92$$]], 0 !== ($flip$jscomp$3$$ & 2147483648) && ($ctx$jscomp$4$$.transform(-1, 0, 0, 1, 0, 0), $ctx$jscomp$4$$.translate(-$$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileWidth$, 0), $restore$jscomp$1_tilesPerRow$jscomp$1$$.push([-1, 0, 0, 1, 0, 0]), $restore$jscomp$1_tilesPerRow$jscomp$1$$.push([1, 
    0, 0, 1, $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileWidth$, 0])), 0 !== ($flip$jscomp$3$$ & 1073741824) && ($ctx$jscomp$4$$.transform(1, 0, 0, -1, 0, 0), $ctx$jscomp$4$$.translate(0, -$$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileWidth$), $restore$jscomp$1_tilesPerRow$jscomp$1$$.push([1, 0, 0, -1, 0, 0]), $restore$jscomp$1_tilesPerRow$jscomp$1$$.push([1, 0, 0, 1, 0, $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.$targetTileHeight$])), 
    0 !== ($flip$jscomp$3$$ & 536870912) && ($ctx$jscomp$4$$.transform(0, 1, 1, 0, 0, 0), $restore$jscomp$1_tilesPerRow$jscomp$1$$.push([0, 1, 1, 0, 0, 0])), $JSCompiler_StaticMethods_JSC$1970_drawTile$$($$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$, $ctx$jscomp$4$$, $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$, $idx$jscomp$14$$, 0, 0), $restore$jscomp$1_tilesPerRow$jscomp$1$$.reverse(), $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$ = $$jscomp$makeIterator$$($restore$jscomp$1_tilesPerRow$jscomp$1$$), 
    $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$ = $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.next(); !$$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$.done; $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$ = $$jscomp$iter$45_JSCompiler_StaticMethods_JSC$1970_drawTile$self$$.next()) {
      $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$ = $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$.value, $ctx$jscomp$4$$.transform($$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$[0], $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$[1], $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$[2], $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$[3], $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$[4], $$jscomp$key$args$jscomp$1_args$jscomp$6_tileset$jscomp$5$$[5]);
    }
  }
}
;var $module$build$ts$data$Map$default$$ = {};
Object.defineProperty($module$build$ts$data$Map$default$$, "__esModule", {value:!0});
$module$build$ts$data$Map$default$$.Map = void 0;
$module$build$ts$data$Map$default$$.Map = function() {
  this.$currentZoneName$ = "";
  this.offsetY = this.offsetX = 0;
  this.$zoneSizeY$ = this.$zoneSizeX$ = -1;
  this.$tileHeight$ = this.$tileWidth$ = 32;
  this.$tilesetFilenames$ = [];
  this.$firstgids$ = this.$layers$ = this.$layerNames$ = this.$aImages$ = -1;
  this.$v$ = [];
  this.$targetTileHeight$ = this.$targetTileWidth$ = 0;
  this.$B$ = [["0_floor", "1_terrain", "2_object"], ["3_roof", "4_roof_add"]];
  this.$A$ = [$module$build$ts$data$Paths$default$$.$Paths$.$v$ + "/item/armor/bloodied_small_axe", $module$build$ts$data$Paths$default$$.$Paths$.$v$ + "/item/blood/floor_stain", $module$build$ts$data$Paths$default$$.$Paths$.$v$ + "/item/blood/floor_stains_2", $module$build$ts$data$Paths$default$$.$Paths$.$v$ + "/item/blood/nsew_stains", $module$build$ts$data$Paths$default$$.$Paths$.$v$ + "/item/blood/small_stains"];
  -1 < window.location.search.indexOf("old") ? this.$strategy$ = new $module$build$ts$landscape$IndividualTilesetRenderingStrategy$default$$.$IndividualTilesetRenderingStrategy$() : this.$strategy$ = new $module$build$ts$landscape$LandscapeRenderingStrategy$default$$.$CombinedTilesetRenderingStrategy$();
};
$module$build$ts$data$Map$default$$.Map.get = function() {
  $module$build$ts$data$Map$default$$.Map.$v$ || ($module$build$ts$data$Map$default$$.Map.$v$ = new $module$build$ts$data$Map$default$$.Map());
  return $module$build$ts$data$Map$default$$.Map.$v$;
};
function $JSCompiler_StaticMethods_getTilesetForGid$$($JSCompiler_StaticMethods_getTilesetForGid$self$$, $value$jscomp$119$$) {
  return $value$jscomp$119$$ < $JSCompiler_StaticMethods_getTilesetForGid$self$$.$v$.length ? $JSCompiler_StaticMethods_getTilesetForGid$self$$.$v$[$value$jscomp$119$$] : $JSCompiler_StaticMethods_getTilesetForGid$self$$.$v$[$JSCompiler_StaticMethods_getTilesetForGid$self$$.$v$.length - 1] + 1;
}
$module$build$ts$data$Map$default$$.Map.prototype.$onTransfer$ = function($$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$, $content$jscomp$6_res$jscomp$inline_553$$) {
  this.$currentZoneName$ = $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$;
  this.$firstgids$ = [];
  this.$layers$ = [];
  this.$layerNames$ = [];
  document.getElementById("body").style.cursor = "wait";
  console.log("load map");
  var $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$ = $marauroa$$.$Deserializer$.$fromBase64$($content$jscomp$6_res$jscomp$inline_553$$.tilesets), $$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$ = $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$.$readInt$();
  this.$tilesetFilenames$ = [];
  for ($$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$ = 0; $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$ < $$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$; $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$++) {
    $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$.$readString$();
    var $baseFilename$jscomp$inline_548_source$jscomp$inline_546$$ = $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$.$readString$(), $$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$ = $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$.$readInt$();
    $baseFilename$jscomp$inline_548_source$jscomp$inline_546$$ = ("/" + $baseFilename$jscomp$inline_548_source$jscomp$inline_546$$.replace(/\.\.\/\.\.\//g, "")).replace(/\.png$/, "").replace("/tileset", $module$build$ts$data$Paths$default$$.$Paths$.$v$);
    !$JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.blood") && -1 < this.$A$.indexOf($baseFilename$jscomp$inline_548_source$jscomp$inline_546$$) ? this.$tilesetFilenames$.push($baseFilename$jscomp$inline_548_source$jscomp$inline_546$$ + "-safe.png") : this.$tilesetFilenames$.push($baseFilename$jscomp$inline_548_source$jscomp$inline_546$$ + ".png");
    this.$firstgids$.push($$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$);
  }
  this.$v$ = [];
  for ($deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$ = $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$ = 0; $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$ < parseInt(this.$firstgids$.length, 10); $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$++) {
    for (; $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$ < this.$firstgids$[$deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$]; $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$++) {
      this.$v$.push($deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$ - 1);
    }
    $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$ = this.$firstgids$[$deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$];
  }
  this.$strategy$.$A$();
  $JSCompiler_StaticMethods_decodeMapLayer$$(this, $content$jscomp$6_res$jscomp$inline_553$$, "0_floor");
  $JSCompiler_StaticMethods_decodeMapLayer$$(this, $content$jscomp$6_res$jscomp$inline_553$$, "1_terrain");
  $JSCompiler_StaticMethods_decodeMapLayer$$(this, $content$jscomp$6_res$jscomp$inline_553$$, "2_object");
  $JSCompiler_StaticMethods_decodeMapLayer$$(this, $content$jscomp$6_res$jscomp$inline_553$$, "3_roof");
  $JSCompiler_StaticMethods_decodeMapLayer$$(this, $content$jscomp$6_res$jscomp$inline_553$$, "4_roof_add");
  this.$C$ = $JSCompiler_StaticMethods_decodeMapLayer$$(this, $content$jscomp$6_res$jscomp$inline_553$$, "protection");
  this.$collisionData$ = $JSCompiler_StaticMethods_decodeMapLayer$$(this, $content$jscomp$6_res$jscomp$inline_553$$, "collision");
  $content$jscomp$6_res$jscomp$inline_553$$ = [];
  $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$ = $$jscomp$makeIterator$$(this.$B$);
  for ($$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$ = $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$.next(); !$$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$.done; $$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$ = $$jscomp$inline_554_i$jscomp$inline_545_lastStart$jscomp$inline_550_zoneName$$.next()) {
    $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$ = [];
    $$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$ = $$jscomp$makeIterator$$($$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$.value);
    for ($$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$ = $$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$.next(); !$$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$.done; $$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$ = $$jscomp$inline_555_$jscomp$inline_557_amount$jscomp$inline_544$$.next()) {
      $$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$ = this.$layerNames$.indexOf($$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$.value), -1 < $$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$ && $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$.push($$jscomp$key$layer$jscomp$inline_558_firstgid$jscomp$inline_547_index$jscomp$inline_559$$);
    }
    $deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$ && $content$jscomp$6_res$jscomp$inline_553$$.push($deserializer$jscomp$inline_543_group$jscomp$inline_556_pos$jscomp$inline_549$$);
  }
  this.$layerGroupIndexes$ = $content$jscomp$6_res$jscomp$inline_553$$;
  this.$strategy$.$v$(this);
};
function $JSCompiler_StaticMethods_decodeMapLayer$$($JSCompiler_StaticMethods_decodeMapLayer$self$$, $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$, $name$jscomp$85$$) {
  if ($content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$ = $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$[$name$jscomp$85$$]) {
    $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$ = $marauroa$$.$Deserializer$.$fromDeflatedBase64$($content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$);
    $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readString$();
    $JSCompiler_StaticMethods_decodeMapLayer$self$$.$zoneSizeX$ = $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readInt$();
    $JSCompiler_StaticMethods_decodeMapLayer$self$$.$zoneSizeY$ = $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readInt$();
    $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$ = $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readByteArray$();
    for (var $layer$jscomp$12$$ = [], $i$jscomp$67$$ = 0; $i$jscomp$67$$ < $JSCompiler_StaticMethods_decodeMapLayer$self$$.$zoneSizeX$ * $JSCompiler_StaticMethods_decodeMapLayer$self$$.$zoneSizeY$ * 4 - 3; $i$jscomp$67$$ += 4) {
      var $tileId$$ = $content$jscomp$8_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.getUint32($i$jscomp$67$$, !0);
      $layer$jscomp$12$$.push($tileId$$);
    }
    $JSCompiler_StaticMethods_decodeMapLayer$self$$.$layerNames$.push($name$jscomp$85$$);
    $JSCompiler_StaticMethods_decodeMapLayer$self$$.$layers$.push($layer$jscomp$12$$);
    return $layer$jscomp$12$$;
  }
}
function $JSCompiler_StaticMethods_collision$$($x$jscomp$113$$, $y$jscomp$93$$) {
  var $JSCompiler_StaticMethods_collision$self$$ = $stendhal$$.data.map;
  return 0 != $JSCompiler_StaticMethods_collision$self$$.$collisionData$[$y$jscomp$93$$ * $JSCompiler_StaticMethods_collision$self$$.$zoneSizeX$ + $x$jscomp$113$$];
}
;var $module$build$ts$data$OutfitStore$default$$ = {};
Object.defineProperty($module$build$ts$data$OutfitStore$default$$, "__esModule", {value:!0});
$module$build$ts$data$OutfitStore$default$$.$OutfitStore$ = void 0;
$module$build$ts$data$OutfitStore$default$$.$OutfitStore$ = function() {
  this.$detailRearLayers$ = [];
  this.count = {hat:19, hair:57, mask:9, eyes:28, mouth:5, head:4, dress:65, body:3};
  this.$v$ = [1, 4, 6, 7, 10, 11, 13, 16, 29, 37, 40, 53, 54, 56, 61, 64, 967, 968, 977, 980, 989, 990, 999];
  this.$hats_no_hair$ = [3, 4, 13, 16, 992, 993, 994, 996, 997];
};
$module$build$ts$data$OutfitStore$default$$.$OutfitStore$.get = function() {
  $module$build$ts$data$OutfitStore$default$$.$OutfitStore$.$v$ || ($module$build$ts$data$OutfitStore$default$$.$OutfitStore$.$v$ = new $module$build$ts$data$OutfitStore$default$$.$OutfitStore$());
  return $module$build$ts$data$OutfitStore$default$$.$OutfitStore$.$v$;
};
$module$build$ts$data$OutfitStore$default$$.$OutfitStore$.prototype.$init$ = function() {
  var $$jscomp$this$jscomp$16$$ = this, $loader$jscomp$2$$ = new $module$build$ts$util$JSONLoader$default$$.$JSONLoader$();
  $loader$jscomp$2$$.$v$ = function() {
    $$jscomp$this$jscomp$16$$.$detailRearLayers$ = $loader$jscomp$2$$.data.detail.rear;
  };
  $loader$jscomp$2$$.load($module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/outfit/outfits.json");
};
var $module$build$ts$data$SpriteStore$default$$ = {};
Object.defineProperty($module$build$ts$data$SpriteStore$default$$, "__esModule", {value:!0});
$module$build$ts$data$SpriteStore$default$$.store = $module$build$ts$data$SpriteStore$default$$.$SpriteStore$ = void 0;
$module$build$ts$data$SpriteStore$default$$.$SpriteStore$ = function() {
  this.$v$ = {};
  this.images = {};
  this.$knownShadows$ = {"24x32":!0, "32x32":!0, "32x48":!0, "32x48_long":!0, "48x64":!0, "48x64_float":!0, "64x48":!0, "64x64":!0, "64x85":!0, "64x96":!0, "76x64":!0, "81x96":!0, "96x96":!0, "96x128":!0, "128x96":!0, "128x170":!0, "144x128":!0, "168x224":!0, "192x192":!0, "192x192_float":!0, "192x256":!0, "320x440":!0, ent:!0};
  var $$jscomp$compprop1$$ = {};
  this.$knownSafeSprites$ = ($$jscomp$compprop1$$[$module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/monsters/huge_animal/thing"] = !0, $$jscomp$compprop1$$[$module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/monsters/mutant/imperial_mutant"] = !0, $$jscomp$compprop1$$[$module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/monsters/undead/bloody_zombie"] = !0, $$jscomp$compprop1$$[$module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/npc/deadmannpc"] = !0, $$jscomp$compprop1$$);
  this.$animations$ = {$idea$:{love:{delay:100, offsetX:24, offsetY:-8}}};
  this.filter = {$splitrgb$:function($rgb$$) {
    $rgb$$ &= 16777215;
    var $b$jscomp$34$$ = $rgb$$ & 255;
    $rgb$$ >>>= 8;
    return [$rgb$$ >>> 8, $rgb$$ & 255, $b$jscomp$34$$];
  }, $mergergb$:function($rgbArray$$) {
    return 16777215 & ($rgbArray$$[0] << 16 | $rgbArray$$[1] << 8 | $rgbArray$$[2]);
  }, $rgb2hsl$:function($l$jscomp$17_rgb$jscomp$1$$) {
    var $h$jscomp$25_r$jscomp$10$$ = $l$jscomp$17_rgb$jscomp$1$$[0] / 255, $g$jscomp$19$$ = $l$jscomp$17_rgb$jscomp$1$$[1] / 255, $b$jscomp$35$$ = $l$jscomp$17_rgb$jscomp$1$$[2] / 255;
    if ($h$jscomp$25_r$jscomp$10$$ > $g$jscomp$19$$) {
      var $max_s$jscomp$7$$ = $h$jscomp$25_r$jscomp$10$$;
      var $min$$ = $g$jscomp$19$$;
      var $maxVar$$ = 0;
    } else {
      $max_s$jscomp$7$$ = $g$jscomp$19$$, $min$$ = $h$jscomp$25_r$jscomp$10$$, $maxVar$$ = 1;
    }
    $b$jscomp$35$$ > $max_s$jscomp$7$$ ? ($max_s$jscomp$7$$ = $b$jscomp$35$$, $maxVar$$ = 2) : $b$jscomp$35$$ < $min$$ && ($min$$ = $b$jscomp$35$$);
    $l$jscomp$17_rgb$jscomp$1$$ = ($max_s$jscomp$7$$ + $min$$) / 2;
    var $diff$jscomp$2$$ = $max_s$jscomp$7$$ - $min$$;
    0.000001 > $diff$jscomp$2$$ ? $h$jscomp$25_r$jscomp$10$$ = $max_s$jscomp$7$$ = 0 : ($max_s$jscomp$7$$ = 0.5 > $l$jscomp$17_rgb$jscomp$1$$ ? $diff$jscomp$2$$ / ($max_s$jscomp$7$$ + $min$$) : $diff$jscomp$2$$ / (2 - $max_s$jscomp$7$$ - $min$$), $h$jscomp$25_r$jscomp$10$$ = (0 === $maxVar$$ ? ($g$jscomp$19$$ - $b$jscomp$35$$) / $diff$jscomp$2$$ : 1 === $maxVar$$ ? 2 + ($b$jscomp$35$$ - $h$jscomp$25_r$jscomp$10$$) / $diff$jscomp$2$$ : 4 + ($h$jscomp$25_r$jscomp$10$$ - $g$jscomp$19$$) / $diff$jscomp$2$$) / 
    6);
    return [$h$jscomp$25_r$jscomp$10$$, $max_s$jscomp$7$$, $l$jscomp$17_rgb$jscomp$1$$];
  }, $hsl2rgb$:function($g$jscomp$20_hsl_tmp1$$) {
    var $h$jscomp$26_r$jscomp$11$$ = $g$jscomp$20_hsl_tmp1$$[0];
    var $b$jscomp$36_bf_s$jscomp$8$$ = $g$jscomp$20_hsl_tmp1$$[1];
    var $gf_l$jscomp$18$$ = $g$jscomp$20_hsl_tmp1$$[2];
    if (0.0000001 > $b$jscomp$36_bf_s$jscomp$8$$) {
      $h$jscomp$26_r$jscomp$11$$ = $g$jscomp$20_hsl_tmp1$$ = $b$jscomp$36_bf_s$jscomp$8$$ = Math.floor(255 * $gf_l$jscomp$18$$);
    } else {
      $g$jscomp$20_hsl_tmp1$$ = 0.5 > $gf_l$jscomp$18$$ ? $gf_l$jscomp$18$$ * (1 + $b$jscomp$36_bf_s$jscomp$8$$) : $gf_l$jscomp$18$$ + $b$jscomp$36_bf_s$jscomp$8$$ - $gf_l$jscomp$18$$ * $b$jscomp$36_bf_s$jscomp$8$$;
      var $tmp2$$ = 2 * $gf_l$jscomp$18$$ - $g$jscomp$20_hsl_tmp1$$;
      $gf_l$jscomp$18$$ = $stendhal$$.data.$sprites$.filter.$hue2rgb$(this.$limitHue$($h$jscomp$26_r$jscomp$11$$), $tmp2$$, $g$jscomp$20_hsl_tmp1$$);
      $b$jscomp$36_bf_s$jscomp$8$$ = $stendhal$$.data.$sprites$.filter.$hue2rgb$(this.$limitHue$($h$jscomp$26_r$jscomp$11$$ - 1 / 3), $tmp2$$, $g$jscomp$20_hsl_tmp1$$);
      $h$jscomp$26_r$jscomp$11$$ = Math.floor(255 * $stendhal$$.data.$sprites$.filter.$hue2rgb$(this.$limitHue$($h$jscomp$26_r$jscomp$11$$ + 1 / 3), $tmp2$$, $g$jscomp$20_hsl_tmp1$$)) & 255;
      $g$jscomp$20_hsl_tmp1$$ = Math.floor(255 * $gf_l$jscomp$18$$) & 255;
      $b$jscomp$36_bf_s$jscomp$8$$ = Math.floor(255 * $b$jscomp$36_bf_s$jscomp$8$$) & 255;
    }
    return [$h$jscomp$26_r$jscomp$11$$, $g$jscomp$20_hsl_tmp1$$, $b$jscomp$36_bf_s$jscomp$8$$];
  }, $hue2rgb$:function($hue$$, $val1$$, $val2$$) {
    var $res$jscomp$3$$ = $hue$$;
    1 > 6 * $hue$$ ? $res$jscomp$3$$ = $val1$$ + 6 * ($val2$$ - $val1$$) * $hue$$ : 1 > 2 * $hue$$ ? $res$jscomp$3$$ = $val2$$ : 2 > 3 * $hue$$ ? $res$jscomp$3$$ = $val1$$ + ($val2$$ - $val1$$) * (2 / 3 - $hue$$) * 6 : $res$jscomp$3$$ = $val1$$;
    return $res$jscomp$3$$;
  }, $limitHue$:function($hue$jscomp$1_res$jscomp$4$$) {
    0 > $hue$jscomp$1_res$jscomp$4$$ ? $hue$jscomp$1_res$jscomp$4$$ += 1 : 1 < $hue$jscomp$1_res$jscomp$4$$ && --$hue$jscomp$1_res$jscomp$4$$;
    return $hue$jscomp$1_res$jscomp$4$$;
  }};
};
$module$build$ts$data$SpriteStore$default$$.$SpriteStore$.prototype.get = function($filename$jscomp$6$$) {
  if (!$filename$jscomp$6$$) {
    return {};
  }
  if (-1 < $filename$jscomp$6$$.indexOf("undefined")) {
    return this.$v$[$filename$jscomp$6$$] || console.log("Broken image path: ", $filename$jscomp$6$$, Error()), this.$v$[$filename$jscomp$6$$] = !0, {};
  }
  if (this.images[$filename$jscomp$6$$]) {
    return this.images[$filename$jscomp$6$$].counter++, this.images[$filename$jscomp$6$$];
  }
  var $temp$jscomp$1$$ = new Image();
  $temp$jscomp$1$$.counter = 0;
  $temp$jscomp$1$$.onerror = function($t$jscomp$3$$, $store$jscomp$1$$) {
    return function() {
      $t$jscomp$3$$.src && !$store$jscomp$1$$.$v$[$t$jscomp$3$$.src] && (console.log("Broken image path:", $t$jscomp$3$$.src, Error()), $store$jscomp$1$$.$v$[$t$jscomp$3$$.src] = !0);
      var $failsafe$$ = $JSCompiler_StaticMethods_getFailsafe$$($store$jscomp$1$$);
      $failsafe$$.src && $t$jscomp$3$$.src !== $failsafe$$.src && ($t$jscomp$3$$.src = $failsafe$$.src);
    };
  }($temp$jscomp$1$$, this);
  $temp$jscomp$1$$.src = $filename$jscomp$6$$;
  return this.images[$filename$jscomp$6$$] = $temp$jscomp$1$$;
};
function $JSCompiler_StaticMethods_getWithPromise$$($filename$jscomp$7$$) {
  var $JSCompiler_StaticMethods_getWithPromise$self$$ = $stendhal$$.data.$sprites$;
  return new Promise(function($resolve$jscomp$4$$) {
    "undefined" != typeof $JSCompiler_StaticMethods_getWithPromise$self$$.images[$filename$jscomp$7$$] && ($JSCompiler_StaticMethods_getWithPromise$self$$.images[$filename$jscomp$7$$].counter++, $resolve$jscomp$4$$($JSCompiler_StaticMethods_getWithPromise$self$$.images[$filename$jscomp$7$$]));
    var $image$jscomp$6$$ = new Image();
    $image$jscomp$6$$.counter = 0;
    $JSCompiler_StaticMethods_getWithPromise$self$$.images[$filename$jscomp$7$$] = $image$jscomp$6$$;
    $image$jscomp$6$$.onload = function() {
      return $resolve$jscomp$4$$($image$jscomp$6$$);
    };
    $image$jscomp$6$$.src = $filename$jscomp$7$$;
  });
}
$module$build$ts$data$SpriteStore$default$$.$SpriteStore$.prototype.rotate = function($img$jscomp$6$$, $angle$jscomp$3$$) {
  var $canvas$jscomp$5$$ = document.getElementById("drawing-stage"), $ctx$jscomp$5$$ = $canvas$jscomp$5$$.getContext("2d");
  $ctx$jscomp$5$$.clearRect(0, 0, $canvas$jscomp$5$$.width, $canvas$jscomp$5$$.height);
  $canvas$jscomp$5$$.width = $img$jscomp$6$$.width;
  $canvas$jscomp$5$$.height = $img$jscomp$6$$.height;
  $ctx$jscomp$5$$.translate($canvas$jscomp$5$$.width / 2, $canvas$jscomp$5$$.height / 2);
  $ctx$jscomp$5$$.rotate($angle$jscomp$3$$ * Math.PI / 180);
  $ctx$jscomp$5$$.translate(-$canvas$jscomp$5$$.width / 2, -$canvas$jscomp$5$$.height / 2);
  $ctx$jscomp$5$$.drawImage($img$jscomp$6$$, 0, 0);
  $img$jscomp$6$$.src = $canvas$jscomp$5$$.toDataURL("image/png");
};
function $JSCompiler_StaticMethods_getRotated$$($filename$jscomp$8$$, $angle$jscomp$4$$) {
  var $JSCompiler_StaticMethods_getRotated$self$$ = $stendhal$$.data.$sprites$;
  if (0 == $angle$jscomp$4$$) {
    return $JSCompiler_StaticMethods_getRotated$self$$.get($filename$jscomp$8$$);
  }
  var $id$jscomp$26$$ = $filename$jscomp$8$$ + "-rot" + $angle$jscomp$4$$;
  if ($JSCompiler_StaticMethods_getRotated$self$$.images[$id$jscomp$26$$]) {
    return $JSCompiler_StaticMethods_getRotated$self$$.images[$id$jscomp$26$$];
  }
  var $img$jscomp$7$$ = $JSCompiler_StaticMethods_getRotated$self$$.get($filename$jscomp$8$$).cloneNode();
  $img$jscomp$7$$.complete ? $JSCompiler_StaticMethods_getRotated$self$$.rotate($img$jscomp$7$$, $angle$jscomp$4$$) : $img$jscomp$7$$.onload = function() {
    $JSCompiler_StaticMethods_getRotated$self$$.rotate($img$jscomp$7$$, $angle$jscomp$4$$);
    $img$jscomp$7$$.onload = void 0;
  };
  return $JSCompiler_StaticMethods_getRotated$self$$.images[$id$jscomp$26$$] = $img$jscomp$7$$;
}
function $JSCompiler_StaticMethods_getFailsafe$$($JSCompiler_StaticMethods_getFailsafe$self$$) {
  var $filename$jscomp$10$$ = $module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/failsafe.png", $failsafe$jscomp$1$$ = $JSCompiler_StaticMethods_getFailsafe$self$$.images[$filename$jscomp$10$$];
  $failsafe$jscomp$1$$ ? $failsafe$jscomp$1$$.counter++ : ($failsafe$jscomp$1$$ = new Image(), $failsafe$jscomp$1$$.counter = 0, $failsafe$jscomp$1$$.src = $filename$jscomp$10$$, $JSCompiler_StaticMethods_getFailsafe$self$$.images[$filename$jscomp$10$$] = $failsafe$jscomp$1$$);
  return $failsafe$jscomp$1$$;
}
function $JSCompiler_StaticMethods_getAreaOf$$($JSCompiler_StaticMethods_getAreaOf$self$$, $image$jscomp$7$$, $width$jscomp$28$$, $height$jscomp$25$$, $offsetX$jscomp$1$$, $offsetY$jscomp$1$$) {
  try {
    $offsetX$jscomp$1$$ = $offsetX$jscomp$1$$ || 0;
    $offsetY$jscomp$1$$ = $offsetY$jscomp$1$$ || 0;
    if ($image$jscomp$7$$.width === $width$jscomp$28$$ && $image$jscomp$7$$.height === $height$jscomp$25$$ && 0 === $offsetX$jscomp$1$$ && 0 === $offsetY$jscomp$1$$) {
      return $image$jscomp$7$$;
    }
    var $canvas$jscomp$6$$ = document.createElement("canvas");
    $canvas$jscomp$6$$.width = $width$jscomp$28$$;
    $canvas$jscomp$6$$.height = $height$jscomp$25$$;
    $canvas$jscomp$6$$.getContext("2d").drawImage($image$jscomp$7$$, $offsetX$jscomp$1$$, $offsetY$jscomp$1$$, $width$jscomp$28$$, $height$jscomp$25$$, 0, 0, $width$jscomp$28$$, $height$jscomp$25$$);
    var $newImage$$ = new Image();
    $newImage$$.src = $canvas$jscomp$6$$.toDataURL("image/png");
    return $newImage$$;
  } catch ($err$jscomp$7$$) {
    if ($err$jscomp$7$$ instanceof DOMException) {
      return $JSCompiler_StaticMethods_getFailsafe$$($JSCompiler_StaticMethods_getAreaOf$self$$);
    }
    throw $err$jscomp$7$$;
  }
}
function $JSCompiler_StaticMethods_getFilteredWithPromise$$($fileName$jscomp$1$$, $param$jscomp$7$$) {
  return $JSCompiler_StaticMethods_getWithPromise$$($fileName$jscomp$1$$).then(function($img$jscomp$9_imgData$jscomp$1$$) {
    var $filterFn$jscomp$1$$;
    if (!($filterFn$jscomp$1$$ = $stendhal$$.data.$sprites$.filter.trueColor) || !$img$jscomp$9_imgData$jscomp$1$$.complete || 0 === $img$jscomp$9_imgData$jscomp$1$$.width || 0 === $img$jscomp$9_imgData$jscomp$1$$.height) {
      return $img$jscomp$9_imgData$jscomp$1$$;
    }
    var $filteredName$jscomp$1$$ = $fileName$jscomp$1$$ + " trueColor " + $param$jscomp$7$$, $canvas$jscomp$8_filtered$jscomp$1$$ = $stendhal$$.data.$sprites$.images[$filteredName$jscomp$1$$];
    if ("undefined" === typeof $canvas$jscomp$8_filtered$jscomp$1$$) {
      $canvas$jscomp$8_filtered$jscomp$1$$ = document.createElement("canvas");
      $canvas$jscomp$8_filtered$jscomp$1$$.width = $img$jscomp$9_imgData$jscomp$1$$.width;
      $canvas$jscomp$8_filtered$jscomp$1$$.height = $img$jscomp$9_imgData$jscomp$1$$.height;
      var $ctx$jscomp$8$$ = $canvas$jscomp$8_filtered$jscomp$1$$.getContext("2d");
      $ctx$jscomp$8$$.drawImage($img$jscomp$9_imgData$jscomp$1$$, 0, 0);
      $img$jscomp$9_imgData$jscomp$1$$ = $ctx$jscomp$8$$.getImageData(0, 0, $img$jscomp$9_imgData$jscomp$1$$.width, $img$jscomp$9_imgData$jscomp$1$$.height);
      $filterFn$jscomp$1$$($img$jscomp$9_imgData$jscomp$1$$.data, $param$jscomp$7$$);
      $ctx$jscomp$8$$.putImageData($img$jscomp$9_imgData$jscomp$1$$, 0, 0);
      $canvas$jscomp$8_filtered$jscomp$1$$.complete = !0;
      $stendhal$$.data.$sprites$.images[$filteredName$jscomp$1$$] = $canvas$jscomp$8_filtered$jscomp$1$$;
    }
    return $canvas$jscomp$8_filtered$jscomp$1$$;
  });
}
function $JSCompiler_StaticMethods_getShadow$$($shadowStyle$$) {
  if ($stendhal$$.data.$sprites$.$knownShadows$[$shadowStyle$$]) {
    var $img$jscomp$10$$ = new Image();
    $img$jscomp$10$$.src = $module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/shadow/" + $shadowStyle$$ + ".png";
    return $img$jscomp$10$$;
  }
}
$module$build$ts$data$SpriteStore$default$$.$SpriteStore$.prototype.$startupCache$ = function() {
  this.get($module$build$ts$data$Paths$default$$.$Paths$.$sprites$ + "/npc/floattingladynpc.png");
  this.get($module$build$ts$data$Paths$default$$.$Paths$.$gui$ + "/banner_background.png");
  for (var $$jscomp$iter$48_$jscomp$iter$49$$ = $$jscomp$makeIterator$$("commerce deathmatch experience fighting friend interior_zone item obtain outside_zone production quest quest_ados_items quest_kill_blordroughs quest_kirdneh_item quest_mithrilbourgh_enemy_army quest_semos_monster special underground_zone".split(" ")), $$jscomp$key$cat_$jscomp$key$weather$$ = $$jscomp$iter$48_$jscomp$iter$49$$.next(); !$$jscomp$key$cat_$jscomp$key$weather$$.done; $$jscomp$key$cat_$jscomp$key$weather$$ = $$jscomp$iter$48_$jscomp$iter$49$$.next()) {
    this.get($module$build$ts$data$Paths$default$$.$Paths$.$achievements$ + "/" + $$jscomp$key$cat_$jscomp$key$weather$$.value + ".png");
  }
  $$jscomp$iter$48_$jscomp$iter$49$$ = $$jscomp$makeIterator$$("fog fog_heavy rain rain_heavy rain_light snow snow_heavy snow_light wave".split(" "));
  for ($$jscomp$key$cat_$jscomp$key$weather$$ = $$jscomp$iter$48_$jscomp$iter$49$$.next(); !$$jscomp$key$cat_$jscomp$key$weather$$.done; $$jscomp$key$cat_$jscomp$key$weather$$ = $$jscomp$iter$48_$jscomp$iter$49$$.next()) {
    this.get($module$build$ts$data$Paths$default$$.$Paths$.$weather$ + "/" + $$jscomp$key$cat_$jscomp$key$weather$$.value + ".png");
  }
};
var $SpriteStoreInternal$$module$build$ts$data$SpriteStore$instance$$;
$SpriteStoreInternal$$module$build$ts$data$SpriteStore$instance$$ || ($SpriteStoreInternal$$module$build$ts$data$SpriteStore$instance$$ = new $module$build$ts$data$SpriteStore$default$$.$SpriteStore$());
$module$build$ts$data$SpriteStore$default$$.store = $SpriteStoreInternal$$module$build$ts$data$SpriteStore$instance$$;
$module$build$ts$data$SpriteStore$default$$.store.filter.trueColor = function($data$jscomp$85$$, $color$jscomp$3_hslColor$$) {
  $color$jscomp$3_hslColor$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$($stendhal$$.data.$sprites$.filter.$splitrgb$($color$jscomp$3_hslColor$$));
  for (var $end$jscomp$10$$ = $data$jscomp$85$$.length, $i$jscomp$69$$ = 0; $i$jscomp$69$$ < $end$jscomp$10$$; $i$jscomp$69$$ += 4) {
    var $hsl$jscomp$1_resultRgb$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$([$data$jscomp$85$$[$i$jscomp$69$$], $data$jscomp$85$$[$i$jscomp$69$$ + 1], $data$jscomp$85$$[$i$jscomp$69$$ + 2]]), $tmp$jscomp$7$$ = $hsl$jscomp$1_resultRgb$$[2] - 0.5;
    $hsl$jscomp$1_resultRgb$$ = $stendhal$$.data.$sprites$.filter.$hsl2rgb$([$color$jscomp$3_hslColor$$[0], $color$jscomp$3_hslColor$$[1], $hsl$jscomp$1_resultRgb$$[2] - 2.0 * ($color$jscomp$3_hslColor$$[2] - 0.5) * ($tmp$jscomp$7$$ * $tmp$jscomp$7$$ - 0.25)]);
    $data$jscomp$85$$[$i$jscomp$69$$] = $hsl$jscomp$1_resultRgb$$[0];
    $data$jscomp$85$$[$i$jscomp$69$$ + 1] = $hsl$jscomp$1_resultRgb$$[1];
    $data$jscomp$85$$[$i$jscomp$69$$ + 2] = $hsl$jscomp$1_resultRgb$$[2];
  }
};
var $module$build$ts$ui$HTMLManager$default$$ = {};
Object.defineProperty($module$build$ts$ui$HTMLManager$default$$, "__esModule", {value:!0});
$module$build$ts$ui$HTMLManager$default$$.$HTMLManager$ = void 0;
$module$build$ts$ui$HTMLManager$default$$.$HTMLManager$ = function() {
};
$module$build$ts$ui$HTMLManager$default$$.$HTMLManager$.get = function() {
  $module$build$ts$ui$HTMLManager$default$$.$HTMLManager$.$v$ || ($module$build$ts$ui$HTMLManager$default$$.$HTMLManager$.$v$ = new $module$build$ts$ui$HTMLManager$default$$.$HTMLManager$());
  return $module$build$ts$ui$HTMLManager$default$$.$HTMLManager$.$v$;
};
function $JSCompiler_StaticMethods_esc$$($msg$jscomp$20$$, $$jscomp$iter$50_filter$jscomp$5$$) {
  $$jscomp$iter$50_filter$jscomp$5$$ = void 0 === $$jscomp$iter$50_filter$jscomp$5$$ ? [] : $$jscomp$iter$50_filter$jscomp$5$$;
  $msg$jscomp$20$$ = $msg$jscomp$20$$.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/\n/g, "<br>");
  $$jscomp$iter$50_filter$jscomp$5$$ = $$jscomp$makeIterator$$($$jscomp$iter$50_filter$jscomp$5$$);
  for (var $$jscomp$key$tag_tag$jscomp$1$$ = $$jscomp$iter$50_filter$jscomp$5$$.next(); !$$jscomp$key$tag_tag$jscomp$1$$.done; $$jscomp$key$tag_tag$jscomp$1$$ = $$jscomp$iter$50_filter$jscomp$5$$.next()) {
    $$jscomp$key$tag_tag$jscomp$1$$ = $$jscomp$key$tag_tag$jscomp$1$$.value, $msg$jscomp$20$$ = $msg$jscomp$20$$.replace("&lt;" + $$jscomp$key$tag_tag$jscomp$1$$ + "&gt;", "<" + $$jscomp$key$tag_tag$jscomp$1$$ + ">").replace("&lt;/" + $$jscomp$key$tag_tag$jscomp$1$$ + "&gt;", "</" + $$jscomp$key$tag_tag$jscomp$1$$ + ">");
  }
  return $msg$jscomp$20$$;
}
function $JSCompiler_StaticMethods_extractPosition$$($canvas$jscomp$9_event$jscomp$45$$) {
  var $pos$jscomp$4$$ = $canvas$jscomp$9_event$jscomp$45$$;
  $canvas$jscomp$9_event$jscomp$45$$.changedTouches && ($pos$jscomp$4$$ = {pageX:Math.round($canvas$jscomp$9_event$jscomp$45$$.changedTouches[0].pageX), pageY:Math.round($canvas$jscomp$9_event$jscomp$45$$.changedTouches[0].pageY), target:$canvas$jscomp$9_event$jscomp$45$$.changedTouches[0].target}, $pos$jscomp$4$$.offsetX = $pos$jscomp$4$$.pageX - $canvas$jscomp$9_event$jscomp$45$$.changedTouches[0].target.offsetLeft, $pos$jscomp$4$$.offsetY = $pos$jscomp$4$$.pageY - $canvas$jscomp$9_event$jscomp$45$$.changedTouches[0].target.offsetTop);
  $canvas$jscomp$9_event$jscomp$45$$ = $canvas$jscomp$9_event$jscomp$45$$.target;
  $pos$jscomp$4$$.$canvasRelativeX$ = Math.round($pos$jscomp$4$$.offsetX * $canvas$jscomp$9_event$jscomp$45$$.width / $canvas$jscomp$9_event$jscomp$45$$.clientWidth);
  $pos$jscomp$4$$.$canvasRelativeY$ = Math.round($pos$jscomp$4$$.offsetY * $canvas$jscomp$9_event$jscomp$45$$.height / $canvas$jscomp$9_event$jscomp$45$$.clientHeight);
  return $pos$jscomp$4$$;
}
;var $module$build$ts$ui$Inventory$default$$ = {};
Object.defineProperty($module$build$ts$ui$Inventory$default$$, "__esModule", {value:!0});
$module$build$ts$ui$Inventory$default$$.$Inventory$ = void 0;
$module$build$ts$ui$Inventory$default$$.$Inventory$ = function() {
  this.$v$ = [];
};
$module$build$ts$ui$Inventory$default$$.$Inventory$.get = function() {
  $module$build$ts$ui$Inventory$default$$.$Inventory$.$v$ || ($module$build$ts$ui$Inventory$default$$.$Inventory$.$v$ = new $module$build$ts$ui$Inventory$default$$.$Inventory$());
  return $module$build$ts$ui$Inventory$default$$.$Inventory$.$v$;
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$ui$Inventory$default$$.$Inventory$.prototype;
$JSCompiler_prototypeAlias$$.update = function() {
  for (var $i$jscomp$70$$ in this.$v$) {
    this.$v$[$i$jscomp$70$$].update();
  }
};
$JSCompiler_prototypeAlias$$.$getInventory$ = function() {
  return this.$v$;
};
$JSCompiler_prototypeAlias$$.add = function($comp$$) {
  this.$v$.push($comp$$);
};
$JSCompiler_prototypeAlias$$.remove = function($comp$jscomp$1_idx$jscomp$16$$) {
  $comp$jscomp$1_idx$jscomp$16$$ = this.$v$.indexOf($comp$jscomp$1_idx$jscomp$16$$);
  -1 < $comp$jscomp$1_idx$jscomp$16$$ && this.$v$.splice($comp$jscomp$1_idx$jscomp$16$$, 1);
};
$JSCompiler_prototypeAlias$$.indexOf = function($comp$jscomp$2$$) {
  return this.$v$.indexOf($comp$jscomp$2$$);
};
var $module$build$ts$ui$KeyHandler$default$$ = {};
Object.defineProperty($module$build$ts$ui$KeyHandler$default$$, "__esModule", {value:!0});
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$ = void 0;
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$ = function() {
};
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$D$ = function() {
  for (var $$jscomp$iter$51$$ = $$jscomp$makeIterator$$(Object.keys($module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$).map(function($key$jscomp$79$$) {
    return $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$[$key$jscomp$79$$];
  })), $$jscomp$key$dir$$ = $$jscomp$iter$51$$.next(); !$$jscomp$key$dir$$.done; $$jscomp$key$dir$$ = $$jscomp$iter$51$$.next()) {
    if (-1 < $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$.indexOf($$jscomp$key$dir$$.value)) {
      return !0;
    }
  }
  return !1;
};
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$C$ = function($event$jscomp$46$$) {
  return $event$jscomp$46$$.ctrlKey ? "face" : $event$jscomp$46$$.shiftKey ? null : "move";
};
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$B$ = function($code$jscomp$2_dir$jscomp$1$$) {
  $code$jscomp$2_dir$jscomp$1$$ -= $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$.left;
  0 === $code$jscomp$2_dir$jscomp$1$$ && ($code$jscomp$2_dir$jscomp$1$$ = 4);
  return $code$jscomp$2_dir$jscomp$1$$;
};
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$onKeyDown$ = function($code$jscomp$3_dir$jscomp$2_e$jscomp$57$$) {
  var $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$ = $code$jscomp$3_dir$jscomp$2_e$jscomp$57$$;
  $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$ || ($JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$ = window.event);
  if ($JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$) {
    if ($code$jscomp$3_dir$jscomp$2_e$jscomp$57$$ = $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.which ? $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.which : $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.keyCode, $code$jscomp$3_dir$jscomp$2_e$jscomp$57$$ >= $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$.left && $code$jscomp$3_dir$jscomp$2_e$jscomp$57$$ <= $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$.$down$) {
      var $target$jscomp$110$$ = $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.target;
      "BODY" !== $target$jscomp$110$$.tagName && "CANVAS" !== $target$jscomp$110$$.tagName || $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.preventDefault();
      if (!(-1 < $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$.indexOf($code$jscomp$3_dir$jscomp$2_e$jscomp$57$$)) && ($module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$.push($code$jscomp$3_dir$jscomp$2_e$jscomp$57$$), $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$ = $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$C$($JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$))) {
        $code$jscomp$3_dir$jscomp$2_e$jscomp$57$$ = $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$B$($code$jscomp$3_dir$jscomp$2_e$jscomp$57$$);
        $marauroa$$.$clientFramework$.$sendAction$({type:$JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$, dir:"" + $code$jscomp$3_dir$jscomp$2_e$jscomp$57$$});
        if ($JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$ = $marauroa$$.$me$) {
          $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$ = "undefined" !== typeof $marauroa$$.$me$.autowalk;
        }
        $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$ && parseInt($marauroa$$.$me$.dir, 10) === $code$jscomp$3_dir$jscomp$2_e$jscomp$57$$ && $marauroa$$.$clientFramework$.$sendAction$({type:"walk"});
      }
    } else {
      $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.altKey || $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.metaKey || $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.ctrlKey || "Control" === $JSCompiler_temp$jscomp$209_event$jscomp$47_type$jscomp$210$$.key || document.activeElement && "input" !== document.activeElement.localName && document.getElementById("chatinput").focus();
    }
  }
};
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$onKeyUp$ = function($action$jscomp$58_e$jscomp$58_i$jscomp$71$$) {
  var $event$jscomp$48_type$jscomp$211$$ = $action$jscomp$58_e$jscomp$58_i$jscomp$71$$;
  $event$jscomp$48_type$jscomp$211$$ || ($event$jscomp$48_type$jscomp$211$$ = window.event);
  if ($event$jscomp$48_type$jscomp$211$$) {
    var $code$jscomp$4_dir$jscomp$3$$ = $event$jscomp$48_type$jscomp$211$$.which ? $event$jscomp$48_type$jscomp$211$$.which : $event$jscomp$48_type$jscomp$211$$.keyCode;
    $code$jscomp$4_dir$jscomp$3$$ >= $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$.left && $code$jscomp$4_dir$jscomp$3$$ <= $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$.$down$ && ($action$jscomp$58_e$jscomp$58_i$jscomp$71$$ = $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$.indexOf($code$jscomp$4_dir$jscomp$3$$), -1 < $action$jscomp$58_e$jscomp$58_i$jscomp$71$$ && $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$.splice($action$jscomp$58_e$jscomp$58_i$jscomp$71$$, 
    1), $action$jscomp$58_e$jscomp$58_i$jscomp$71$$ = {}, $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$D$() || ($action$jscomp$58_e$jscomp$58_i$jscomp$71$$.type = "stop", $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$58_e$jscomp$58_i$jscomp$71$$)), 0 < $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$.length && ($code$jscomp$4_dir$jscomp$3$$ = $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$[0], $event$jscomp$48_type$jscomp$211$$ = $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$C$($event$jscomp$48_type$jscomp$211$$))) && 
    ($code$jscomp$4_dir$jscomp$3$$ = $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$B$($code$jscomp$4_dir$jscomp$3$$), $action$jscomp$58_e$jscomp$58_i$jscomp$71$$.type = $event$jscomp$48_type$jscomp$211$$, $action$jscomp$58_e$jscomp$58_i$jscomp$71$$.dir = "" + $code$jscomp$4_dir$jscomp$3$$, $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$58_e$jscomp$58_i$jscomp$71$$));
  }
};
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$ = {left:37, $up$:38, right:39, $down$:40};
$module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$A$ = [];
var $module$build$ts$entity$RPObject$default$$ = {};
Object.defineProperty($module$build$ts$entity$RPObject$default$$, "__esModule", {value:!0});
$module$build$ts$entity$RPObject$default$$.$RPObject$ = void 0;
$module$build$ts$entity$RPObject$default$$.$RPObject$ = function() {
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$RPObject$default$$.$RPObject$.prototype;
$JSCompiler_prototypeAlias$$.$onEvent$ = function($e$jscomp$59$$) {
  var $event$jscomp$49$$ = $marauroa$$.$rpeventFactory$.create($e$jscomp$59$$.c), $i$jscomp$72$$;
  for ($i$jscomp$72$$ in $e$jscomp$59$$.a) {
    $e$jscomp$59$$.a.hasOwnProperty($i$jscomp$72$$) && ($event$jscomp$49$$[$i$jscomp$72$$] = $e$jscomp$59$$.a[$i$jscomp$72$$]), $event$jscomp$49$$._rpclass = $e$jscomp$59$$.c;
  }
  for (var $slot$jscomp$5$$ in $e$jscomp$59$$.s) {
    $e$jscomp$59$$.s.hasOwnProperty($slot$jscomp$5$$) && ($event$jscomp$49$$[$slot$jscomp$5$$] = $e$jscomp$59$$.s[$slot$jscomp$5$$]);
  }
  $event$jscomp$49$$.$execute$(this);
};
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$80$$, $value$jscomp$120$$) {
  this[$key$jscomp$80$$] = $value$jscomp$120$$;
};
$JSCompiler_prototypeAlias$$.$setMapEntry$ = function($map$jscomp$9$$, $key$jscomp$81$$, $value$jscomp$121$$) {
  this[$map$jscomp$9$$][$key$jscomp$81$$] = $value$jscomp$121$$;
};
$JSCompiler_prototypeAlias$$.$unset$ = function($key$jscomp$82$$) {
  delete this[$key$jscomp$82$$];
};
$JSCompiler_prototypeAlias$$.$unsetMapEntry$ = function($map$jscomp$10$$, $key$jscomp$83$$) {
  delete this[$map$jscomp$10$$][$key$jscomp$83$$];
};
$JSCompiler_prototypeAlias$$.$destroy$ = function() {
};
$JSCompiler_prototypeAlias$$.$createSlot$ = function($name$jscomp$86_slot$jscomp$6$$) {
  $name$jscomp$86_slot$jscomp$6$$ = $marauroa$$.$rpslotFactory$.create($name$jscomp$86_slot$jscomp$6$$);
  $name$jscomp$86_slot$jscomp$6$$.$_parent$ = this;
  return $name$jscomp$86_slot$jscomp$6$$;
};
$JSCompiler_prototypeAlias$$.$init$ = function() {
};
$JSCompiler_prototypeAlias$$.$getWidth$ = function() {
  return this.width || 1;
};
$JSCompiler_prototypeAlias$$.$getHeight$ = function() {
  return this.height || 1;
};
var $module$build$ts$entity$Entity$default$$ = {};
Object.defineProperty($module$build$ts$entity$Entity$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Entity$default$$.$Entity$ = void 0;
$module$build$ts$entity$Entity$default$$.$Entity$ = function() {
  var $$jscomp$super$this$jscomp$17$$ = $module$build$ts$entity$RPObject$default$$.$RPObject$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$17$$.$minimapShow$ = !1;
  $$jscomp$super$this$jscomp$17$$.$minimapStyle$ = "rgb(200,255,200)";
  $$jscomp$super$this$jscomp$17$$.zIndex = 10000;
  return $$jscomp$super$this$jscomp$17$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Entity$default$$.$Entity$, $module$build$ts$entity$RPObject$default$$.$RPObject$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Entity$default$$.$Entity$.prototype;
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$84$$, $value$jscomp$122$$) {
  $module$build$ts$entity$RPObject$default$$.$RPObject$.prototype.set.call(this, $key$jscomp$84$$, $value$jscomp$122$$);
  "name" === $key$jscomp$84$$ ? ("undefined" === typeof this.title && (this.title = $value$jscomp$122$$), this._name = $value$jscomp$122$$) : this[$key$jscomp$84$$] = -1 < ["x", "y", "height", "width"].indexOf($key$jscomp$84$$) ? parseInt($value$jscomp$122$$, 10) : $value$jscomp$122$$;
};
function $JSCompiler_StaticMethods_isNextTo$$($JSCompiler_StaticMethods_isNextTo$self$$, $other$jscomp$11$$) {
  return $other$jscomp$11$$ && $JSCompiler_StaticMethods_isNextTo$self$$.x && $JSCompiler_StaticMethods_isNextTo$self$$.y && $other$jscomp$11$$.x && $other$jscomp$11$$.y && ($JSCompiler_StaticMethods_isNextTo$self$$.x + $JSCompiler_StaticMethods_isNextTo$self$$.width >= $other$jscomp$11$$.x && $JSCompiler_StaticMethods_isNextTo$self$$.x <= $other$jscomp$11$$.x || $other$jscomp$11$$.x + $other$jscomp$11$$.width >= $JSCompiler_StaticMethods_isNextTo$self$$.x && $other$jscomp$11$$.x <= $JSCompiler_StaticMethods_isNextTo$self$$.x) ? 
  $JSCompiler_StaticMethods_isNextTo$self$$.y + $JSCompiler_StaticMethods_isNextTo$self$$.height >= $other$jscomp$11$$.y && $JSCompiler_StaticMethods_isNextTo$self$$.y <= $other$jscomp$11$$.y || $other$jscomp$11$$.y + $other$jscomp$11$$.height >= $JSCompiler_StaticMethods_isNextTo$self$$.y && $other$jscomp$11$$.y <= $JSCompiler_StaticMethods_isNextTo$self$$.y : !1;
}
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !1;
};
function $JSCompiler_StaticMethods_actionAliasToAction$$($actionAlias$$) {
  var $actionAliases$$ = {look_closely:"use", read:"look"}, $actionCommand$$ = "look";
  "string" === typeof $actionAlias$$ && ($actionAliases$$.hasOwnProperty($actionAlias$$) ? $actionCommand$$ = $actionAliases$$[$actionAlias$$] : $actionCommand$$ = $actionAlias$$);
  return $actionCommand$$;
}
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$1$$) {
  if (this.menu) {
    var $JSCompiler_temp_const$jscomp$178_pos$jscomp$5$$ = this.menu.indexOf("|");
    $list$jscomp$1$$.push({title:this.menu.substring(0, $JSCompiler_temp_const$jscomp$178_pos$jscomp$5$$), type:this.menu.substring($JSCompiler_temp_const$jscomp$178_pos$jscomp$5$$ + 1).toLowerCase()});
  }
  if (this.action) {
    $JSCompiler_temp_const$jscomp$178_pos$jscomp$5$$ = $list$jscomp$1$$.push;
    var $JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$;
    ($JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$ = this.action) ? ($JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$ = $JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$.replace(/_/g, " ").trim(), $JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$ = $JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$.charAt(0).toUpperCase() + $JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$.slice(1)) : 
    $JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$ = "";
    $JSCompiler_temp_const$jscomp$178_pos$jscomp$5$$.call($list$jscomp$1$$, {title:$JSCompiler_inline_result$jscomp$180_s$jscomp$inline_564_temp$jscomp$inline_566$$, type:$JSCompiler_StaticMethods_actionAliasToAction$$(this.action)});
  } else {
    $list$jscomp$1$$.push({title:"Look", type:"look"});
  }
};
$JSCompiler_prototypeAlias$$.$onMiniMapDraw$ = function() {
};
$JSCompiler_prototypeAlias$$.$updatePosition$ = function() {
  this._y = this.y;
  this._x = this.x;
};
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$9$$) {
  this.$sprite$ && this.$drawSpriteAt$($ctx$jscomp$9$$, 32 * this.x, 32 * this.y);
};
$JSCompiler_prototypeAlias$$.$drawSpriteAt$ = function($ctx$jscomp$11$$, $x$jscomp$115$$, $y$jscomp$95$$) {
  var $image$jscomp$8$$ = $stendhal$$.data.$sprites$.get(this.$sprite$.filename);
  if ($image$jscomp$8$$.height) {
    var $offsetX$jscomp$2$$ = this.$sprite$.offsetX || 0, $offsetY$jscomp$2$$ = this.$sprite$.offsetY || 0, $width$jscomp$29$$ = this.$sprite$.width || $image$jscomp$8$$.width, $height$jscomp$26$$ = this.$sprite$.height || $image$jscomp$8$$.height;
    $x$jscomp$115$$ += Math.floor((32 * this.$getWidth$() - $width$jscomp$29$$) / 2);
    $y$jscomp$95$$ += Math.floor((32 * this.$getHeight$() - $height$jscomp$26$$) / 2);
    $ctx$jscomp$11$$.drawImage($image$jscomp$8$$, $offsetX$jscomp$2$$, $offsetY$jscomp$2$$, $width$jscomp$29$$, $height$jscomp$26$$, $x$jscomp$115$$, $y$jscomp$95$$, $width$jscomp$29$$, $height$jscomp$26$$);
  }
};
function $JSCompiler_StaticMethods_getIdPath$$($JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$) {
  var $object$jscomp$9_slot$jscomp$7$$ = $JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$;
  for ($JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$ = ""; $object$jscomp$9_slot$jscomp$7$$;) {
    $JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$ = $object$jscomp$9_slot$jscomp$7$$.id + "\t" + $JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$;
    $object$jscomp$9_slot$jscomp$7$$ = $object$jscomp$9_slot$jscomp$7$$.$_parent$;
    if (!$object$jscomp$9_slot$jscomp$7$$) {
      break;
    }
    $JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$ = $object$jscomp$9_slot$jscomp$7$$.$_name$ + "\t" + $JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$;
    $object$jscomp$9_slot$jscomp$7$$ = $object$jscomp$9_slot$jscomp$7$$.$_parent$;
  }
  return "[" + $JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$.substr(0, $JSCompiler_StaticMethods_getIdPath$self_res$jscomp$5$$.length - 1) + "]";
}
$JSCompiler_prototypeAlias$$.$say$ = function($text$jscomp$14$$, $rangeSquared$$) {
  $marauroa$$.$me$ && $JSCompiler_StaticMethods_isInHearingRange$$(this, $rangeSquared$$) && $module$build$ts$util$Chat$default$$.$Chat$.log("normal", $text$jscomp$14$$);
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  var $cursor$jscomp$1$$ = "unknown";
  this.cursor && ($cursor$jscomp$1$$ = this.cursor);
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/" + $cursor$jscomp$1$$.toLowerCase().replace("_", "") + ".png) 1 3, auto";
};
$JSCompiler_prototypeAlias$$.$getDefaultAction$ = function() {
  return {type:$JSCompiler_StaticMethods_actionAliasToAction$$(this.action), target:"#" + this.id, zone:$marauroa$$.$currentZoneName$};
};
$JSCompiler_prototypeAlias$$.$getResistance$ = function() {
  return this.resistance;
};
$JSCompiler_prototypeAlias$$.onclick = function() {
  $marauroa$$.$clientFramework$.$sendAction$(this.$getDefaultAction$());
};
$JSCompiler_prototypeAlias$$.$isDraggable$ = function() {
  return !1;
};
function $JSCompiler_StaticMethods_inView$$($JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$) {
  var $JSCompiler_object_inline_left_124$$ = $stendhal$$.$ui$.$gamewindow$.offsetX;
  var $JSCompiler_object_inline_right_125$$ = $JSCompiler_object_inline_left_124$$ + $stendhal$$.$ui$.$gamewindow$.width;
  var $JSCompiler_object_inline_top_126$$ = $stendhal$$.$ui$.$gamewindow$.offsetY;
  var $JSCompiler_object_inline_bottom_127$$ = $JSCompiler_object_inline_top_126$$ + $stendhal$$.$ui$.$gamewindow$.height;
  var $JSCompiler_object_inline_right_129_pixelX$jscomp$4$$ = $JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$.x * $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ + Math.floor($stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2), $pixelY$jscomp$4$$ = $JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$.y * $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ + Math.floor($stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2);
  var $JSCompiler_object_inline_left_128$$ = $JSCompiler_object_inline_right_129_pixelX$jscomp$4$$ - $JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$.drawWidth / 2;
  $JSCompiler_object_inline_right_129_pixelX$jscomp$4$$ += $JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$.drawWidth / 2;
  $JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$ = $pixelY$jscomp$4$$ - $JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$.drawHeight;
  return $JSCompiler_object_inline_right_129_pixelX$jscomp$4$$ > $JSCompiler_object_inline_left_124$$ && $JSCompiler_object_inline_left_128$$ < $JSCompiler_object_inline_right_125$$ && $pixelY$jscomp$4$$ > $JSCompiler_object_inline_top_126$$ && $JSCompiler_StaticMethods_inView$self_JSCompiler_object_inline_top_131$$ < $JSCompiler_object_inline_bottom_127$$;
}
;var $module$build$ts$entity$InvisibleEntity$default$$ = {};
Object.defineProperty($module$build$ts$entity$InvisibleEntity$default$$, "__esModule", {value:!0});
$module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$ = void 0;
$module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$ = function() {
  return $module$build$ts$entity$Entity$default$$.$Entity$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$.prototype.$isVisibleToAction$ = function() {
  return !1;
};
$module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/walk.png) 1 3, auto";
};
var $module$build$ts$entity$LoopedSoundSource$default$$ = {};
Object.defineProperty($module$build$ts$entity$LoopedSoundSource$default$$, "__esModule", {value:!0});
$module$build$ts$entity$LoopedSoundSource$default$$.$LoopedSoundSource$ = void 0;
$module$build$ts$entity$LoopedSoundSource$default$$.$LoopedSoundSource$ = function() {
  var $$jscomp$super$this$jscomp$18$$ = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$18$$.$v$ = $module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$.get();
  return $$jscomp$super$this$jscomp$18$$;
};
$$jscomp$inherits$$($module$build$ts$entity$LoopedSoundSource$default$$.$LoopedSoundSource$, $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$);
function $JSCompiler_StaticMethods_isLoaded$$($JSCompiler_StaticMethods_isLoaded$self$$) {
  return "undefined" !== typeof $JSCompiler_StaticMethods_isLoaded$self$$.$v$.$JSC$2020_sources$[$JSCompiler_StaticMethods_isLoaded$self$$.id];
}
;var $module$build$ts$ui$LoopedSoundSourceManager$default$$ = {};
Object.defineProperty($module$build$ts$ui$LoopedSoundSourceManager$default$$, "__esModule", {value:!0});
$module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$ = void 0;
var $sfxLoops$$module$build$ts$ui$LoopedSoundSourceManager$$ = {"clock-1":!0, "fire-1":!0, "sleep-1":!0};
$module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$ = function() {
  this.$v$ = $module$build$ts$ui$SoundManager$default$$.$SoundManager$.get();
  this.$JSC$2020_sources$ = {};
};
$module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$.get = function() {
  $module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$.$v$ || ($module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$.$v$ = new $module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$());
  return $module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$.$v$;
};
function $JSCompiler_StaticMethods_getZoneEntities$$() {
  var $ents$$ = [];
  if ($stendhal$$.$zone$.$entities$) {
    for (var $$jscomp$iter$53$$ = $$jscomp$makeIterator$$($stendhal$$.$zone$.$entities$), $$jscomp$key$ent_ent$$ = $$jscomp$iter$53$$.next(); !$$jscomp$key$ent_ent$$.done; $$jscomp$key$ent_ent$$ = $$jscomp$iter$53$$.next()) {
      $$jscomp$key$ent_ent$$ = $$jscomp$key$ent_ent$$.value, $$jscomp$key$ent_ent$$ instanceof $module$build$ts$entity$LoopedSoundSource$default$$.$LoopedSoundSource$ && $ents$$.push($$jscomp$key$ent_ent$$);
    }
  }
  return $ents$$;
}
function $JSCompiler_StaticMethods_onDistanceChanged$$($JSCompiler_StaticMethods_onDistanceChanged$self$$, $x$jscomp$118$$, $y$jscomp$98$$) {
  for (var $$jscomp$iter$55$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getZoneEntities$$()), $$jscomp$key$ent$jscomp$2_ent$jscomp$2$$ = $$jscomp$iter$55$$.next(); !$$jscomp$key$ent$jscomp$2_ent$jscomp$2$$.done; $$jscomp$key$ent$jscomp$2_ent$jscomp$2$$ = $$jscomp$iter$55$$.next()) {
    if ($$jscomp$key$ent$jscomp$2_ent$jscomp$2$$ = $$jscomp$key$ent$jscomp$2_ent$jscomp$2$$.value, $JSCompiler_StaticMethods_isLoaded$$($$jscomp$key$ent$jscomp$2_ent$jscomp$2$$)) {
      var $layername$jscomp$10$$ = $JSCompiler_StaticMethods_getLayerName$$($JSCompiler_StaticMethods_onDistanceChanged$self$$.$v$, $$jscomp$key$ent$jscomp$2_ent$jscomp$2$$.layer);
      $JSCompiler_StaticMethods_adjustForDistance$$($layername$jscomp$10$$, $JSCompiler_StaticMethods_onDistanceChanged$self$$.$JSC$2020_sources$[$$jscomp$key$ent$jscomp$2_ent$jscomp$2$$.id].$sound$, $$jscomp$key$ent$jscomp$2_ent$jscomp$2$$.radius, $$jscomp$key$ent$jscomp$2_ent$jscomp$2$$.x, $$jscomp$key$ent$jscomp$2_ent$jscomp$2$$.y, $x$jscomp$118$$, $y$jscomp$98$$);
    }
  }
}
;var $module$build$ts$ui$TouchHandler$default$$ = {};
Object.defineProperty($module$build$ts$ui$TouchHandler$default$$, "__esModule", {value:!0});
$module$build$ts$ui$TouchHandler$default$$.$TouchHandler$ = void 0;
$module$build$ts$ui$TouchHandler$default$$.$TouchHandler$ = function() {
  this.$touchEngaged$ = !1;
  this.$v$ = this.$A$ = 0;
  this.origin = void 0;
};
$module$build$ts$ui$TouchHandler$default$$.$TouchHandler$.get = function() {
  $module$build$ts$ui$TouchHandler$default$$.$TouchHandler$.$v$ || ($module$build$ts$ui$TouchHandler$default$$.$TouchHandler$.$v$ = new $module$build$ts$ui$TouchHandler$default$$.$TouchHandler$());
  return $module$build$ts$ui$TouchHandler$default$$.$TouchHandler$.$v$;
};
function $JSCompiler_StaticMethods_isTouchEvent$$($evt$jscomp$40$$) {
  return window.TouchEvent && $evt$jscomp$40$$ instanceof TouchEvent;
}
$module$build$ts$ui$TouchHandler$default$$.$TouchHandler$.prototype.$onTouchStart$ = function($x$jscomp$119$$, $y$jscomp$99$$) {
  this.$A$ = +new Date();
  this.$touchEngaged$ = !0;
  this.origin = {x:$x$jscomp$119$$, y:$y$jscomp$99$$};
};
$module$build$ts$ui$TouchHandler$default$$.$TouchHandler$.prototype.$onTouchEnd$ = function() {
  this.$v$ = +new Date();
  this.$touchEngaged$ = !1;
};
function $JSCompiler_StaticMethods_isLongTouch$$($evt$jscomp$41_pos$jscomp$6$$) {
  var $JSCompiler_StaticMethods_isLongTouch$self$$ = $stendhal$$.$ui$.$touch$;
  if ($evt$jscomp$41_pos$jscomp$6$$ && !$JSCompiler_StaticMethods_isTouchEvent$$($evt$jscomp$41_pos$jscomp$6$$)) {
    return !1;
  }
  var $durationMatch$$ = 300 < $JSCompiler_StaticMethods_isLongTouch$self$$.$v$ - $JSCompiler_StaticMethods_isLongTouch$self$$.$A$, $positionMatch$$ = !0;
  $evt$jscomp$41_pos$jscomp$6$$ && $JSCompiler_StaticMethods_isLongTouch$self$$.origin && ($evt$jscomp$41_pos$jscomp$6$$ = $JSCompiler_StaticMethods_extractPosition$$($evt$jscomp$41_pos$jscomp$6$$), $positionMatch$$ = 16 >= Math.abs($evt$jscomp$41_pos$jscomp$6$$.pageX - $JSCompiler_StaticMethods_isLongTouch$self$$.origin.x) && 16 >= Math.abs($evt$jscomp$41_pos$jscomp$6$$.pageY - $JSCompiler_StaticMethods_isLongTouch$self$$.origin.y));
  return $durationMatch$$ && $positionMatch$$;
}
function $JSCompiler_StaticMethods_unsetHeldItem$$() {
  $stendhal$$.$ui$.$touch$.$held$ = void 0;
}
function $JSCompiler_StaticMethods_unsetOrigin$$() {
  $stendhal$$.$ui$.$touch$.origin = void 0;
}
function $JSCompiler_StaticMethods_holdingItem$$() {
  var $JSCompiler_StaticMethods_holdingItem$self$$ = $stendhal$$.$ui$.$touch$;
  return void 0 != $JSCompiler_StaticMethods_holdingItem$self$$.$held$ && null != $JSCompiler_StaticMethods_holdingItem$self$$.$held$;
}
function $JSCompiler_StaticMethods_drawHeld$$($ctx$jscomp$12$$) {
  var $JSCompiler_StaticMethods_drawHeld$self$$ = $stendhal$$.$ui$.$touch$;
  $ctx$jscomp$12$$.globalAlpha = 0.5;
  $ctx$jscomp$12$$.drawImage($JSCompiler_StaticMethods_drawHeld$self$$.$held$.image, $JSCompiler_StaticMethods_drawHeld$self$$.$held$.offsetX + $stendhal$$.$ui$.$gamewindow$.offsetX, $JSCompiler_StaticMethods_drawHeld$self$$.$held$.offsetY + $stendhal$$.$ui$.$gamewindow$.offsetY);
  $ctx$jscomp$12$$.globalAlpha = 1.0;
}
;var $module$build$ts$ui$joystick$JoystickBase$default$$ = {};
Object.defineProperty($module$build$ts$ui$joystick$JoystickBase$default$$, "__esModule", {value:!0});
$module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$ = void 0;
$module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$ = function() {
  this.direction = this.$radius$ = 0;
};
$module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$.prototype.reset = function() {
};
$module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$.prototype.$onRemoved$ = function() {
};
function $JSCompiler_StaticMethods_getResource$$($res$jscomp$6$$) {
  return $stendhal$$.$paths$.$gui$ + "/joystick/" + $res$jscomp$6$$ + ".png";
}
function $JSCompiler_StaticMethods_getCenterX$$() {
  return $JSCompiler_StaticMethods_getInt$$($stendhal$$.$config$, "ui.joystick.center.x", 224);
}
function $JSCompiler_StaticMethods_getCenterY$$() {
  return $JSCompiler_StaticMethods_getInt$$($stendhal$$.$config$, "ui.joystick.center.y", 384);
}
function $JSCompiler_StaticMethods_checkActionEvent$$($e$jscomp$60$$) {
  return $e$jscomp$60$$ instanceof MouseEvent && 0 == $e$jscomp$60$$.button || $e$jscomp$60$$ instanceof TouchEvent ? !0 : !1;
}
function $JSCompiler_StaticMethods_onDirectionChange$$($JSCompiler_StaticMethods_onDirectionChange$self$$, $dir$jscomp$4$$) {
  $JSCompiler_StaticMethods_onDirectionChange$self$$.direction = $dir$jscomp$4$$;
  0 < $JSCompiler_StaticMethods_onDirectionChange$self$$.direction && 5 > $JSCompiler_StaticMethods_onDirectionChange$self$$.direction ? $marauroa$$.$clientFramework$.$sendAction$({type:"move", dir:"" + $JSCompiler_StaticMethods_onDirectionChange$self$$.direction}) : $marauroa$$.$clientFramework$.$sendAction$({type:"stop"});
}
;var $module$build$ts$ui$joystick$DirectionPad$default$$ = {};
Object.defineProperty($module$build$ts$ui$joystick$DirectionPad$default$$, "__esModule", {value:!0});
$module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$ = void 0;
$module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$ = function() {
  var $$jscomp$super$this$jscomp$19$$ = $module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$.call(this) || this;
  var $tmp$jscomp$9$$ = new Image();
  $tmp$jscomp$9$$.onload = function() {
    $$jscomp$super$this$jscomp$19$$.$radius$ = 1.25 * $tmp$jscomp$9$$.width;
    $JSCompiler_StaticMethods_JSC$2037_onInit$$($$jscomp$super$this$jscomp$19$$);
  };
  $tmp$jscomp$9$$.src = $JSCompiler_StaticMethods_getResource$$("dpad_button");
  $$jscomp$super$this$jscomp$19$$.$up$ = new Image();
  $$jscomp$super$this$jscomp$19$$.$down$ = new Image();
  $$jscomp$super$this$jscomp$19$$.left = new Image();
  $$jscomp$super$this$jscomp$19$$.right = new Image();
  return $$jscomp$super$this$jscomp$19$$;
};
$$jscomp$inherits$$($module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$, $module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$);
function $JSCompiler_StaticMethods_JSC$2037_onInit$$($JSCompiler_StaticMethods_JSC$2037_onInit$self$$) {
  var $center_x$$ = $JSCompiler_StaticMethods_getCenterX$$(), $center_y$$ = $JSCompiler_StaticMethods_getCenterY$$(), $container$$ = document.getElementById("joystick-container");
  $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$up$.onload = function() {
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$up$.style.left = $center_x$$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$up$.width / 2 + "px";
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$up$.style.top = $center_y$$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$radius$ + "px";
    $container$$.appendChild($JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$up$);
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$up$.onload = null;
  };
  $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$.onload = function() {
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$.style.left = $center_x$$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$.width / 2 + "px";
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$.style.top = $center_y$$ + $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$radius$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$.height + "px";
    $container$$.appendChild($JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$);
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$.onload = null;
  };
  $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.left.onload = function() {
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.left.style.left = $center_x$$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$radius$ + "px";
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.left.style.top = $center_y$$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.left.height / 2 + "px";
    $container$$.appendChild($JSCompiler_StaticMethods_JSC$2037_onInit$self$$.left);
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.left.onload = null;
  };
  $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right.onload = function() {
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right.style.left = $center_x$$ + $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$radius$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right.width + "px";
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right.style.top = $center_y$$ - $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right.height / 2 + "px";
    $container$$.appendChild($JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right);
    $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right.onload = null;
  };
  for (var $$jscomp$iter$56$$ = $$jscomp$makeIterator$$([$JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$up$, $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$down$, $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.left, $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.right]), $$jscomp$key$dimg_dimg$$ = $$jscomp$iter$56$$.next(); !$$jscomp$key$dimg_dimg$$.done; $$jscomp$key$dimg_dimg$$ = $$jscomp$iter$56$$.next()) {
    $$jscomp$key$dimg_dimg$$ = $$jscomp$key$dimg_dimg$$.value, $$jscomp$key$dimg_dimg$$.style.position = "absolute", $$jscomp$key$dimg_dimg$$.draggable = !1, $$jscomp$key$dimg_dimg$$.addEventListener("mousedown", function($e$jscomp$61$$) {
      $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$onMouseDown$($e$jscomp$61$$);
    }), $$jscomp$key$dimg_dimg$$.addEventListener("touchstart", function($e$jscomp$62$$) {
      $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$onMouseDown$($e$jscomp$62$$);
    }), $$jscomp$key$dimg_dimg$$.addEventListener("touchend", function($e$jscomp$63$$) {
      $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.$onMouseUp$($e$jscomp$63$$);
    });
  }
  $JSCompiler_StaticMethods_JSC$2037_onInit$self$$.reset();
}
$module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$.prototype.$onMouseDown$ = function($e$jscomp$64$$) {
  if ($JSCompiler_StaticMethods_checkActionEvent$$($e$jscomp$64$$)) {
    var $new_direction$$ = 0;
    switch($e$jscomp$64$$.target) {
      case this.$up$:
        this.$up$.src = $JSCompiler_StaticMethods_getResource$$("dpad_button_active");
        $new_direction$$ = 1;
        break;
      case this.right:
        this.right.src = $JSCompiler_StaticMethods_getResource$$("dpad_button_active");
        this.right.style.transform = "rotate(90deg)";
        $new_direction$$ = 2;
        break;
      case this.$down$:
        this.$down$.src = $JSCompiler_StaticMethods_getResource$$("dpad_button_active");
        this.$down$.style.transform = "rotate(180deg)";
        $new_direction$$ = 3;
        break;
      case this.left:
        this.left.src = $JSCompiler_StaticMethods_getResource$$("dpad_button_active"), this.left.style.transform = "rotate(-90deg)", $new_direction$$ = 4;
    }
    $new_direction$$ != this.direction && $JSCompiler_StaticMethods_onDirectionChange$$(this, $new_direction$$);
  }
};
$module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$.prototype.$onMouseUp$ = function($e$jscomp$65$$) {
  $JSCompiler_StaticMethods_checkActionEvent$$($e$jscomp$65$$) && this.reset();
};
$module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$.prototype.reset = function() {
  this.$up$.src = $JSCompiler_StaticMethods_getResource$$("dpad_button");
  this.$down$.src = $JSCompiler_StaticMethods_getResource$$("dpad_button");
  this.$down$.style.transform = "rotate(180deg)";
  this.left.src = $JSCompiler_StaticMethods_getResource$$("dpad_button");
  this.left.style.transform = "rotate(-90deg)";
  this.right.src = $JSCompiler_StaticMethods_getResource$$("dpad_button");
  this.right.style.transform = "rotate(90deg)";
  0 != this.direction && $JSCompiler_StaticMethods_onDirectionChange$$(this, 0);
};
$module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$.prototype.$onRemoved$ = function() {
  for (var $container$jscomp$1$$ = document.getElementById("joystick-container"), $$jscomp$iter$57$$ = $$jscomp$makeIterator$$([this.$up$, this.$down$, this.left, this.right]), $$jscomp$key$dimg$jscomp$1_dimg$jscomp$1$$ = $$jscomp$iter$57$$.next(); !$$jscomp$key$dimg$jscomp$1_dimg$jscomp$1$$.done; $$jscomp$key$dimg$jscomp$1_dimg$jscomp$1$$ = $$jscomp$iter$57$$.next()) {
    $$jscomp$key$dimg$jscomp$1_dimg$jscomp$1$$ = $$jscomp$key$dimg$jscomp$1_dimg$jscomp$1$$.value, $container$jscomp$1$$.contains($$jscomp$key$dimg$jscomp$1_dimg$jscomp$1$$) && $container$jscomp$1$$.removeChild($$jscomp$key$dimg$jscomp$1_dimg$jscomp$1$$);
  }
};
var $module$build$ts$ui$joystick$Joystick$default$$ = {};
Object.defineProperty($module$build$ts$ui$joystick$Joystick$default$$, "__esModule", {value:!0});
$module$build$ts$ui$joystick$Joystick$default$$.$Joystick$ = void 0;
$module$build$ts$ui$joystick$Joystick$default$$.$Joystick$ = function() {
  var $$jscomp$super$this$jscomp$20$$ = $module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$.call(this) || this;
  $$jscomp$super$this$jscomp$20$$.$B$ = !1;
  $$jscomp$super$this$jscomp$20$$.$A$ = new Image();
  $$jscomp$super$this$jscomp$20$$.$v$ = new Image();
  for (var $container$jscomp$2$$ = document.getElementById("joystick-container"), $$jscomp$iter$60$$ = $$jscomp$makeIterator$$([$$jscomp$super$this$jscomp$20$$.$A$, $$jscomp$super$this$jscomp$20$$.$v$]), $$jscomp$key$jimg_jimg$$ = $$jscomp$iter$60$$.next(); !$$jscomp$key$jimg_jimg$$.done; $$jscomp$key$jimg_jimg$$ = $$jscomp$iter$60$$.next()) {
    $$jscomp$key$jimg_jimg$$ = $$jscomp$key$jimg_jimg$$.value;
    $$jscomp$key$jimg_jimg$$.style.position = "absolute";
    $$jscomp$key$jimg_jimg$$.draggable = !1;
    $container$jscomp$2$$.appendChild($$jscomp$key$jimg_jimg$$);
    for (var $$jscomp$iter$58_$jscomp$iter$59$$ = $$jscomp$makeIterator$$(["mousedown", "touchstart"]), $$jscomp$key$etype$$ = $$jscomp$iter$58_$jscomp$iter$59$$.next(); !$$jscomp$key$etype$$.done; $$jscomp$key$etype$$ = $$jscomp$iter$58_$jscomp$iter$59$$.next()) {
      $$jscomp$key$jimg_jimg$$.addEventListener($$jscomp$key$etype$$.value, function($e$jscomp$66$$) {
        $$jscomp$super$this$jscomp$20$$.$onMouseDown$($e$jscomp$66$$);
      });
    }
    $$jscomp$key$jimg_jimg$$.addEventListener("touchend", function($e$jscomp$67$$) {
      $$jscomp$super$this$jscomp$20$$.$onMouseUp$($e$jscomp$67$$);
    });
    $$jscomp$iter$58_$jscomp$iter$59$$ = $$jscomp$makeIterator$$(["mousemove", "touchmove"]);
    for ($$jscomp$key$etype$$ = $$jscomp$iter$58_$jscomp$iter$59$$.next(); !$$jscomp$key$etype$$.done; $$jscomp$key$etype$$ = $$jscomp$iter$58_$jscomp$iter$59$$.next()) {
      $$jscomp$key$jimg_jimg$$.addEventListener($$jscomp$key$etype$$.value, function($e$jscomp$68$$) {
        ("mousemove" !== $e$jscomp$68$$.type || $$jscomp$super$this$jscomp$20$$.$B$) && $$jscomp$super$this$jscomp$20$$.$onMouseDown$($e$jscomp$68$$);
      });
    }
  }
  $$jscomp$super$this$jscomp$20$$.$A$.onload = function() {
    $$jscomp$super$this$jscomp$20$$.$A$.onload = null;
    $$jscomp$super$this$jscomp$20$$.$radius$ = Math.floor($$jscomp$super$this$jscomp$20$$.$A$.width / 2);
    $$jscomp$super$this$jscomp$20$$.$A$.style.left = $JSCompiler_StaticMethods_getCenterX$$() - $$jscomp$super$this$jscomp$20$$.$radius$ + "px";
    $$jscomp$super$this$jscomp$20$$.$A$.style.top = $JSCompiler_StaticMethods_getCenterY$$() - $$jscomp$super$this$jscomp$20$$.$radius$ + "px";
    $$jscomp$super$this$jscomp$20$$.reset();
  };
  $$jscomp$super$this$jscomp$20$$.$A$.src = $JSCompiler_StaticMethods_getResource$$("joystick_outer");
  return $$jscomp$super$this$jscomp$20$$;
};
$$jscomp$inherits$$($module$build$ts$ui$joystick$Joystick$default$$.$Joystick$, $module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$);
$module$build$ts$ui$joystick$Joystick$default$$.$Joystick$.prototype.$onMouseDown$ = function($JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$) {
  if ($JSCompiler_StaticMethods_checkActionEvent$$($JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$)) {
    this.$B$ = !0;
    this.$v$.src = $JSCompiler_StaticMethods_getResource$$("joystick_inner_active");
    $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$ = $JSCompiler_StaticMethods_extractPosition$$($JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$);
    $JSCompiler_StaticMethods_updateInner$$(this, $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$.pageX, $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$.pageY);
    $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$ = 0;
    var $iCenterY$jscomp$inline_590_rect$jscomp$inline_586$$ = this.$v$.getBoundingClientRect(), $oCenterX$jscomp$inline_587$$ = $JSCompiler_StaticMethods_getCenterX$$(), $oCenterY$jscomp$inline_588$$ = $JSCompiler_StaticMethods_getCenterY$$(), $iCenterX$jscomp$inline_589$$ = $iCenterY$jscomp$inline_590_rect$jscomp$inline_586$$.left + Math.floor(this.$v$.width / 2);
    $iCenterY$jscomp$inline_590_rect$jscomp$inline_586$$ = $iCenterY$jscomp$inline_590_rect$jscomp$inline_586$$.top + Math.floor(this.$v$.height / 2);
    $iCenterX$jscomp$inline_589$$ < $oCenterX$jscomp$inline_587$$ - 24 ? $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$ = 4 : $iCenterX$jscomp$inline_589$$ > $oCenterX$jscomp$inline_587$$ + 24 ? $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$ = 2 : $iCenterY$jscomp$inline_590_rect$jscomp$inline_586$$ < $oCenterY$jscomp$inline_588$$ - 24 ? $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$ = 
    1 : $iCenterY$jscomp$inline_590_rect$jscomp$inline_586$$ > $oCenterY$jscomp$inline_588$$ + 24 && ($JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$ = 3);
    $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$ != this.direction && $JSCompiler_StaticMethods_onDirectionChange$$(this, $JSCompiler_inline_result$jscomp$188_dir$jscomp$inline_585_e$jscomp$69_pos$jscomp$7$$);
  }
};
$module$build$ts$ui$joystick$Joystick$default$$.$Joystick$.prototype.$onMouseUp$ = function($e$jscomp$70$$) {
  $JSCompiler_StaticMethods_checkActionEvent$$($e$jscomp$70$$) && this.reset();
};
function $JSCompiler_StaticMethods_updateInner$$($JSCompiler_StaticMethods_updateInner$self$$, $x$jscomp$120$$, $y$jscomp$100$$) {
  var $bounds$$ = $JSCompiler_StaticMethods_updateInner$self$$.$A$.getBoundingClientRect(), $ydiff$$ = Math.abs($y$jscomp$100$$ - ($bounds$$.top + $JSCompiler_StaticMethods_updateInner$self$$.$radius$));
  Math.abs($x$jscomp$120$$ - ($bounds$$.left + $JSCompiler_StaticMethods_updateInner$self$$.$radius$)) <= $JSCompiler_StaticMethods_updateInner$self$$.$radius$ && ($JSCompiler_StaticMethods_updateInner$self$$.$v$.style.left = $x$jscomp$120$$ - Math.floor($JSCompiler_StaticMethods_updateInner$self$$.$v$.width / 2) + "px");
  $ydiff$$ <= $JSCompiler_StaticMethods_updateInner$self$$.$radius$ && ($JSCompiler_StaticMethods_updateInner$self$$.$v$.style.top = $y$jscomp$100$$ - Math.floor($JSCompiler_StaticMethods_updateInner$self$$.$v$.height / 2) + "px");
}
$module$build$ts$ui$joystick$Joystick$default$$.$Joystick$.prototype.reset = function() {
  var $$jscomp$this$jscomp$22$$ = this;
  this.$B$ = !1;
  this.$v$.onload = function() {
    $$jscomp$this$jscomp$22$$.$v$.onload = null;
    var $rect$jscomp$1$$ = $$jscomp$this$jscomp$22$$.$A$.getBoundingClientRect();
    $JSCompiler_StaticMethods_updateInner$$($$jscomp$this$jscomp$22$$, $rect$jscomp$1$$.left + $$jscomp$this$jscomp$22$$.$radius$, $rect$jscomp$1$$.top + $$jscomp$this$jscomp$22$$.$radius$);
  };
  this.$v$.src = $JSCompiler_StaticMethods_getResource$$("joystick_inner");
  0 != this.direction && $JSCompiler_StaticMethods_onDirectionChange$$(this, 0);
};
$module$build$ts$ui$joystick$Joystick$default$$.$Joystick$.prototype.$onRemoved$ = function() {
  for (var $container$jscomp$3$$ = document.getElementById("joystick-container"), $$jscomp$iter$61$$ = $$jscomp$makeIterator$$([this.$A$, this.$v$]), $$jscomp$key$jimg$jscomp$1_jimg$jscomp$1$$ = $$jscomp$iter$61$$.next(); !$$jscomp$key$jimg$jscomp$1_jimg$jscomp$1$$.done; $$jscomp$key$jimg$jscomp$1_jimg$jscomp$1$$ = $$jscomp$iter$61$$.next()) {
    $$jscomp$key$jimg$jscomp$1_jimg$jscomp$1$$ = $$jscomp$key$jimg$jscomp$1_jimg$jscomp$1$$.value, $container$jscomp$3$$.contains($$jscomp$key$jimg$jscomp$1_jimg$jscomp$1$$) && $container$jscomp$3$$.removeChild($$jscomp$key$jimg$jscomp$1_jimg$jscomp$1$$);
  }
};
$module$build$ts$ui$joystick$Joystick$default$$.$Joystick$.$v$ = 24;
var $module$build$ts$sprite$TextBubble$default$$ = {};
Object.defineProperty($module$build$ts$sprite$TextBubble$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$TextBubble$default$$.$TextBubble$ = void 0;
$module$build$ts$sprite$TextBubble$default$$.$TextBubble$ = function($text$jscomp$15$$) {
  this.height = this.width = this.y = this.x = -1;
  this.duration = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$;
  this.text = $text$jscomp$15$$;
  this.timeStamp = Date.now();
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.prototype;
$JSCompiler_prototypeAlias$$.$getX$ = function() {
  return this.x;
};
$JSCompiler_prototypeAlias$$.$getY$ = function() {
  return this.y;
};
$JSCompiler_prototypeAlias$$.$getWidth$ = function() {
  return this.width;
};
$JSCompiler_prototypeAlias$$.$getHeight$ = function() {
  return this.height;
};
function $JSCompiler_StaticMethods_clips$$($JSCompiler_StaticMethods_clips$self_b$jscomp$37$$, $x1$jscomp$5$$, $x2$jscomp$3$$, $y1$jscomp$5$$, $y2$jscomp$3$$) {
  var $x$jscomp$121$$ = $JSCompiler_StaticMethods_clips$self_b$jscomp$37$$.$getX$(), $y$jscomp$101$$ = $JSCompiler_StaticMethods_clips$self_b$jscomp$37$$.$getY$(), $r$jscomp$12$$ = $x$jscomp$121$$ + $JSCompiler_StaticMethods_clips$self_b$jscomp$37$$.$getWidth$();
  $JSCompiler_StaticMethods_clips$self_b$jscomp$37$$ = $y$jscomp$101$$ + $JSCompiler_StaticMethods_clips$self_b$jscomp$37$$.$getHeight$();
  return $x$jscomp$121$$ <= $x1$jscomp$5$$ && $x1$jscomp$5$$ <= $r$jscomp$12$$ && $x$jscomp$121$$ <= $x2$jscomp$3$$ && $x2$jscomp$3$$ <= $r$jscomp$12$$ && $y$jscomp$101$$ <= $y1$jscomp$5$$ && $y1$jscomp$5$$ <= $JSCompiler_StaticMethods_clips$self_b$jscomp$37$$ && $y$jscomp$101$$ <= $y2$jscomp$3$$ && $y2$jscomp$3$$ <= $JSCompiler_StaticMethods_clips$self_b$jscomp$37$$;
}
$JSCompiler_prototypeAlias$$.$onAdded$ = function($ctx$jscomp$13$$) {
  var $$jscomp$this$jscomp$23$$ = this;
  if ("undefined" === typeof this.$C$) {
    var $listener$jscomp$64$$ = function($JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$) {
      var $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$ = document.getElementById("gamewindow").getBoundingClientRect(), $pointX$jscomp$inline_595$$ = $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.clientX - $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$.x + $stendhal$$.$ui$.$gamewindow$.offsetX;
      $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$ = $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.clientY - $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$.y + $stendhal$$.$ui$.$gamewindow$.offsetY + $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$;
      if ($JSCompiler_StaticMethods_clips$$($$jscomp$this$jscomp$23$$, $pointX$jscomp$inline_595$$, $pointX$jscomp$inline_595$$, $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$, $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$)) {
        a: {
          $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.stopPropagation(), $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$ = $stendhal$$.$ui$.$gamewindow$;
          for (var $idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$ = $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.$v$.length - 1; 0 <= $idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$; $idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$--) {
            var $topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$ = $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.$v$[$idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$];
            if ($topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$ == $$jscomp$this$jscomp$23$$ || $JSCompiler_StaticMethods_clips$$($topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$, $pointX$jscomp$inline_595$$, $pointX$jscomp$inline_595$$, $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$, $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$)) {
              $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.$v$.splice($idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$, 1);
              $topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$.$onRemoved$();
              break a;
            }
          }
          for ($idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$ = $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.$textSprites$.length - 1; 0 <= $idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$; $idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$--) {
            if ($topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$ = $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.$textSprites$[$idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$], $topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$ == $$jscomp$this$jscomp$23$$ || $JSCompiler_StaticMethods_clips$$($topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$, $pointX$jscomp$inline_595$$, $pointX$jscomp$inline_595$$, $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$, 
            $pointY$jscomp$inline_596_screenRect$jscomp$inline_594$$)) {
              $JSCompiler_StaticMethods_removeTextBubble$self$jscomp$inline_1059_e$jscomp$71$$.$textSprites$.splice($idx$98$jscomp$inline_1062_idx$jscomp$inline_1060$$, 1);
              $topSprite$99$jscomp$inline_1063_topSprite$jscomp$inline_1061$$.$onRemoved$();
              break;
            }
          }
        }
      }
    };
    $ctx$jscomp$13$$.canvas.addEventListener("click", $listener$jscomp$64$$);
    this.$C$ = function() {
      $ctx$jscomp$13$$.canvas.removeEventListener("click", $listener$jscomp$64$$);
    };
  }
};
$JSCompiler_prototypeAlias$$.$onRemoved$ = function() {
  "undefined" !== typeof this.$C$ && this.$C$();
};
$JSCompiler_prototypeAlias$$.$expired$ = function() {
  return Date.now() >= this.timeStamp + this.duration;
};
$module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$ = 5000;
$module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$ = 15;
var $module$build$ts$util$BackgroundPainter$default$$ = {};
Object.defineProperty($module$build$ts$util$BackgroundPainter$default$$, "__esModule", {value:!0});
$module$build$ts$util$BackgroundPainter$default$$.$BackgroundPainter$ = void 0;
$module$build$ts$util$BackgroundPainter$default$$.$BackgroundPainter$ = function($img$jscomp$12_y$jscomp$103$$) {
  this.$D$ = [];
  this.$C$ = !1;
  this.$v$ = $img$jscomp$12_y$jscomp$103$$;
  this.$B$ = this.$v$.width / 3;
  this.$A$ = this.$v$.height / 3;
  for ($img$jscomp$12_y$jscomp$103$$ = 0; $img$jscomp$12_y$jscomp$103$$ < this.$v$.height; $img$jscomp$12_y$jscomp$103$$ += this.$A$) {
    for (var $row$jscomp$2$$ = [], $x$jscomp$123$$ = 0; $x$jscomp$123$$ < this.$v$.width; $x$jscomp$123$$ += this.$B$) {
      $row$jscomp$2$$.push({x:$x$jscomp$123$$, y:$img$jscomp$12_y$jscomp$103$$});
    }
    this.$D$.push($row$jscomp$2$$);
  }
};
var $module$build$ts$sprite$AchievementBanner$default$$ = {};
Object.defineProperty($module$build$ts$sprite$AchievementBanner$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$ = void 0;
$module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$ = function($cat$jscomp$1_gamewindow$jscomp$2$$, $bg_title$jscomp$19$$, $$jscomp$super$this$jscomp$21_desc$jscomp$1$$) {
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.call(this, $$jscomp$super$this$jscomp$21_desc$jscomp$1$$) || this;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.font = "normal 14px " + $stendhal$$.$config$.get("ui.font.tlog");
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$F$ = "normal 20px " + $stendhal$$.$config$.get("ui.font.tlog");
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$A$ = 0;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$D$ = 0;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$B$ = 32;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.title = $bg_title$jscomp$19$$;
  $bg_title$jscomp$19$$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$gui$ + "/banner_background.png");
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$G$ = new $module$build$ts$util$BackgroundPainter$default$$.$BackgroundPainter$($bg_title$jscomp$19$$);
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$v$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$achievements$ + "/" + $cat$jscomp$1_gamewindow$jscomp$2$$.toLowerCase() + ".png");
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.duration = 4 * $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$;
  $cat$jscomp$1_gamewindow$jscomp$2$$ = document.getElementById("gamewindow");
  var $ctx$jscomp$inline_599$$ = $cat$jscomp$1_gamewindow$jscomp$2$$.getContext("2d");
  $ctx$jscomp$inline_599$$.font = $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.font;
  var $m$jscomp$inline_601$$ = $ctx$jscomp$inline_599$$.measureText($$jscomp$super$this$jscomp$21_desc$jscomp$1$$.text);
  var $JSCompiler_object_inline_width_1189$$ = $m$jscomp$inline_601$$.width;
  $ctx$jscomp$inline_599$$.font = $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$F$;
  $m$jscomp$inline_601$$ = $ctx$jscomp$inline_599$$.measureText($$jscomp$super$this$jscomp$21_desc$jscomp$1$$.title);
  $JSCompiler_object_inline_width_1189$$ = Math.max($JSCompiler_object_inline_width_1189$$, $m$jscomp$inline_601$$.width);
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$A$ = $JSCompiler_object_inline_width_1189$$ + $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$B$;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$D$ = 96;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.width = $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$A$ + 2 * $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$B$;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.height = $bg_title$jscomp$19$$.height || $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.$D$;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.x = $cat$jscomp$1_gamewindow$jscomp$2$$.width / 2 - $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.width / 2;
  $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.y = $cat$jscomp$1_gamewindow$jscomp$2$$.height - $$jscomp$super$this$jscomp$21_desc$jscomp$1$$.height;
  return $$jscomp$super$this$jscomp$21_desc$jscomp$1$$;
};
$$jscomp$inherits$$($module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$, $module$build$ts$sprite$TextBubble$default$$.$TextBubble$);
$module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$.$A$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$;
$module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$.$v$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$;
$module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$.prototype.$draw$ = function($ctx$jscomp$15$$) {
  var $targetX$$ = $stendhal$$.$ui$.$gamewindow$.offsetX + this.x, $targetY$$ = $stendhal$$.$ui$.$gamewindow$.offsetY + this.y, $iconX$$ = $targetX$$ + this.width / 2 - this.$A$ / 2, $iconY$$ = $targetY$$ + this.height / 2 - 0.75 * this.$v$.height, $textX$$ = $iconX$$ + this.$v$.width + this.$B$, $textY$$ = $targetY$$ + this.height / 2 + 10, $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$ = this.$G$, $width$jscomp$inline_607$$ = this.width, $height$jscomp$inline_608$$ = this.height;
  if ($JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$B$ && $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$A$) {
    for (var $drawY$jscomp$inline_609$$ = 0, $ir$jscomp$inline_610$$ = 0; $drawY$jscomp$inline_609$$ < $height$jscomp$inline_608$$;) {
      for (var $drawX$jscomp$inline_611$$ = 0, $ic$jscomp$inline_612_slice$jscomp$inline_613$$ = 0; $drawX$jscomp$inline_611$$ < $width$jscomp$inline_607$$;) {
        $ic$jscomp$inline_612_slice$jscomp$inline_613$$ = $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$D$[$ir$jscomp$inline_610$$][$ic$jscomp$inline_612_slice$jscomp$inline_613$$], $ctx$jscomp$15$$.drawImage($JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$v$, $ic$jscomp$inline_612_slice$jscomp$inline_613$$.x, $ic$jscomp$inline_612_slice$jscomp$inline_613$$.y, $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$B$, $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$A$, 
        $targetX$$ + $drawX$jscomp$inline_611$$, $targetY$$ + $drawY$jscomp$inline_609$$, $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$B$, $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$A$), $drawX$jscomp$inline_611$$ += $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$B$, $drawX$jscomp$inline_611$$ = $drawX$jscomp$inline_611$$ <= $width$jscomp$inline_607$$ ? $drawX$jscomp$inline_611$$ : $drawX$jscomp$inline_611$$ + ($width$jscomp$inline_607$$ - $drawX$jscomp$inline_611$$), 
        $ic$jscomp$inline_612_slice$jscomp$inline_613$$ = $drawX$jscomp$inline_611$$ + $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$B$ >= $width$jscomp$inline_607$$ ? 2 : 1;
      }
      $drawY$jscomp$inline_609$$ += $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$A$;
      $drawY$jscomp$inline_609$$ = $drawY$jscomp$inline_609$$ <= $height$jscomp$inline_608$$ ? $drawY$jscomp$inline_609$$ : $drawY$jscomp$inline_609$$ + ($height$jscomp$inline_608$$ - $drawY$jscomp$inline_609$$);
      $ir$jscomp$inline_610$$ = $drawY$jscomp$inline_609$$ + $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$A$ >= $height$jscomp$inline_608$$ ? 2 : 1;
    }
  } else {
    $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$C$ || (console.warn("cannot draw background while tile width or height is 0"), $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$C$ = !0), $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$B$ = $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$v$.width / 3, $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$A$ = $JSCompiler_StaticMethods_paint$self$jscomp$inline_603$$.$v$.height / 3;
  }
  this.$v$.height && this.$v$.complete && $ctx$jscomp$15$$.drawImage(this.$v$, 0, 0, this.$v$.width, this.$v$.height, $iconX$$, $iconY$$, this.$v$.width, this.$v$.height);
  $ctx$jscomp$15$$.fillStyle = "#000000";
  $ctx$jscomp$15$$.font = this.$F$;
  $ctx$jscomp$15$$.fillText(this.title, $textX$$, $textY$$ - 25);
  $ctx$jscomp$15$$.font = this.font;
  $ctx$jscomp$15$$.fillText(this.text, $textX$$, $textY$$);
  return this.$expired$();
};
$module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$.prototype.$getX$ = function() {
  return $stendhal$$.$ui$.$gamewindow$.offsetX + this.x;
};
$module$build$ts$sprite$AchievementBanner$default$$.$AchievementBanner$.prototype.$getY$ = function() {
  return $stendhal$$.$ui$.$gamewindow$.offsetY + this.y;
};
var $module$build$ts$util$Color$default$$ = {};
Object.defineProperty($module$build$ts$util$Color$default$$, "__esModule", {value:!0});
$module$build$ts$util$Color$default$$.$Color$ = void 0;
$module$build$ts$util$Color$default$$.$Color$ = function() {
};
$module$build$ts$util$Color$default$$.$Color$.$F$ = function($ratio$$) {
  return "rgb(" + Math.floor(255 * Math.min(2 * (1 - $ratio$$), 1)) + ", " + Math.floor(255 * Math.min(2 * $ratio$$, 1)) + ", 0)";
};
$module$build$ts$util$Color$default$$.$Color$.$G$ = "#00ffff";
$module$build$ts$util$Color$default$$.$Color$.$A$ = "#000000";
$module$build$ts$util$Color$default$$.$Color$.$H$ = "rgb(0, 0, 255)";
$module$build$ts$util$Color$default$$.$Color$.$S$ = $module$build$ts$util$Color$default$$.$Color$.$G$;
$module$build$ts$util$Color$default$$.$Color$.$T$ = "#a9a9a9";
$module$build$ts$util$Color$default$$.$Color$.$B$ = "#808080";
$module$build$ts$util$Color$default$$.$Color$.$W$ = "#008000";
$module$build$ts$util$Color$default$$.$Color$.$Y$ = "#c0c0c0";
$module$build$ts$util$Color$default$$.$Color$.$Z$ = "#ff00ff";
$module$build$ts$util$Color$default$$.$Color$.$aa$ = "#ffa500";
$module$build$ts$util$Color$default$$.$Color$.$ba$ = "#ffc0cb";
$module$build$ts$util$Color$default$$.$Color$.$v$ = "#ff0000";
$module$build$ts$util$Color$default$$.$Color$.$M$ = "rgb(238, 130, 238)";
$module$build$ts$util$Color$default$$.$Color$.$D$ = "#ffffff";
$module$build$ts$util$Color$default$$.$Color$.$N$ = "#ffff00";
$module$build$ts$util$Color$default$$.$Color$.$J$ = $module$build$ts$util$Color$default$$.$Color$.$N$;
$module$build$ts$util$Color$default$$.$Color$.$K$ = "rgb(255, 150, 0)";
$module$build$ts$util$Color$default$$.$Color$.$V$ = $module$build$ts$util$Color$default$$.$Color$.$B$;
$module$build$ts$util$Color$default$$.$Color$.$X$ = "rgb(99, 61, 139)";
$module$build$ts$util$Color$default$$.$Color$.$NPC$ = "rgb(0, 150, 0)";
$module$build$ts$util$Color$default$$.$Color$.$C$ = $module$build$ts$util$Color$default$$.$Color$.$D$;
$module$build$ts$util$Color$default$$.$Color$.$L$ = $module$build$ts$util$Color$default$$.$Color$.$H$;
$module$build$ts$util$Color$default$$.$Color$.$O$ = "#cccccc";
$module$build$ts$util$Color$default$$.$Color$.$P$ = $module$build$ts$util$Color$default$$.$Color$.$v$;
$module$build$ts$util$Color$default$$.$Color$.$R$ = $module$build$ts$util$Color$default$$.$Color$.$v$;
$module$build$ts$util$Color$default$$.$Color$.$U$ = "rgb(212, 158, 72)";
$module$build$ts$util$Color$default$$.$Color$.$ea$ = "rgb(202, 230, 202)";
$module$build$ts$util$Color$default$$.$Color$.$fa$ = "rgb(209, 144, 224)";
$module$build$ts$util$Color$default$$.$Color$.$$$ = "rgb(200, 255, 200)";
$module$build$ts$util$Color$default$$.$Color$.$ca$ = $module$build$ts$util$Color$default$$.$Color$.$D$;
$module$build$ts$util$Color$default$$.$Color$.$da$ = $module$build$ts$util$Color$default$$.$Color$.$A$;
$module$build$ts$util$Color$default$$.$Color$.$I$ = $module$build$ts$util$Color$default$$.$Color$.$M$;
var $module$build$ts$util$NotificationType$default$$ = {};
Object.defineProperty($module$build$ts$util$NotificationType$default$$, "__esModule", {value:!0});
$module$build$ts$util$NotificationType$default$$.$NotificationType$ = void 0;
$module$build$ts$util$NotificationType$default$$.$NotificationType$ = {client:$module$build$ts$util$Color$default$$.$Color$.$B$, detailed:$module$build$ts$util$Color$default$$.$Color$.$A$, emote:"rgb(99, 61, 139)", error:$module$build$ts$util$Color$default$$.$Color$.$v$, group:"rgb(00, 00, 160)", heal:"#008000", information:"#ffa500", negative:$module$build$ts$util$Color$default$$.$Color$.$v$, normal:$module$build$ts$util$Color$default$$.$Color$.$A$, poison:$module$build$ts$util$Color$default$$.$Color$.$v$, 
positive:"#008000", privmsg:"#a9a9a9", response:"#006400", scene_setting:"#572002", server:"#a9a9a9", significant_negative:"#ffc0cb", significant_positive:"rgb(65, 105, 225)", support:"#ff7200", tutorial:"rgb(172, 0, 172)", warning:"#a00000"};
var $module$build$ts$util$Speech$default$$ = {};
Object.defineProperty($module$build$ts$util$Speech$default$$, "__esModule", {value:!0});
$module$build$ts$util$Speech$default$$.$Speech$ = void 0;
$module$build$ts$util$Speech$default$$.$Speech$ = function() {
};
$module$build$ts$util$Speech$default$$.$Speech$.$A$ = function($ctx$jscomp$17$$, $x$jscomp$125$$, $y$jscomp$105$$, $width$jscomp$31$$, $height$jscomp$28$$) {
  var $tail$$ = void 0 === $tail$$ ? !1 : $tail$$;
  $ctx$jscomp$17$$.strokeRect($x$jscomp$125$$, $y$jscomp$105$$ - 15, $width$jscomp$31$$, $height$jscomp$28$$);
  $ctx$jscomp$17$$.fillRect($x$jscomp$125$$, $y$jscomp$105$$ - 15, $width$jscomp$31$$, $height$jscomp$28$$);
  $ctx$jscomp$17$$.beginPath();
  $ctx$jscomp$17$$.moveTo($x$jscomp$125$$, $y$jscomp$105$$);
  $tail$$ && ($ctx$jscomp$17$$.lineTo($x$jscomp$125$$ - 5, $y$jscomp$105$$ + 8), $ctx$jscomp$17$$.lineTo($x$jscomp$125$$ + 1, $y$jscomp$105$$ + 5));
  $ctx$jscomp$17$$.stroke();
  $ctx$jscomp$17$$.closePath();
  $ctx$jscomp$17$$.fill();
};
$module$build$ts$util$Speech$default$$.$Speech$.$v$ = function($ctx$jscomp$18$$, $x$jscomp$126$$, $y$jscomp$106$$, $width$jscomp$32$$, $height$jscomp$29$$) {
  $ctx$jscomp$18$$.beginPath();
  $ctx$jscomp$18$$.moveTo($x$jscomp$126$$ + 3, $y$jscomp$106$$);
  $ctx$jscomp$18$$.lineTo($x$jscomp$126$$ + $width$jscomp$32$$ - 3, $y$jscomp$106$$);
  $ctx$jscomp$18$$.quadraticCurveTo($x$jscomp$126$$ + $width$jscomp$32$$, $y$jscomp$106$$, $x$jscomp$126$$ + $width$jscomp$32$$, $y$jscomp$106$$ + 3);
  $ctx$jscomp$18$$.lineTo($x$jscomp$126$$ + $width$jscomp$32$$, $y$jscomp$106$$ + $height$jscomp$29$$ - 3);
  $ctx$jscomp$18$$.quadraticCurveTo($x$jscomp$126$$ + $width$jscomp$32$$, $y$jscomp$106$$ + $height$jscomp$29$$, $x$jscomp$126$$ + $width$jscomp$32$$ - 3, $y$jscomp$106$$ + $height$jscomp$29$$);
  $ctx$jscomp$18$$.lineTo($x$jscomp$126$$ + 3, $y$jscomp$106$$ + $height$jscomp$29$$);
  $ctx$jscomp$18$$.quadraticCurveTo($x$jscomp$126$$, $y$jscomp$106$$ + $height$jscomp$29$$, $x$jscomp$126$$, $y$jscomp$106$$ + $height$jscomp$29$$ - 3);
  $ctx$jscomp$18$$.lineTo($x$jscomp$126$$, $y$jscomp$106$$ + 8);
  $ctx$jscomp$18$$.lineTo($x$jscomp$126$$ - 8, $y$jscomp$106$$ + 11);
  $ctx$jscomp$18$$.lineTo($x$jscomp$126$$, $y$jscomp$106$$ + 3);
  $ctx$jscomp$18$$.lineTo($x$jscomp$126$$, $y$jscomp$106$$ + 3);
  $ctx$jscomp$18$$.quadraticCurveTo($x$jscomp$126$$, $y$jscomp$106$$, $x$jscomp$126$$ + 3, $y$jscomp$106$$);
  $ctx$jscomp$18$$.stroke();
  $ctx$jscomp$18$$.closePath();
  $ctx$jscomp$18$$.fill();
};
var $module$build$ts$sprite$NotificationBubble$default$$ = {};
Object.defineProperty($module$build$ts$sprite$NotificationBubble$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$NotificationBubble$default$$.$NotificationBubble$ = void 0;
$module$build$ts$sprite$NotificationBubble$default$$.$NotificationBubble$ = function($mtype_nextline$$, $$jscomp$iter$62_text$jscomp$16$$, $profile$jscomp$3$$) {
  var $$jscomp$super$this$jscomp$22$$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.call(this, $$jscomp$iter$62_text$jscomp$16$$) || this;
  $$jscomp$super$this$jscomp$22$$.$A$ = 4;
  $$jscomp$super$this$jscomp$22$$.$B$ = $mtype_nextline$$;
  $$jscomp$super$this$jscomp$22$$.$D$ = $profile$jscomp$3$$;
  $$jscomp$super$this$jscomp$22$$.duration = Math.max($module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$, $$jscomp$super$this$jscomp$22$$.text.length * $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$ / 50);
  $$jscomp$super$this$jscomp$22$$.$v$ = [];
  $mtype_nextline$$ = "";
  $$jscomp$iter$62_text$jscomp$16$$ = $$jscomp$makeIterator$$($$jscomp$iter$62_text$jscomp$16$$.split("\t").join(" ").split(" "));
  for (var $$jscomp$key$w_w$jscomp$14$$ = $$jscomp$iter$62_text$jscomp$16$$.next(); !$$jscomp$key$w_w$jscomp$14$$.done; $$jscomp$key$w_w$jscomp$14$$ = $$jscomp$iter$62_text$jscomp$16$$.next()) {
    $$jscomp$key$w_w$jscomp$14$$ = $$jscomp$key$w_w$jscomp$14$$.value, $mtype_nextline$$ && ($mtype_nextline$$ += " "), $mtype_nextline$$ += $$jscomp$key$w_w$jscomp$14$$, 60 < $mtype_nextline$$.length ? ($$jscomp$super$this$jscomp$22$$.$v$.push($mtype_nextline$$.substr(0, 60) + "-"), $mtype_nextline$$ = "-" + $mtype_nextline$$.substr(60)) : 30 <= $mtype_nextline$$.length && ($$jscomp$super$this$jscomp$22$$.$v$.push($mtype_nextline$$), $mtype_nextline$$ = "");
  }
  $mtype_nextline$$ && $$jscomp$super$this$jscomp$22$$.$v$.push($mtype_nextline$$);
  $profile$jscomp$3$$ && ($$jscomp$super$this$jscomp$22$$.profile = new Image(), $JSCompiler_StaticMethods_loadProfileSprite$$($$jscomp$super$this$jscomp$22$$));
  return $$jscomp$super$this$jscomp$22$$;
};
$$jscomp$inherits$$($module$build$ts$sprite$NotificationBubble$default$$.$NotificationBubble$, $module$build$ts$sprite$TextBubble$default$$.$TextBubble$);
$module$build$ts$sprite$NotificationBubble$default$$.$NotificationBubble$.$A$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$;
$module$build$ts$sprite$NotificationBubble$default$$.$NotificationBubble$.$v$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$;
$module$build$ts$sprite$NotificationBubble$default$$.$NotificationBubble$.prototype.$draw$ = function($ctx$jscomp$19$$) {
  for (var $screenBottom_sy$jscomp$5$$ = $stendhal$$.$ui$.$gamewindow$.offsetY + $ctx$jscomp$19$$.canvas.height, $li$97_screenCenterX$$ = $stendhal$$.$ui$.$gamewindow$.offsetX + $ctx$jscomp$19$$.canvas.width / 2, $lcount$$ = this.$v$.length, $longest_meas$$ = "", $li$jscomp$2$$ = 0; $li$jscomp$2$$ < $lcount$$; $li$jscomp$2$$++) {
    this.$v$[$li$jscomp$2$$].length > $longest_meas$$.length && ($longest_meas$$ = this.$v$[$li$jscomp$2$$]);
  }
  $longest_meas$$ = $ctx$jscomp$19$$.measureText($longest_meas$$);
  if (0 > this.width || 0 > this.height) {
    this.width = $longest_meas$$.width + 2 * this.$A$, this.height = 20 * $lcount$$;
  }
  this.x = $li$97_screenCenterX$$ - this.width / 2;
  this.y = $screenBottom_sy$jscomp$5$$ - this.height + $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$ - 1;
  $ctx$jscomp$19$$.lineWidth = 2;
  $ctx$jscomp$19$$.font = "14px sans-serif";
  $ctx$jscomp$19$$.fillStyle = "#ffffff";
  $ctx$jscomp$19$$.strokeStyle = "#000000";
  this.profile ? (this.profile.complete && this.profile.height || $JSCompiler_StaticMethods_loadProfileSprite$$(this), this.profile.complete && this.profile.height && $ctx$jscomp$19$$.drawImage(this.profile, this.x - 48, this.y - 16), $module$build$ts$util$Speech$default$$.$Speech$.$v$($ctx$jscomp$19$$, this.x, this.y - 15, this.width, this.height)) : $module$build$ts$util$Speech$default$$.$Speech$.$A$($ctx$jscomp$19$$, this.x, this.y, this.width, this.height);
  $ctx$jscomp$19$$.fillStyle = $module$build$ts$util$NotificationType$default$$.$NotificationType$[this.$B$] || "#000000";
  $screenBottom_sy$jscomp$5$$ = this.y;
  for ($li$97_screenCenterX$$ = 0; $li$97_screenCenterX$$ < $lcount$$; $li$97_screenCenterX$$++) {
    $ctx$jscomp$19$$.fillText(this.$v$[$li$97_screenCenterX$$], this.x + this.$A$, $screenBottom_sy$jscomp$5$$), $screenBottom_sy$jscomp$5$$ += 20;
  }
  return this.$expired$();
};
function $JSCompiler_StaticMethods_loadProfileSprite$$($JSCompiler_StaticMethods_loadProfileSprite$self$$) {
  var $img$jscomp$13$$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/npc/" + $JSCompiler_StaticMethods_loadProfileSprite$self$$.$D$ + ".png");
  $img$jscomp$13$$.complete && $img$jscomp$13$$.height && ($JSCompiler_StaticMethods_loadProfileSprite$self$$.profile = $JSCompiler_StaticMethods_getAreaOf$$($stendhal$$.data.$sprites$, $img$jscomp$13$$, 48, 48, 48, 128));
}
;var $module$build$ts$sprite$SpeechBubble$default$$ = {};
Object.defineProperty($module$build$ts$sprite$SpeechBubble$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$ = void 0;
$module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$ = function($$jscomp$super$this$jscomp$23_text$jscomp$17$$, $entity$jscomp$12_x$jscomp$127$$) {
  $$jscomp$super$this$jscomp$23_text$jscomp$17$$ = $$jscomp$super$this$jscomp$23_text$jscomp$17$$.replace(/\\\\/g, "\\");
  $$jscomp$super$this$jscomp$23_text$jscomp$17$$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.call(this, 30 < $$jscomp$super$this$jscomp$23_text$jscomp$17$$.length ? $$jscomp$super$this$jscomp$23_text$jscomp$17$$.substring(0, 30) + "..." : $$jscomp$super$this$jscomp$23_text$jscomp$17$$) || this;
  $$jscomp$super$this$jscomp$23_text$jscomp$17$$.$v$ = $entity$jscomp$12_x$jscomp$127$$;
  $$jscomp$super$this$jscomp$23_text$jscomp$17$$.offsetY = 0;
  $entity$jscomp$12_x$jscomp$127$$ = $$jscomp$super$this$jscomp$23_text$jscomp$17$$.$getX$();
  for (var $y$jscomp$107$$ = $$jscomp$super$this$jscomp$23_text$jscomp$17$$.$getY$(), $$jscomp$iter$63$$ = $$jscomp$makeIterator$$($stendhal$$.$ui$.$gamewindow$.$textSprites$), $$jscomp$key$bubble_bubble$$ = $$jscomp$iter$63$$.next(); !$$jscomp$key$bubble_bubble$$.done; $$jscomp$key$bubble_bubble$$ = $$jscomp$iter$63$$.next()) {
    $$jscomp$key$bubble_bubble$$ = $$jscomp$key$bubble_bubble$$.value, $entity$jscomp$12_x$jscomp$127$$ == $$jscomp$key$bubble_bubble$$.$getX$() && $y$jscomp$107$$ + $$jscomp$super$this$jscomp$23_text$jscomp$17$$.offsetY == $$jscomp$key$bubble_bubble$$.$getY$() && ($$jscomp$super$this$jscomp$23_text$jscomp$17$$.offsetY += $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2);
  }
  $$jscomp$super$this$jscomp$23_text$jscomp$17$$.duration = Math.max($module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$, $$jscomp$super$this$jscomp$23_text$jscomp$17$$.text.length * $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$ / 50);
  return $$jscomp$super$this$jscomp$23_text$jscomp$17$$;
};
$$jscomp$inherits$$($module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$, $module$build$ts$sprite$TextBubble$default$$.$TextBubble$);
$module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$.$A$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$;
$module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$.$v$ = $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$v$;
$module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$.prototype.$draw$ = function($ctx$jscomp$20$$) {
  $ctx$jscomp$20$$.lineWidth = 2;
  $ctx$jscomp$20$$.font = "14px Arial";
  $ctx$jscomp$20$$.fillStyle = "#ffffff";
  $ctx$jscomp$20$$.strokeStyle = "#000000";
  if (0 > this.width || 0 > this.height) {
    this.width = $ctx$jscomp$20$$.measureText(this.text).width + 8, this.height = 20;
  }
  var $x$jscomp$128$$ = this.$getX$(), $y$jscomp$108$$ = this.$getY$();
  $module$build$ts$util$Speech$default$$.$Speech$.$v$($ctx$jscomp$20$$, $x$jscomp$128$$, $y$jscomp$108$$, this.width, this.height);
  $ctx$jscomp$20$$.fillStyle = "#000000";
  $ctx$jscomp$20$$.fillText(this.text, $x$jscomp$128$$ + 4, $y$jscomp$108$$ + $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$);
  return this.$expired$();
};
$module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$.prototype.$getX$ = function() {
  var $x$jscomp$129$$ = 32 * this.$v$._x + 32 * this.$v$.width;
  if ($JSCompiler_StaticMethods_inView$$(this.$v$)) {
    var $overdraw$$ = $x$jscomp$129$$ + this.width - ($stendhal$$.$ui$.$gamewindow$.offsetX + $stendhal$$.$ui$.$gamewindow$.width) + 1;
    $x$jscomp$129$$ = 0 < $overdraw$$ ? $x$jscomp$129$$ - $overdraw$$ : $x$jscomp$129$$;
  }
  return $x$jscomp$129$$;
};
$module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$.prototype.$getY$ = function() {
  var $y$jscomp$109$$ = 32 * this.$v$._y - 16 - 32 * (this.$v$.height - 1) + this.offsetY - $module$build$ts$sprite$TextBubble$default$$.$TextBubble$.$A$;
  $JSCompiler_StaticMethods_inView$$(this.$v$) && ($y$jscomp$109$$ = $y$jscomp$109$$ < $stendhal$$.$ui$.$gamewindow$.offsetY + 1 ? $stendhal$$.$ui$.$gamewindow$.offsetY + 1 : $y$jscomp$109$$);
  return $y$jscomp$109$$;
};
var $module$build$ts$ui$ViewPort$default$$ = {};
Object.defineProperty($module$build$ts$ui$ViewPort$default$$, "__esModule", {value:!0});
$module$build$ts$ui$ViewPort$default$$.$ViewPort$ = void 0;
$module$build$ts$ui$ViewPort$default$$.$ViewPort$ = function() {
  this.offsetY = this.offsetX = 0;
  this.timeStamp = Date.now();
  this.$targetTileHeight$ = this.$targetTileWidth$ = 32;
  this.$textSprites$ = [];
  this.$v$ = [];
  this.$emojiSprites$ = [];
  this.$A$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$C$();
  this.$joystick$ = new $module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$();
  this.$onMouseDown$ = function() {
    var $entity$jscomp$13$$, $startX$$, $startY$$, $mHandle$$ = {$_onMouseDown$:function($e$jscomp$72$$) {
      var $pos$jscomp$8$$ = $JSCompiler_StaticMethods_extractPosition$$($e$jscomp$72$$);
      if ($JSCompiler_StaticMethods_isTouchEvent$$($e$jscomp$72$$)) {
        if ($JSCompiler_StaticMethods_holdingItem$$()) {
          return;
        }
        $stendhal$$.$ui$.$touch$.$onTouchStart$($pos$jscomp$8$$.pageX, $pos$jscomp$8$$.pageY);
      }
      $stendhal$$.$ui$.$globalpopup$ && $stendhal$$.$ui$.$globalpopup$.close();
      $startX$$ = $pos$jscomp$8$$.$canvasRelativeX$;
      $startY$$ = $pos$jscomp$8$$.$canvasRelativeY$;
      var $x$jscomp$130$$ = $pos$jscomp$8$$.$canvasRelativeX$ + $stendhal$$.$ui$.$gamewindow$.offsetX, $y$jscomp$110$$ = $pos$jscomp$8$$.$canvasRelativeY$ + $stendhal$$.$ui$.$gamewindow$.offsetY;
      a: {
        var $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$ = $y$jscomp$110$$ + 15;
        for (var $$jscomp$inline_620_JSCompiler_StaticMethods_textBubbleAt$self$jscomp$inline_617$$ = $stendhal$$.$ui$.$gamewindow$, $$jscomp$inline_618$$ = $$jscomp$makeIterator$$($$jscomp$inline_620_JSCompiler_StaticMethods_textBubbleAt$self$jscomp$inline_617$$.$v$), $$jscomp$inline_619$$ = $$jscomp$inline_618$$.next(); !$$jscomp$inline_619$$.done; $$jscomp$inline_619$$ = $$jscomp$inline_618$$.next()) {
          if ($JSCompiler_StaticMethods_clips$$($$jscomp$inline_619$$.value, $x$jscomp$130$$, $x$jscomp$130$$, $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$, $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$)) {
            $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$ = !0;
            break a;
          }
        }
        $$jscomp$inline_620_JSCompiler_StaticMethods_textBubbleAt$self$jscomp$inline_617$$ = $$jscomp$makeIterator$$($$jscomp$inline_620_JSCompiler_StaticMethods_textBubbleAt$self$jscomp$inline_617$$.$textSprites$);
        for ($$jscomp$inline_619$$ = $$jscomp$inline_620_JSCompiler_StaticMethods_textBubbleAt$self$jscomp$inline_617$$.next(); !$$jscomp$inline_619$$.done; $$jscomp$inline_619$$ = $$jscomp$inline_620_JSCompiler_StaticMethods_textBubbleAt$self$jscomp$inline_617$$.next()) {
          if ($JSCompiler_StaticMethods_clips$$($$jscomp$inline_619$$.value, $x$jscomp$130$$, $x$jscomp$130$$, $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$, $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$)) {
            $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$ = !0;
            break a;
          }
        }
        $JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$ = !1;
      }
      if (!$JSCompiler_inline_result$jscomp$190_y$jscomp$inline_616$$) {
        if ($entity$jscomp$13$$ = $JSCompiler_StaticMethods_entityAt$$($x$jscomp$130$$, $y$jscomp$110$$), $stendhal$$.$ui$.$timestampMouseDown$ = +new Date(), "dblclick" !== $e$jscomp$72$$.type && $e$jscomp$72$$.target) {
          $e$jscomp$72$$.target.addEventListener("mousemove", $mHandle$$.$onDrag$), $e$jscomp$72$$.target.addEventListener("mouseup", $mHandle$$.$onMouseUp$), $e$jscomp$72$$.target.addEventListener("touchmove", $mHandle$$.$onDrag$), $e$jscomp$72$$.target.addEventListener("touchend", $mHandle$$.$onMouseUp$);
        } else {
          if ($entity$jscomp$13$$ == $stendhal$$.$zone$.$ground$) {
            $entity$jscomp$13$$.onclick($pos$jscomp$8$$.$canvasRelativeX$, $pos$jscomp$8$$.$canvasRelativeY$, !0);
          }
        }
      }
    }, $isRightClick$:function($e$jscomp$73$$) {
      return 300 < +new Date() - $stendhal$$.$ui$.$timestampMouseDown$ ? !0 : $e$jscomp$73$$.which ? 3 === $e$jscomp$73$$.which : 2 === $e$jscomp$73$$.button;
    }, $onMouseUp$:function($e$jscomp$74$$) {
      var $is_touch$jscomp$1_long_touch$jscomp$1$$ = $JSCompiler_StaticMethods_isTouchEvent$$($e$jscomp$74$$);
      $is_touch$jscomp$1_long_touch$jscomp$1$$ && $stendhal$$.$ui$.$touch$.$onTouchEnd$($e$jscomp$74$$);
      var $pos$jscomp$9$$ = $JSCompiler_StaticMethods_extractPosition$$($e$jscomp$74$$);
      $is_touch$jscomp$1_long_touch$jscomp$1$$ = $is_touch$jscomp$1_long_touch$jscomp$1$$ && $JSCompiler_StaticMethods_isLongTouch$$($e$jscomp$74$$);
      if ($e$jscomp$74$$ instanceof MouseEvent && $mHandle$$.$isRightClick$($e$jscomp$74$$) || $is_touch$jscomp$1_long_touch$jscomp$1$$) {
        $entity$jscomp$13$$ != $stendhal$$.$zone$.$ground$ && $stendhal$$.$ui$.$actionContextMenu$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Action", new $module$build$ts$ui$dialog$ActionContextMenu$default$$.$ActionContextMenu$($entity$jscomp$13$$, []), $pos$jscomp$9$$.pageX - 50, $pos$jscomp$9$$.pageY - 5));
      } else {
        if ($JSCompiler_StaticMethods_holdingItem$$()) {
          $stendhal$$.$ui$.$gamewindow$.$onDrop$($e$jscomp$74$$);
        } else {
          $entity$jscomp$13$$.onclick($pos$jscomp$9$$.$canvasRelativeX$, $pos$jscomp$9$$.$canvasRelativeY$);
        }
      }
      $mHandle$$.$cleanUp$($pos$jscomp$9$$);
      $pos$jscomp$9$$.target.focus();
      $e$jscomp$74$$.preventDefault();
    }, $onDrag$:function($e$jscomp$75$$) {
      $JSCompiler_StaticMethods_isTouchEvent$$($e$jscomp$75$$) && $stendhal$$.$ui$.$gamewindow$.$onDragStart$($e$jscomp$75$$);
      var $pos$jscomp$10_yDiff$$ = $JSCompiler_StaticMethods_extractPosition$$($e$jscomp$75$$), $xDiff$$ = $startX$$ - $pos$jscomp$10_yDiff$$.offsetX;
      $pos$jscomp$10_yDiff$$ = $startY$$ - $pos$jscomp$10_yDiff$$.offsetY;
      5 < $xDiff$$ * $xDiff$$ + $pos$jscomp$10_yDiff$$ * $pos$jscomp$10_yDiff$$ && $mHandle$$.$cleanUp$($e$jscomp$75$$);
    }, $cleanUp$:function($e$jscomp$76$$) {
      $entity$jscomp$13$$ = null;
      $e$jscomp$76$$.target && ($e$jscomp$76$$.target.removeEventListener("mouseup", $mHandle$$.$onMouseUp$), $e$jscomp$76$$.target.removeEventListener("mousemove", $mHandle$$.$onDrag$), $e$jscomp$76$$.target.removeEventListener("touchend", $mHandle$$.$onMouseUp$), $e$jscomp$76$$.target.removeEventListener("touchmove", $mHandle$$.$onDrag$), $JSCompiler_StaticMethods_unsetHeldItem$$(), $JSCompiler_StaticMethods_unsetOrigin$$());
    }};
    return $mHandle$$.$_onMouseDown$;
  }();
  var $element$jscomp$12$$ = document.getElementById("gamewindow");
  this.$ctx$ = $element$jscomp$12$$.getContext("2d");
  this.width = $element$jscomp$12$$.width;
  this.height = $element$jscomp$12$$.height;
};
$module$build$ts$ui$ViewPort$default$$.$ViewPort$.get = function() {
  $module$build$ts$ui$ViewPort$default$$.$ViewPort$.$v$ || ($module$build$ts$ui$ViewPort$default$$.$ViewPort$.$v$ = new $module$build$ts$ui$ViewPort$default$$.$ViewPort$());
  return $module$build$ts$ui$ViewPort$default$$.$ViewPort$.$v$;
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$ui$ViewPort$default$$.$ViewPort$.prototype;
$JSCompiler_prototypeAlias$$.$draw$ = function() {
  var $startTime$jscomp$7$$ = (new Date()).getTime();
  !$marauroa$$.$me$ || "visible" !== document.visibilityState || $marauroa$$.$currentZoneName$ !== $stendhal$$.data.map.$currentZoneName$ && "int_vault" !== $stendhal$$.data.map.$currentZoneName$ && "int_adventure_island" !== $stendhal$$.data.map.$currentZoneName$ && "tutorial_island" !== $stendhal$$.data.map.$currentZoneName$ || (this.$ctx$.globalAlpha = 1.0, $JSCompiler_StaticMethods_adjustView$$(this, this.$ctx$.canvas), this.$ctx$.fillStyle = "black", this.$ctx$.fillRect(0, 0, 10000, 10000), 
  $stendhal$$.data.map.$strategy$.$render$(this.$ctx$.canvas, this, Math.floor(this.offsetX / this.$targetTileWidth$), Math.floor(this.offsetY / this.$targetTileHeight$), this.$targetTileWidth$, this.$targetTileHeight$), this.$A$.$draw$(this.$ctx$), $JSCompiler_StaticMethods_drawEntitiesTop$$(this), $JSCompiler_StaticMethods_drawEmojiSprites$$(this), $JSCompiler_StaticMethods_drawTextSprites$$(this), $JSCompiler_StaticMethods_drawTextSprites$$(this, this.$v$), $stendhal$$.$ui$.$touch$.$held$ && $JSCompiler_StaticMethods_drawHeld$$(this.$ctx$), 
  $stendhal$$.$ui$.$equip$.update(), $module$build$ts$ui$UI$default$$.$ui$.get(106).update());
  setTimeout(function() {
    $stendhal$$.$ui$.$gamewindow$.$draw$.apply($stendhal$$.$ui$.$gamewindow$, arguments);
  }, Math.max(50 - ((new Date()).getTime() - $startTime$jscomp$7$$), 1));
};
$JSCompiler_prototypeAlias$$.$drawEntities$ = function() {
  var $currentTime_entity$jscomp$14$$ = (new Date()).getTime(), $time$jscomp$1$$ = $currentTime_entity$jscomp$14$$ - this.timeStamp;
  this.timeStamp = $currentTime_entity$jscomp$14$$;
  for (var $i$jscomp$73$$ in $stendhal$$.$zone$.$entities$) {
    $currentTime_entity$jscomp$14$$ = $stendhal$$.$zone$.$entities$[$i$jscomp$73$$], "undefined" != typeof $currentTime_entity$jscomp$14$$.$draw$ && ($currentTime_entity$jscomp$14$$.$updatePosition$($time$jscomp$1$$), $currentTime_entity$jscomp$14$$.$draw$(this.$ctx$));
  }
};
function $JSCompiler_StaticMethods_drawEntitiesTop$$($JSCompiler_StaticMethods_drawEntitiesTop$self$$) {
  for (var $i$jscomp$74$$ in $stendhal$$.$zone$.$entities$) {
    var $entity$jscomp$15$$ = $stendhal$$.$zone$.$entities$[$i$jscomp$74$$];
    "undefined" !== typeof $entity$jscomp$15$$.$setStatusBarOffset$ && $entity$jscomp$15$$.$setStatusBarOffset$();
    "undefined" != typeof $entity$jscomp$15$$.$drawTop$ && $entity$jscomp$15$$.$drawTop$($JSCompiler_StaticMethods_drawEntitiesTop$self$$.$ctx$);
  }
}
function $JSCompiler_StaticMethods_drawTextSprites$$($JSCompiler_StaticMethods_drawTextSprites$self$$, $sgroup$$) {
  $sgroup$$ = void 0 === $sgroup$$ ? $JSCompiler_StaticMethods_drawTextSprites$self$$.$textSprites$ : $sgroup$$;
  for (var $i$jscomp$75$$ = 0; $i$jscomp$75$$ < $sgroup$$.length; $i$jscomp$75$$++) {
    var $sprite$$ = $sgroup$$[$i$jscomp$75$$];
    $sprite$$.$draw$($JSCompiler_StaticMethods_drawTextSprites$self$$.$ctx$) && ($sgroup$$.splice($i$jscomp$75$$, 1), $sprite$$.$onRemoved$(), $i$jscomp$75$$--);
  }
}
function $JSCompiler_StaticMethods_drawEmojiSprites$$($JSCompiler_StaticMethods_drawEmojiSprites$self$$) {
  for (var $i$jscomp$76$$ = 0; $i$jscomp$76$$ < $JSCompiler_StaticMethods_drawEmojiSprites$self$$.$emojiSprites$.length; $i$jscomp$76$$++) {
    $JSCompiler_StaticMethods_drawEmojiSprites$self$$.$emojiSprites$[$i$jscomp$76$$].$draw$($JSCompiler_StaticMethods_drawEmojiSprites$self$$.$ctx$) && ($JSCompiler_StaticMethods_drawEmojiSprites$self$$.$emojiSprites$.splice($i$jscomp$76$$, 1), $i$jscomp$76$$--);
  }
}
function $JSCompiler_StaticMethods_adjustView$$($JSCompiler_StaticMethods_adjustView$self$$, $canvas$jscomp$10$$) {
  $JSCompiler_StaticMethods_adjustView$self$$.$ctx$.setTransform(1, 0, 0, 1, 0, 0);
  var $centerX$$ = $marauroa$$.$me$._x * $JSCompiler_StaticMethods_adjustView$self$$.$targetTileWidth$ + $JSCompiler_StaticMethods_adjustView$self$$.$targetTileWidth$ / 2 - $canvas$jscomp$10$$.width / 2, $centerY$$ = $marauroa$$.$me$._y * $JSCompiler_StaticMethods_adjustView$self$$.$targetTileHeight$ + $JSCompiler_StaticMethods_adjustView$self$$.$targetTileHeight$ / 2 - $canvas$jscomp$10$$.height / 2;
  $centerX$$ = Math.min($centerX$$, $stendhal$$.data.map.$zoneSizeX$ * $JSCompiler_StaticMethods_adjustView$self$$.$targetTileWidth$ - $canvas$jscomp$10$$.width);
  $centerX$$ = Math.max($centerX$$, 0);
  $centerY$$ = Math.min($centerY$$, $stendhal$$.data.map.$zoneSizeY$ * $JSCompiler_StaticMethods_adjustView$self$$.$targetTileHeight$ - $canvas$jscomp$10$$.height);
  $centerY$$ = Math.max($centerY$$, 0);
  $JSCompiler_StaticMethods_adjustView$self$$.offsetX = Math.round($centerX$$);
  $JSCompiler_StaticMethods_adjustView$self$$.offsetY = Math.round($centerY$$);
  $JSCompiler_StaticMethods_adjustView$self$$.$ctx$.translate(-$JSCompiler_StaticMethods_adjustView$self$$.offsetX, -$JSCompiler_StaticMethods_adjustView$self$$.offsetY);
}
$JSCompiler_prototypeAlias$$.$onExitZone$ = function() {
  for (var $$jscomp$iter$66$$ = $$jscomp$makeIterator$$([this.$textSprites$, this.$emojiSprites$]), $$jscomp$key$sgroup_sgroup$jscomp$1$$ = $$jscomp$iter$66$$.next(); !$$jscomp$key$sgroup_sgroup$jscomp$1$$.done; $$jscomp$key$sgroup_sgroup$jscomp$1$$ = $$jscomp$iter$66$$.next()) {
    $$jscomp$key$sgroup_sgroup$jscomp$1$$ = $$jscomp$key$sgroup_sgroup$jscomp$1$$.value;
    for (var $idx$jscomp$19$$ = $$jscomp$key$sgroup_sgroup$jscomp$1$$.length - 1; 0 <= $idx$jscomp$19$$; $idx$jscomp$19$$--) {
      var $sprite$jscomp$6$$ = $$jscomp$key$sgroup_sgroup$jscomp$1$$[$idx$jscomp$19$$];
      $$jscomp$key$sgroup_sgroup$jscomp$1$$.splice($idx$jscomp$19$$, 1);
      $sprite$jscomp$6$$ instanceof $module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$ && $sprite$jscomp$6$$.$onRemoved$();
    }
  }
};
$JSCompiler_prototypeAlias$$.$onMouseMove$ = function($e$jscomp$77_x$jscomp$133$$) {
  var $pos$jscomp$11_y$jscomp$113$$ = $JSCompiler_StaticMethods_extractPosition$$($e$jscomp$77_x$jscomp$133$$);
  $e$jscomp$77_x$jscomp$133$$ = $pos$jscomp$11_y$jscomp$113$$.$canvasRelativeX$ + $stendhal$$.$ui$.$gamewindow$.offsetX;
  $pos$jscomp$11_y$jscomp$113$$ = $pos$jscomp$11_y$jscomp$113$$.$canvasRelativeY$ + $stendhal$$.$ui$.$gamewindow$.offsetY;
  var $entity$jscomp$16$$ = $JSCompiler_StaticMethods_entityAt$$($e$jscomp$77_x$jscomp$133$$, $pos$jscomp$11_y$jscomp$113$$);
  document.getElementById("gamewindow").style.cursor = $entity$jscomp$16$$.$getCursor$($e$jscomp$77_x$jscomp$133$$, $pos$jscomp$11_y$jscomp$113$$);
};
$JSCompiler_prototypeAlias$$.$onMouseWheel$ = function($e$jscomp$78$$) {
  if ($marauroa$$.$me$) {
    $e$jscomp$78$$.preventDefault();
    var $currentDir$$ = parseInt($marauroa$$.$me$.dir, 10), $newDir$$ = null;
    "number" === typeof $currentDir$$ && (100 <= $e$jscomp$78$$.deltaY ? ($newDir$$ = $currentDir$$ + 1, 4 < $newDir$$ && ($newDir$$ = 1)) : -100 >= $e$jscomp$78$$.deltaY && ($newDir$$ = $currentDir$$ - 1, 1 > $newDir$$ && ($newDir$$ = 4)));
    null != $newDir$$ && $marauroa$$.$clientFramework$.$sendAction$({type:"face", dir:"" + $newDir$$});
  }
};
$JSCompiler_prototypeAlias$$.$onDragStart$ = function($e$jscomp$79$$) {
  var $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$ = $JSCompiler_StaticMethods_extractPosition$$($e$jscomp$79$$), $draggedEntity$$;
  $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getEntitiesAt$$($stendhal$$.$zone$, $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$.$canvasRelativeX$ + $stendhal$$.$ui$.$gamewindow$.offsetX, $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$.$canvasRelativeY$ + $stendhal$$.$ui$.$gamewindow$.offsetY));
  for (var $$jscomp$key$obj_obj$jscomp$40$$ = $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$.next(); !$$jscomp$key$obj_obj$jscomp$40$$.done; $$jscomp$key$obj_obj$jscomp$40$$ = $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$.next()) {
    $$jscomp$key$obj_obj$jscomp$40$$ = $$jscomp$key$obj_obj$jscomp$40$$.value, $$jscomp$key$obj_obj$jscomp$40$$.$isDraggable$() && ($draggedEntity$$ = $$jscomp$key$obj_obj$jscomp$40$$);
  }
  if ($draggedEntity$$ && "item" === $draggedEntity$$.type) {
    $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$ = $JSCompiler_StaticMethods_getAreaOf$$($stendhal$$.data.$sprites$, $stendhal$$.data.$sprites$.get($draggedEntity$$.$sprite$.filename), 32, 32), $stendhal$$.$ui$.$heldItem$ = {path:$JSCompiler_StaticMethods_getIdPath$$($draggedEntity$$), $zone$:$marauroa$$.$currentZoneName$, $quantity$:$draggedEntity$$.hasOwnProperty("quantity") ? $draggedEntity$$.quantity : 1};
  } else {
    if ($draggedEntity$$ && "corpse" === $draggedEntity$$.type) {
      $$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$ = $stendhal$$.data.$sprites$.get($draggedEntity$$.$sprite$.filename), $stendhal$$.$ui$.$heldItem$ = {path:$JSCompiler_StaticMethods_getIdPath$$($draggedEntity$$), $zone$:$marauroa$$.$currentZoneName$, $quantity$:1};
    } else {
      $e$jscomp$79$$.preventDefault();
      return;
    }
  }
  $e$jscomp$79$$.dataTransfer && (window.event = $e$jscomp$79$$, $e$jscomp$79$$.dataTransfer.setDragImage($$jscomp$iter$67_img$jscomp$14_pos$jscomp$12$$, 0, 0));
};
$JSCompiler_prototypeAlias$$.$onDragOver$ = function($e$jscomp$80$$) {
  $e$jscomp$80$$.preventDefault();
  $e$jscomp$80$$.dataTransfer && ($e$jscomp$80$$.dataTransfer.dropEffect = "move");
  return !1;
};
$JSCompiler_prototypeAlias$$.$onDrop$ = function($e$jscomp$81$$) {
  if ($stendhal$$.$ui$.$heldItem$) {
    var $pos$jscomp$13$$ = $JSCompiler_StaticMethods_extractPosition$$($e$jscomp$81$$), $action$jscomp$59$$ = {x:Math.floor(($pos$jscomp$13$$.$canvasRelativeX$ + $stendhal$$.$ui$.$gamewindow$.offsetX) / 32).toString(), y:Math.floor(($pos$jscomp$13$$.$canvasRelativeY$ + $stendhal$$.$ui$.$gamewindow$.offsetY) / 32).toString(), zone:$stendhal$$.$ui$.$heldItem$.$zone$}, $id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$ = $stendhal$$.$ui$.$heldItem$.path.substr(1, $stendhal$$.$ui$.$heldItem$.path.length - 
    2);
    /\t/.test($id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$) ? ($action$jscomp$59$$.type = "drop", $action$jscomp$59$$.source_path = $stendhal$$.$ui$.$heldItem$.path) : ($action$jscomp$59$$.type = "displace", $action$jscomp$59$$.baseitem = $id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$);
    $id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$ = $stendhal$$.$ui$.$heldItem$.$quantity$;
    $stendhal$$.$ui$.$heldItem$ = void 0;
    $id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$ = $JSCompiler_StaticMethods_holdingItem$$() && 1 < $id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$;
    $e$jscomp$81$$.ctrlKey || $id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$ ? $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Quantity", new $module$build$ts$ui$dialog$DropQuantitySelectorDialog$default$$.$DropQuantitySelectorDialog$($action$jscomp$59$$, $id$jscomp$30_quantity$jscomp$4_touch_held$jscomp$1$$), $pos$jscomp$13$$.pageX - 50, $pos$jscomp$13$$.pageY - 25) : $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$59$$);
  }
  $e$jscomp$81$$.stopPropagation();
  $e$jscomp$81$$.preventDefault();
};
$JSCompiler_prototypeAlias$$.$onTouchEnd$ = function($e$jscomp$82$$) {
  $stendhal$$.$ui$.$touch$.$onTouchEnd$();
  $stendhal$$.$ui$.$gamewindow$.$onDrop$($e$jscomp$82$$);
  $JSCompiler_StaticMethods_holdingItem$$() && ($JSCompiler_StaticMethods_unsetHeldItem$$(), $JSCompiler_StaticMethods_unsetOrigin$$());
  $module$build$ts$Client$default$$.$Client$.$B$($e$jscomp$82$$);
};
$JSCompiler_prototypeAlias$$.$onContentMenu$ = function($e$jscomp$83$$) {
  $e$jscomp$83$$.preventDefault();
};
function $JSCompiler_StaticMethods_updateJoystick$$() {
  var $JSCompiler_StaticMethods_updateJoystick$self$$ = $stendhal$$.$ui$.$gamewindow$;
  $JSCompiler_StaticMethods_updateJoystick$self$$.$joystick$.$onRemoved$();
  switch($stendhal$$.$config$.get("ui.joystick")) {
    case "joystick":
      $JSCompiler_StaticMethods_updateJoystick$self$$.$joystick$ = new $module$build$ts$ui$joystick$Joystick$default$$.$Joystick$();
      break;
    case "dpad":
      $JSCompiler_StaticMethods_updateJoystick$self$$.$joystick$ = new $module$build$ts$ui$joystick$DirectionPad$default$$.$DirectionPad$();
      break;
    default:
      $JSCompiler_StaticMethods_updateJoystick$self$$.$joystick$ = new $module$build$ts$ui$joystick$JoystickBase$default$$.$JoystickBase$();
  }
}
;var $module$build$ts$SingletonRepo$default$$ = {};
Object.defineProperty($module$build$ts$SingletonRepo$default$$, "__esModule", {value:!0});
$module$build$ts$SingletonRepo$default$$.$singletons$ = $module$build$ts$SingletonRepo$default$$.$SingletonRepo$ = void 0;
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$ = function() {
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$H$ = function() {
  return $module$build$ts$data$CStatus$default$$.$CStatus$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$I$ = function() {
  return $module$build$ts$data$CacheManager$default$$.$CacheManager$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$V$ = function() {
  return $module$build$ts$Client$default$$.$Client$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$A$ = function() {
  return $module$build$ts$util$ConfigManager$default$$.$ConfigManager$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$B$ = function() {
  return $module$build$ts$data$EmojiStore$default$$.$EmojiStore$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$J$ = function() {
  return $module$build$ts$EventRegistry$default$$.$EventRegistry$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$D$ = function() {
  return $module$build$ts$data$GroupManager$default$$.$GroupManager$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$K$ = function() {
  return $module$build$ts$ui$HTMLManager$default$$.$HTMLManager$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$getInventory$ = function() {
  return $module$build$ts$ui$Inventory$default$$.$Inventory$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$L$ = function() {
  return $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$;
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$M$ = function() {
  return $module$build$ts$ui$LoopedSoundSourceManager$default$$.$LoopedSoundSourceManager$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$N$ = function() {
  return $module$build$ts$data$Map$default$$.Map.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$O$ = function() {
  return $module$build$ts$data$OutfitStore$default$$.$OutfitStore$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$P$ = function() {
  return $module$build$ts$data$Paths$default$$.$Paths$;
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$F$ = function() {
  return $module$build$ts$util$SessionManager$default$$.$SessionManager$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$G$ = function() {
  return $module$build$ts$SlashActionRepo$default$$.$SlashActionRepo$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$v$ = function() {
  return $module$build$ts$ui$SoundManager$default$$.$SoundManager$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$R$ = function() {
  return $module$build$ts$data$SpriteStore$default$$.store;
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$S$ = function() {
  return $module$build$ts$data$TileStore$default$$.$TileStore$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$T$ = function() {
  return $module$build$ts$ui$TouchHandler$default$$.$TouchHandler$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$U$ = function() {
  return $module$build$ts$ui$ViewPort$default$$.$ViewPort$.get();
};
$module$build$ts$SingletonRepo$default$$.$SingletonRepo$.$C$ = function() {
  return $module$build$ts$util$WeatherRenderer$default$$.$WeatherRenderer$.get();
};
$module$build$ts$SingletonRepo$default$$.$singletons$ = $module$build$ts$SingletonRepo$default$$.$SingletonRepo$;
var $module$build$ts$entity$Ground$default$$ = {};
Object.defineProperty($module$build$ts$entity$Ground$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Ground$default$$.$Ground$ = void 0;
$module$build$ts$entity$Ground$default$$.$Ground$ = function() {
};
$module$build$ts$entity$Ground$default$$.$Ground$.prototype.$isVisibleToAction$ = function() {
  return !1;
};
$module$build$ts$entity$Ground$default$$.$Ground$.prototype.$getCursor$ = function($x$jscomp$135$$, $y$jscomp$115$$) {
  return 15 > $x$jscomp$135$$ || 15 > $y$jscomp$115$$ || $x$jscomp$135$$ > 32 * $stendhal$$.data.map.$zoneSizeX$ - 15 || $y$jscomp$115$$ > 32 * $stendhal$$.data.map.$zoneSizeY$ - 15 ? "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/walkborder.png) 1 3, auto" : $JSCompiler_StaticMethods_collision$$(Math.floor($x$jscomp$135$$ / 32), Math.floor($y$jscomp$115$$ / 32)) ? "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/stop.png) 1 3, auto" : "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/walk.png) 1 3, auto";
};
$module$build$ts$entity$Ground$default$$.$Ground$.prototype.onclick = function($gameX_x$jscomp$136$$, $action$jscomp$60_y$jscomp$116$$, $dblclick_extend$$) {
  if ($JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "client.pathfinding")) {
    $gameX_x$jscomp$136$$ += $stendhal$$.$ui$.$gamewindow$.offsetX;
    var $gameY$$ = $action$jscomp$60_y$jscomp$116$$ + $stendhal$$.$ui$.$gamewindow$.offsetY;
    $action$jscomp$60_y$jscomp$116$$ = {type:"moveto", x:"" + Math.floor($gameX_x$jscomp$136$$ / 32), y:"" + Math.floor($gameY$$ / 32)};
    "boolean" == typeof $dblclick_extend$$ && $dblclick_extend$$ && ($action$jscomp$60_y$jscomp$116$$.double_click = "");
    if ($dblclick_extend$$ = 15 > $gameX_x$jscomp$136$$ ? "4" : $gameX_x$jscomp$136$$ > 32 * $stendhal$$.data.map.$zoneSizeX$ - 15 ? "2" : 15 > $gameY$$ ? "1" : $gameY$$ > 32 * $stendhal$$.data.map.$zoneSizeY$ - 15 ? "3" : null) {
      $action$jscomp$60_y$jscomp$116$$.extend = $dblclick_extend$$;
    }
    $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$60_y$jscomp$116$$);
  }
};
var $module$build$ts$entity$Zone$default$$ = {};
Object.defineProperty($module$build$ts$entity$Zone$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Zone$default$$.$Zone$ = void 0;
$module$build$ts$entity$Zone$default$$.$Zone$ = function() {
  this.$entities$ = [];
};
function $JSCompiler_StaticMethods_getEntitiesAt$$($JSCompiler_StaticMethods_getEntitiesAt$self$$, $x$jscomp$137_xGrid$$, $y$jscomp$117_yGrid$$) {
  $x$jscomp$137_xGrid$$ /= 32;
  $y$jscomp$117_yGrid$$ /= 32;
  var $entities$$ = [], $i$jscomp$77$$;
  for ($i$jscomp$77$$ in $JSCompiler_StaticMethods_getEntitiesAt$self$$.$entities$) {
    var $obj$jscomp$41$$ = $JSCompiler_StaticMethods_getEntitiesAt$self$$.$entities$[$i$jscomp$77$$];
    $obj$jscomp$41$$.$isVisibleToAction$() && $obj$jscomp$41$$._x <= $x$jscomp$137_xGrid$$ && $obj$jscomp$41$$._y <= $y$jscomp$117_yGrid$$ && $obj$jscomp$41$$._x + ($obj$jscomp$41$$.width || 1) >= $x$jscomp$137_xGrid$$ && $obj$jscomp$41$$._y + ($obj$jscomp$41$$.height || 1) >= $y$jscomp$117_yGrid$$ && $entities$$.push($obj$jscomp$41$$);
  }
  return $entities$$;
}
function $JSCompiler_StaticMethods_entityAt$$($x$jscomp$138$$, $y$jscomp$118$$) {
  for (var $JSCompiler_StaticMethods_entityAt$self$$ = $stendhal$$.$zone$, $res$jscomp$7$$ = $stendhal$$.$zone$.$ground$, $$jscomp$iter$68_obj$101$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getEntitiesAt$$($JSCompiler_StaticMethods_entityAt$self$$, $x$jscomp$138$$, $y$jscomp$118$$)), $$jscomp$key$obj$jscomp$1_localX$$ = $$jscomp$iter$68_obj$101$$.next(); !$$jscomp$key$obj$jscomp$1_localX$$.done; $$jscomp$key$obj$jscomp$1_localX$$ = $$jscomp$iter$68_obj$101$$.next()) {
    $res$jscomp$7$$ = $$jscomp$key$obj$jscomp$1_localX$$.value;
  }
  if ($res$jscomp$7$$ != $stendhal$$.$zone$.$ground$) {
    return $res$jscomp$7$$;
  }
  for (var $i$jscomp$78$$ in $JSCompiler_StaticMethods_entityAt$self$$.$entities$) {
    if ($$jscomp$iter$68_obj$101$$ = $JSCompiler_StaticMethods_entityAt$self$$.$entities$[$i$jscomp$78$$], $$jscomp$iter$68_obj$101$$.$isVisibleToAction$() && $$jscomp$iter$68_obj$101$$.drawHeight) {
      $$jscomp$key$obj$jscomp$1_localX$$ = 32 * $$jscomp$iter$68_obj$101$$._x;
      var $localY$$ = 32 * $$jscomp$iter$68_obj$101$$._y, $drawHeight$$ = $$jscomp$iter$68_obj$101$$.drawHeight, $drawWidth$$ = $$jscomp$iter$68_obj$101$$.drawWidth, $drawX$jscomp$1$$ = (32 * $$jscomp$iter$68_obj$101$$.width - $drawWidth$$) / 2, $drawY$jscomp$1$$ = 32 * $$jscomp$iter$68_obj$101$$.height - $drawHeight$$;
      $$jscomp$key$obj$jscomp$1_localX$$ + $drawX$jscomp$1$$ <= $x$jscomp$138$$ && $$jscomp$key$obj$jscomp$1_localX$$ + $drawX$jscomp$1$$ + $drawWidth$$ >= $x$jscomp$138$$ && $localY$$ + $drawY$jscomp$1$$ <= $y$jscomp$118$$ && $localY$$ + $drawY$jscomp$1$$ + $drawHeight$$ >= $y$jscomp$118$$ && ($res$jscomp$7$$ = $$jscomp$iter$68_obj$101$$);
    }
  }
  return $res$jscomp$7$$;
}
function $JSCompiler_StaticMethods_sortEntities$$() {
  var $JSCompiler_StaticMethods_sortEntities$self$$ = $stendhal$$.$zone$;
  $JSCompiler_StaticMethods_sortEntities$self$$.$entities$ = [];
  for (var $i$jscomp$79$$ in $marauroa$$.$currentZone$) {
    $marauroa$$.$currentZone$.hasOwnProperty($i$jscomp$79$$) && "function" != typeof $marauroa$$.$currentZone$[$i$jscomp$79$$] && $JSCompiler_StaticMethods_sortEntities$self$$.$entities$.push($marauroa$$.$currentZone$[$i$jscomp$79$$]);
  }
  $JSCompiler_StaticMethods_sortEntities$self$$.$entities$.sort(function($entity1$$, $entity2$$) {
    var $rv$$ = $entity1$$.zIndex - $entity2$$.zIndex;
    0 == $rv$$ && ($rv$$ = $entity1$$.y + $entity1$$.height - ($entity2$$.y + $entity2$$.height), 0 == $rv$$ && ($rv$$ = $entity1$$.id - $entity2$$.id));
    return $rv$$;
  });
}
;var $module$build$ts$ui$dialog$ApplicationMenuDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$ApplicationMenuDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$ApplicationMenuDialog$default$$.$ApplicationMenuDialog$ = void 0;
var $slashActions$$module$build$ts$ui$dialog$ApplicationMenuDialog$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$G$();
$module$build$ts$ui$dialog$ApplicationMenuDialog$default$$.$ApplicationMenuDialog$ = function() {
  var $$jscomp$super$this$jscomp$24$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "applicationmenudialog-template") || this;
  $$jscomp$super$this$jscomp$24$$.actions = [{title:"Account", children:[{title:"Change Password", action:"changepassword",}, {title:"Select character", action:"characterselector",}, {title:"Login History", action:"loginhistory",}]}, {title:"Tools", children:[{title:"Take Screenshot", action:"screenshot",}, {title:"Settings", action:"settings",}]}, {title:"Commands", children:[{title:"Atlas", action:"atlas",}, {title:"Online Players", action:"who",}, {title:"Hall of Fame", action:"halloffame",}, 
  {title:"Travel Log", action:"progressstatus",}]}, {title:"Help", children:[{title:"Manual", action:"manual",}, {title:"FAQ", action:"faq",}, {title:"Beginners Guide", action:"beginnersguide",}, {title:"Commands", action:"help",}, {title:"Rules", action:"rules",}]},];
  for (var $content$jscomp$9$$ = "", $i$jscomp$80$$ = 0; $i$jscomp$80$$ < $$jscomp$super$this$jscomp$24$$.actions.length; $i$jscomp$80$$++) {
    $content$jscomp$9$$ += '<div class="inlineblock buttonColumn"><h4 class="menugroup">' + $JSCompiler_StaticMethods_esc$$($$jscomp$super$this$jscomp$24$$.actions[$i$jscomp$80$$].title) + "</h4>";
    for (var $j$jscomp$5$$ = 0; $j$jscomp$5$$ < $$jscomp$super$this$jscomp$24$$.actions[$i$jscomp$80$$].children.length; $j$jscomp$5$$++) {
      $content$jscomp$9$$ += '<button id="menubutton.' + $$jscomp$super$this$jscomp$24$$.actions[$i$jscomp$80$$].children[$j$jscomp$5$$].action + '" class="menubutton">' + $JSCompiler_StaticMethods_esc$$($$jscomp$super$this$jscomp$24$$.actions[$i$jscomp$80$$].children[$j$jscomp$5$$].title) + "</button><br>";
    }
    $content$jscomp$9$$ += "</div>";
  }
  $$jscomp$super$this$jscomp$24$$.$componentElement$.innerHTML = $content$jscomp$9$$;
  $$jscomp$super$this$jscomp$24$$.$componentElement$.addEventListener("click", function($event$jscomp$50$$) {
    var $_a$jscomp$inline_624$$, $cmd$jscomp$inline_625$$ = null === ($_a$jscomp$inline_624$$ = $event$jscomp$50$$.target.id) || void 0 === $_a$jscomp$inline_624$$ ? void 0 : $_a$jscomp$inline_624$$.substring(11);
    $cmd$jscomp$inline_625$$ && $slashActions$$module$build$ts$ui$dialog$ApplicationMenuDialog$$.$execute$("/" + $cmd$jscomp$inline_625$$);
    $$jscomp$super$this$jscomp$24$$.$componentElement$.dispatchEvent(new Event("close"));
    $event$jscomp$50$$.preventDefault();
  });
  return $$jscomp$super$this$jscomp$24$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$ApplicationMenuDialog$default$$.$ApplicationMenuDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
var $module$build$ts$ui$dialog$ChooseCharacterDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$ChooseCharacterDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$ChooseCharacterDialog$default$$.$ChooseCharacterDialog$ = void 0;
$module$build$ts$ui$dialog$ChooseCharacterDialog$default$$.$ChooseCharacterDialog$ = function($characters$jscomp$1$$) {
  var $$jscomp$super$this$jscomp$25$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "choose-character-template") || this;
  var $$jscomp$loop$115$$ = {}, $i$jscomp$81$$;
  for ($i$jscomp$81$$ in $characters$jscomp$1$$) {
    if ($characters$jscomp$1$$.hasOwnProperty($i$jscomp$81$$)) {
      $$jscomp$loop$115$$.$$jscomp$loop$prop$name$116$ = $characters$jscomp$1$$[$i$jscomp$81$$].a.name;
      var $button$jscomp$2$$ = document.createElement("button");
      $button$jscomp$2$$.innerText = $$jscomp$loop$115$$.$$jscomp$loop$prop$name$116$;
      $button$jscomp$2$$.addEventListener("click", function($$jscomp$loop$115$jscomp$1$$) {
        return function() {
          $$jscomp$super$this$jscomp$25$$.$componentElement$.dispatchEvent(new Event("close"));
          $module$build$ts$Client$default$$.$Client$.get().$chooseCharacter$($$jscomp$loop$115$jscomp$1$$.$$jscomp$loop$prop$name$116$);
        };
      }($$jscomp$loop$115$$));
      $$jscomp$super$this$jscomp$25$$.$componentElement$.append($button$jscomp$2$$);
    }
    $$jscomp$loop$115$$ = {$$jscomp$loop$prop$name$116$:$$jscomp$loop$115$$.$$jscomp$loop$prop$name$116$};
  }
  return $$jscomp$super$this$jscomp$25$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$ChooseCharacterDialog$default$$.$ChooseCharacterDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
var $module$build$ts$ui$dialog$CreateAccountDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$CreateAccountDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$CreateAccountDialog$default$$.$CreateAccountDialog$ = void 0;
$module$build$ts$ui$dialog$CreateAccountDialog$default$$.$CreateAccountDialog$ = function() {
  var $$jscomp$super$this$jscomp$26$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "createaccountdialog-template") || this;
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$26$$, "input[type=submit]").addEventListener("click", function($event$jscomp$52_username$jscomp$4$$) {
    $event$jscomp$52_username$jscomp$4$$.preventDefault();
    $event$jscomp$52_username$jscomp$4$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$26$$, "#username").value;
    var $password$jscomp$2$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$26$$, "#password").value, $passwordRepeat$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$26$$, "#passwordrepeat").value, $email$jscomp$1$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$26$$, "#email").value;
    $event$jscomp$52_username$jscomp$4$$ && $password$jscomp$2$$ && $passwordRepeat$$ ? $password$jscomp$2$$ != $passwordRepeat$$ ? alert("Password and password repetition do not match.") : $marauroa$$.$clientFramework$.$createAccount$($event$jscomp$52_username$jscomp$4$$, $password$jscomp$2$$, $email$jscomp$1$$) : alert("Please fill in all required fields.");
  });
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$26$$, "a").addEventListener("click", function($event$jscomp$53$$) {
    $event$jscomp$53$$.preventDefault();
    $$jscomp$super$this$jscomp$26$$.close();
    $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Login", new $module$build$ts$ui$dialog$LoginDialog$default$$.$LoginDialog$(), 100, 50);
  });
  return $$jscomp$super$this$jscomp$26$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$CreateAccountDialog$default$$.$CreateAccountDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
var $module$build$ts$ui$dialog$LoginDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$LoginDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$LoginDialog$default$$.$LoginDialog$ = void 0;
$module$build$ts$ui$dialog$LoginDialog$default$$.$LoginDialog$ = function() {
  var $$jscomp$super$this$jscomp$27$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "logindialog-template") || this;
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$27$$, "button").addEventListener("click", function($event$jscomp$54_username$jscomp$5$$) {
    $event$jscomp$54_username$jscomp$5$$.preventDefault();
    $event$jscomp$54_username$jscomp$5$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$27$$, "#username").value;
    var $password$jscomp$3$$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$27$$, "#password").value;
    $module$build$ts$Client$default$$.$Client$.get().username = $event$jscomp$54_username$jscomp$5$$;
    $marauroa$$.$clientFramework$.$login$($event$jscomp$54_username$jscomp$5$$, $password$jscomp$3$$);
    $$jscomp$super$this$jscomp$27$$.close();
  });
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$27$$, "a").addEventListener("click", function($event$jscomp$55$$) {
    $event$jscomp$55$$.preventDefault();
    $$jscomp$super$this$jscomp$27$$.close();
    $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Create Account", new $module$build$ts$ui$dialog$CreateAccountDialog$default$$.$CreateAccountDialog$(), 100, 50);
  });
  return $$jscomp$super$this$jscomp$27$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$LoginDialog$default$$.$LoginDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
var $module$build$ts$ui$toolkit$Panel$default$$ = {};
Object.defineProperty($module$build$ts$ui$toolkit$Panel$default$$, "__esModule", {value:!0});
$module$build$ts$ui$toolkit$Panel$default$$.$Panel$ = void 0;
$module$build$ts$ui$toolkit$Panel$default$$.$Panel$ = function($$jscomp$super$this$jscomp$28_id$jscomp$31$$, $themable$jscomp$1$$) {
  $$jscomp$super$this$jscomp$28_id$jscomp$31$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, $$jscomp$super$this$jscomp$28_id$jscomp$31$$, void 0 === $themable$jscomp$1$$ ? !1 : $themable$jscomp$1$$) || this;
  $$jscomp$super$this$jscomp$28_id$jscomp$31$$.children = [];
  $$jscomp$super$this$jscomp$28_id$jscomp$31$$.$A$ = $$jscomp$super$this$jscomp$28_id$jscomp$31$$.$componentElement$;
  return $$jscomp$super$this$jscomp$28_id$jscomp$31$$;
};
$$jscomp$inherits$$($module$build$ts$ui$toolkit$Panel$default$$.$Panel$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$toolkit$Panel$default$$.$Panel$.prototype.add = function($child$$) {
  -1 < this.children.indexOf($child$$) || (this.children.push($child$$), this.$A$.contains($child$$.$componentElement$) || this.$A$.append($child$$.$componentElement$));
};
$module$build$ts$ui$toolkit$Panel$default$$.$Panel$.prototype.remove = function($child$jscomp$1$$) {
  var $index$jscomp$83$$ = this.children.indexOf($child$jscomp$1$$);
  0 > $index$jscomp$83$$ || (this.children.splice($index$jscomp$83$$, 1), this.$A$.contains($child$jscomp$1$$.$componentElement$) && $child$jscomp$1$$.$componentElement$.remove());
};
$module$build$ts$ui$toolkit$Panel$default$$.$Panel$.prototype.clear = function() {
  this.children = [];
  this.$A$.innerHTML = "";
};
var $module$build$ts$ui$toolkit$TabPanelComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$toolkit$TabPanelComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$toolkit$TabPanelComponent$default$$.$TabPanelComponent$ = void 0;
$module$build$ts$ui$toolkit$TabPanelComponent$default$$.$TabPanelComponent$ = function() {
  var $$jscomp$super$this$jscomp$29$$ = $module$build$ts$ui$toolkit$Panel$default$$.$Panel$.call(this, "tabpanel-template") || this;
  $$jscomp$super$this$jscomp$29$$.$v$ = 0;
  $$jscomp$super$this$jscomp$29$$.buttons = [];
  $$jscomp$super$this$jscomp$29$$.$A$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$29$$, ".tabpanel-content");
  return $$jscomp$super$this$jscomp$29$$;
};
$$jscomp$inherits$$($module$build$ts$ui$toolkit$TabPanelComponent$default$$.$TabPanelComponent$, $module$build$ts$ui$toolkit$Panel$default$$.$Panel$);
$module$build$ts$ui$toolkit$TabPanelComponent$default$$.$TabPanelComponent$.prototype.add = function($child$jscomp$2$$) {
  this.children.length != this.$v$ && ($child$jscomp$2$$.$componentElement$.style.display = "none");
  $module$build$ts$ui$toolkit$Panel$default$$.$Panel$.prototype.add.call(this, $child$jscomp$2$$);
};
function $JSCompiler_StaticMethods_addTab$$($JSCompiler_StaticMethods_addTab$self$$, $label$jscomp$9$$) {
  var $button$jscomp$3$$ = document.createElement("button");
  $button$jscomp$3$$.innerText = $label$jscomp$9$$;
  $button$jscomp$3$$.dataset.index = "" + $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_addTab$self$$, ".tabpanel-tabs").children.length;
  $button$jscomp$3$$.addEventListener("click", function($e$jscomp$84$$) {
    $JSCompiler_StaticMethods_setCurrentTab$$($JSCompiler_StaticMethods_addTab$self$$, Number.parseInt($e$jscomp$84$$.target.dataset.index, 10));
  });
  0 === $JSCompiler_StaticMethods_addTab$self$$.buttons.length && $button$jscomp$3$$.classList.add("active");
  $JSCompiler_StaticMethods_addTab$self$$.buttons.push($button$jscomp$3$$);
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_addTab$self$$, ".tabpanel-tabs").append($button$jscomp$3$$);
}
function $JSCompiler_StaticMethods_setCurrentTab$$($JSCompiler_StaticMethods_setCurrentTab$self$$, $idx$jscomp$20$$) {
  $idx$jscomp$20$$ != $JSCompiler_StaticMethods_setCurrentTab$self$$.$v$ && ($JSCompiler_StaticMethods_setCurrentTab$self$$.children[$JSCompiler_StaticMethods_setCurrentTab$self$$.$v$].$componentElement$.style.display = "none", $JSCompiler_StaticMethods_setCurrentTab$self$$.buttons[$JSCompiler_StaticMethods_setCurrentTab$self$$.$v$].classList.remove("active"), $JSCompiler_StaticMethods_setCurrentTab$self$$.$v$ = $idx$jscomp$20$$, $JSCompiler_StaticMethods_setCurrentTab$self$$.children[$JSCompiler_StaticMethods_setCurrentTab$self$$.$v$].$componentElement$.style.display = 
  "block", $JSCompiler_StaticMethods_setCurrentTab$self$$.buttons[$JSCompiler_StaticMethods_setCurrentTab$self$$.$v$].classList.add("active"));
}
;var $module$build$ts$ui$component$BagComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$BagComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$BagComponent$default$$.$BagComponent$ = void 0;
$module$build$ts$ui$component$BagComponent$default$$.$BagComponent$ = function($$jscomp$super$this$jscomp$30_object$jscomp$10$$, $slot$jscomp$8$$, $sizeX$jscomp$2$$, $sizeY$jscomp$2$$, $quickPickup$jscomp$2$$, $defaultImage$jscomp$2$$) {
  $$jscomp$super$this$jscomp$30_object$jscomp$10$$ = $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.call(this, $$jscomp$super$this$jscomp$30_object$jscomp$10$$, $slot$jscomp$8$$, $sizeX$jscomp$2$$, $sizeY$jscomp$2$$, $quickPickup$jscomp$2$$, $defaultImage$jscomp$2$$) || this;
  $$jscomp$super$this$jscomp$30_object$jscomp$10$$.$C$ = "3 4";
  return $$jscomp$super$this$jscomp$30_object$jscomp$10$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$BagComponent$default$$.$BagComponent$, $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$);
$module$build$ts$ui$component$BagComponent$default$$.$BagComponent$.counter = $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.counter;
$module$build$ts$ui$component$BagComponent$default$$.$BagComponent$.prototype.update = function() {
  var $features$jscomp$2_size$jscomp$30_sizeArray$$ = null;
  null != $marauroa$$.$me$ && ($features$jscomp$2_size$jscomp$30_sizeArray$$ = $marauroa$$.$me$.features);
  null != $features$jscomp$2_size$jscomp$30_sizeArray$$ && (($features$jscomp$2_size$jscomp$30_sizeArray$$ = $features$jscomp$2_size$jscomp$30_sizeArray$$.bag) && this.$C$ != $features$jscomp$2_size$jscomp$30_sizeArray$$ && (this.$C$ = $features$jscomp$2_size$jscomp$30_sizeArray$$, $features$jscomp$2_size$jscomp$30_sizeArray$$ = $features$jscomp$2_size$jscomp$30_sizeArray$$.split(" "), this.resize(parseInt($features$jscomp$2_size$jscomp$30_sizeArray$$[0], 10), parseInt($features$jscomp$2_size$jscomp$30_sizeArray$$[1], 
  10))), $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.update.call(this));
};
$module$build$ts$ui$component$BagComponent$default$$.$BagComponent$.prototype.resize = function($sizeX$jscomp$4$$, $sizeY$jscomp$4$$) {
  $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.$A$.call(this, $sizeX$jscomp$4$$, $sizeY$jscomp$4$$);
  this.$v$.$init$($sizeX$jscomp$4$$ * $sizeY$jscomp$4$$);
};
var $module$build$ts$ui$component$BuddyListComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$BuddyListComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$ = void 0;
$module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$ = function() {
  var $$jscomp$super$this$jscomp$31$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "buddyList") || this;
  $$jscomp$super$this$jscomp$31$$.$buddies$ = [];
  $$jscomp$super$this$jscomp$31$$.$componentElement$.addEventListener("mouseup", function($event$jscomp$57$$) {
    $$jscomp$super$this$jscomp$31$$.$onMouseUp$($event$jscomp$57$$);
  });
  $$jscomp$super$this$jscomp$31$$.$componentElement$.addEventListener("contextmenu", function($event$jscomp$58$$) {
    $event$jscomp$58$$.preventDefault();
  });
  return $$jscomp$super$this$jscomp$31$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$.prototype.update = function() {
  var $data$jscomp$86_html$jscomp$4$$ = $marauroa$$.$me$.buddies, $buddies$$ = [], $buddy_i$jscomp$82$$;
  for ($buddy_i$jscomp$82$$ in $data$jscomp$86_html$jscomp$4$$) {
    if ($data$jscomp$86_html$jscomp$4$$.hasOwnProperty($buddy_i$jscomp$82$$)) {
      var $entry$jscomp$12$$ = {name:$buddy_i$jscomp$82$$};
      "true" == $data$jscomp$86_html$jscomp$4$$[$buddy_i$jscomp$82$$] ? ($entry$jscomp$12$$.$isOnline$ = !0, $entry$jscomp$12$$.status = "online") : ($entry$jscomp$12$$.$isOnline$ = !1, $entry$jscomp$12$$.status = "offline");
      $buddies$$.push($entry$jscomp$12$$);
    }
  }
  this.sort($buddies$$);
  $data$jscomp$86_html$jscomp$4$$ = "";
  for ($buddy_i$jscomp$82$$ = 0; $buddy_i$jscomp$82$$ < $buddies$$.length; $buddy_i$jscomp$82$$++) {
    $data$jscomp$86_html$jscomp$4$$ += "<li class=" + $buddies$$[$buddy_i$jscomp$82$$].status + '><img src="', $data$jscomp$86_html$jscomp$4$$ = "online" == $buddies$$[$buddy_i$jscomp$82$$].status ? $data$jscomp$86_html$jscomp$4$$ + ($stendhal$$.$paths$.$gui$ + "/buddy_online.png") : $data$jscomp$86_html$jscomp$4$$ + ($stendhal$$.$paths$.$gui$ + "/buddy_offline.png"), $data$jscomp$86_html$jscomp$4$$ += '"> ' + $JSCompiler_StaticMethods_esc$$($buddies$$[$buddy_i$jscomp$82$$].name) + "</li>";
  }
  this.$v$ !== $data$jscomp$86_html$jscomp$4$$ && (this.$v$ = $JSCompiler_StaticMethods_child$$(this, "#buddyListUL").innerHTML = $data$jscomp$86_html$jscomp$4$$);
  this.$buddies$ = $buddies$$;
};
$module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$.prototype.sort = function($buddies$jscomp$1$$) {
  $buddies$jscomp$1$$.sort(function($a$jscomp$38$$, $b$jscomp$38$$) {
    if ($a$jscomp$38$$.$isOnline$) {
      if (!$b$jscomp$38$$.$isOnline$) {
        return -1;
      }
    } else {
      if ($b$jscomp$38$$.$isOnline$) {
        return 1;
      }
    }
    return $a$jscomp$38$$.name < $b$jscomp$38$$.name ? -1 : $a$jscomp$38$$.name > $b$jscomp$38$$.name ? 1 : 0;
  });
};
$module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$.prototype.$buildActions$ = function($actions$jscomp$1$$) {
  this.current && ("online" === this.current.className ? ($actions$jscomp$1$$.push({title:"Talk", action:function($buddyListComponent$$) {
    $JSCompiler_StaticMethods_setText$$($module$build$ts$ui$UI$default$$.$ui$.get(103), "/msg " + $buddyListComponent$$.current.textContent.trim() + " ");
  }}), $actions$jscomp$1$$.push({title:"Where", action:function($buddyListComponent$jscomp$1$$) {
    $marauroa$$.$clientFramework$.$sendAction$({type:"where", target:$buddyListComponent$jscomp$1$$.current.textContent.trim(), zone:$marauroa$$.$currentZoneName$});
  }})) : $actions$jscomp$1$$.push({title:"Leave Message", action:function($buddyListComponent$jscomp$2$$) {
    $JSCompiler_StaticMethods_setText$$($module$build$ts$ui$UI$default$$.$ui$.get(103), "/storemessage " + $buddyListComponent$jscomp$2$$.current.textContent.trim() + " ");
  }}), $actions$jscomp$1$$.push({title:"Remove", action:function($buddyListComponent$jscomp$3$$) {
    $marauroa$$.$clientFramework$.$sendAction$({type:"removebuddy", target:$buddyListComponent$jscomp$3$$.current.textContent.trim(), zone:$marauroa$$.$currentZoneName$});
    for (var $buddy$jscomp$inline_631$$ = $buddyListComponent$jscomp$3$$.current.textContent.trim(), $arrayLength$jscomp$inline_632$$ = $buddyListComponent$jscomp$3$$.$buddies$.length, $i$jscomp$inline_633$$ = 0; $i$jscomp$inline_633$$ < $arrayLength$jscomp$inline_632$$; $i$jscomp$inline_633$$++) {
      if ($buddyListComponent$jscomp$3$$.$buddies$[$i$jscomp$inline_633$$].name === $buddy$jscomp$inline_631$$) {
        $buddyListComponent$jscomp$3$$.$buddies$.splice($i$jscomp$inline_633$$, 1);
        break;
      }
    }
  }}));
};
$module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$.prototype.$onMouseUp$ = function($event$jscomp$59$$) {
  this.current = void 0;
  var $target$jscomp$111$$ = $event$jscomp$59$$.target;
  "LI" === $target$jscomp$111$$.tagName ? this.current = $target$jscomp$111$$ : "IMG" === $target$jscomp$111$$.tagName && (this.current = $target$jscomp$111$$.parentElement);
  this.current && $stendhal$$.$ui$.$actionContextMenu$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Action", new $module$build$ts$ui$dialog$ActionContextMenu$default$$.$ActionContextMenu$(this), Math.max(10, $event$jscomp$59$$.pageX - 50), $event$jscomp$59$$.pageY - 5));
};
var $module$build$ts$ui$dialog$ChatOptionsDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$ChatOptionsDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$ChatOptionsDialog$default$$.$ChatOptionsDialog$ = void 0;
$module$build$ts$ui$dialog$ChatOptionsDialog$default$$.$ChatOptionsDialog$ = function() {
  var $$jscomp$super$this$jscomp$32$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "keywordmap-template") || this;
  for (var $custom_options$$ = [], $$jscomp$iter$69_npc_options$$ = $$jscomp$makeIterator$$($stendhal$$.$config$.get("chat.custom_keywords").split(",")), $$jscomp$key$opt_opt$102$$ = $$jscomp$iter$69_npc_options$$.next(); !$$jscomp$key$opt_opt$102$$.done; $$jscomp$key$opt_opt$102$$ = $$jscomp$iter$69_npc_options$$.next()) {
    var $not_attending_opt$jscomp$1$$ = $$jscomp$key$opt_opt$102$$.value;
    ($not_attending_opt$jscomp$1$$ = $not_attending_opt$jscomp$1$$.trim()) && $custom_options$$.push($not_attending_opt$jscomp$1$$);
  }
  $$jscomp$iter$69_npc_options$$ = [];
  $not_attending_opt$jscomp$1$$ = 1 == $module$build$ts$util$Chat$default$$.$Chat$.$A$.length && "hello" === $module$build$ts$util$Chat$default$$.$Chat$.$A$[0].toLowerCase();
  var $$jscomp$iter$70$$ = $$jscomp$makeIterator$$($module$build$ts$util$Chat$default$$.$Chat$.$A$);
  for ($$jscomp$key$opt_opt$102$$ = $$jscomp$iter$70$$.next(); !$$jscomp$key$opt_opt$102$$.done; $$jscomp$key$opt_opt$102$$ = $$jscomp$iter$70$$.next()) {
    $$jscomp$key$opt_opt$102$$ = $$jscomp$key$opt_opt$102$$.value;
    $$jscomp$key$opt_opt$102$$ = $$jscomp$key$opt_opt$102$$.toLowerCase();
    var $original$$ = $module$build$ts$ui$dialog$ChatOptionsDialog$default$$.$ChatOptionsDialog$.$v$[$$jscomp$key$opt_opt$102$$];
    $original$$ && ($$jscomp$key$opt_opt$102$$ = $original$$);
    -1 == $custom_options$$.indexOf($$jscomp$key$opt_opt$102$$) && $$jscomp$iter$69_npc_options$$.push($$jscomp$key$opt_opt$102$$);
  }
  0 < $$jscomp$iter$69_npc_options$$.length && $JSCompiler_StaticMethods_addGroup$$($$jscomp$super$this$jscomp$32$$, $not_attending_opt$jscomp$1$$ || !$module$build$ts$util$Chat$default$$.$Chat$.$C$ ? void 0 : $module$build$ts$util$Chat$default$$.$Chat$.$C$, $$jscomp$iter$69_npc_options$$);
  0 < $custom_options$$.length && $JSCompiler_StaticMethods_addGroup$$($$jscomp$super$this$jscomp$32$$, "Custom", $custom_options$$);
  return $$jscomp$super$this$jscomp$32$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$ChatOptionsDialog$default$$.$ChatOptionsDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
function $JSCompiler_StaticMethods_addGroup$$($JSCompiler_StaticMethods_addGroup$self$$, $row$jscomp$3_title$jscomp$21$$, $options$jscomp$49$$) {
  if ($row$jscomp$3_title$jscomp$21$$) {
    var $$jscomp$loop$117_titleElement$$ = document.createElement("div");
    $$jscomp$loop$117_titleElement$$.innerText = $row$jscomp$3_title$jscomp$21$$;
    $JSCompiler_StaticMethods_addGroup$self$$.$componentElement$.appendChild($$jscomp$loop$117_titleElement$$);
  }
  $row$jscomp$3_title$jscomp$21$$ = document.createElement("div");
  $JSCompiler_StaticMethods_addGroup$self$$.$componentElement$.appendChild($row$jscomp$3_title$jscomp$21$$);
  $$jscomp$loop$117_titleElement$$ = {};
  for (var $idx$jscomp$21$$ = 0; $idx$jscomp$21$$ < $options$jscomp$49$$.length; $$jscomp$loop$117_titleElement$$ = {$$jscomp$loop$prop$keyword$118$:$$jscomp$loop$117_titleElement$$.$$jscomp$loop$prop$keyword$118$}, $idx$jscomp$21$$++) {
    0 < $idx$jscomp$21$$ && 0 == $idx$jscomp$21$$ % 13 && ($row$jscomp$3_title$jscomp$21$$ = document.createElement("div"), $JSCompiler_StaticMethods_addGroup$self$$.$componentElement$.appendChild($row$jscomp$3_title$jscomp$21$$));
    $$jscomp$loop$117_titleElement$$.$$jscomp$loop$prop$keyword$118$ = $options$jscomp$49$$[$idx$jscomp$21$$];
    var $button$jscomp$4$$ = document.createElement("button");
    $button$jscomp$4$$.className = "shortcut-button";
    $button$jscomp$4$$.innerText = $$jscomp$loop$117_titleElement$$.$$jscomp$loop$prop$keyword$118$;
    $button$jscomp$4$$.addEventListener("click", function($$jscomp$loop$117$jscomp$1$$) {
      return function() {
        $marauroa$$.$clientFramework$.$sendAction$({type:"chat", text:$$jscomp$loop$117$jscomp$1$$.$$jscomp$loop$prop$keyword$118$});
        $JSCompiler_StaticMethods_addGroup$self$$.close();
      };
    }($$jscomp$loop$117_titleElement$$));
    $row$jscomp$3_title$jscomp$21$$.appendChild($button$jscomp$4$$);
  }
}
$module$build$ts$ui$dialog$ChatOptionsDialog$default$$.$ChatOptionsDialog$.$v$ = {bye:"goodbye", hi:"hello",};
var $module$build$ts$ui$dialog$EmojiMapDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$EmojiMapDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$EmojiMapDialog$default$$.$EmojiMapDialog$ = void 0;
$module$build$ts$ui$dialog$EmojiMapDialog$default$$.$EmojiMapDialog$ = function() {
  var $$jscomp$super$this$jscomp$33$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "emojimap-template") || this;
  var $$jscomp$iter$71_emojiStore$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$B$(), $idx$jscomp$22$$ = 0, $row$jscomp$4$$ = document.createElement("div");
  $$jscomp$super$this$jscomp$33$$.$componentElement$.appendChild($row$jscomp$4$$);
  var $$jscomp$loop$119$$ = {};
  $$jscomp$iter$71_emojiStore$$ = $$jscomp$makeIterator$$([].concat($$jscomp$arrayFromIterable$$($$jscomp$iter$71_emojiStore$$.$emojilist$)));
  for (var $$jscomp$key$emoji_button$jscomp$5$$ = $$jscomp$iter$71_emojiStore$$.next(); !$$jscomp$key$emoji_button$jscomp$5$$.done; $$jscomp$loop$119$$ = {$$jscomp$loop$prop$emoji$120$:$$jscomp$loop$119$$.$$jscomp$loop$prop$emoji$120$}, $$jscomp$key$emoji_button$jscomp$5$$ = $$jscomp$iter$71_emojiStore$$.next()) {
    $$jscomp$loop$119$$.$$jscomp$loop$prop$emoji$120$ = $$jscomp$key$emoji_button$jscomp$5$$.value, 0 < $idx$jscomp$22$$ && 0 == $idx$jscomp$22$$ % 13 && ($row$jscomp$4$$ = document.createElement("div"), $$jscomp$super$this$jscomp$33$$.$componentElement$.appendChild($row$jscomp$4$$)), $$jscomp$key$emoji_button$jscomp$5$$ = document.createElement("button"), $$jscomp$key$emoji_button$jscomp$5$$.className = "shortcut-button", $$jscomp$key$emoji_button$jscomp$5$$.appendChild($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + 
    "/emoji/" + $$jscomp$loop$119$$.$$jscomp$loop$prop$emoji$120$ + ".png").cloneNode()), $$jscomp$key$emoji_button$jscomp$5$$.addEventListener("click", function($$jscomp$loop$119$jscomp$1$$) {
      return function() {
        $marauroa$$.$clientFramework$.$sendAction$({type:"chat", text:":" + $$jscomp$loop$119$jscomp$1$$.$$jscomp$loop$prop$emoji$120$ + ":"});
        $$jscomp$super$this$jscomp$33$$.close();
      };
    }($$jscomp$loop$119$$)), $row$jscomp$4$$.appendChild($$jscomp$key$emoji_button$jscomp$5$$), $idx$jscomp$22$$++;
  }
  return $$jscomp$super$this$jscomp$33$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$EmojiMapDialog$default$$.$EmojiMapDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
var $module$build$ts$ui$component$ChatInputComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$ChatInputComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$ChatInputComponent$default$$.$ChatInputComponent$ = void 0;
var $config$$module$build$ts$ui$component$ChatInputComponent$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$A$(), $slashActions$$module$build$ts$ui$component$ChatInputComponent$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$G$();
$module$build$ts$ui$component$ChatInputComponent$default$$.$ChatInputComponent$ = function() {
  var $$jscomp$super$this$jscomp$34$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "chatinput") || this;
  $$jscomp$super$this$jscomp$34$$.$B$ = $$jscomp$super$this$jscomp$34$$.$componentElement$;
  $$jscomp$super$this$jscomp$34$$.$componentElement$.addEventListener("keydown", function($event$jscomp$60$$) {
    $$jscomp$super$this$jscomp$34$$.$onKeyDown$($event$jscomp$60$$);
  });
  $$jscomp$super$this$jscomp$34$$.$componentElement$.addEventListener("keypress", function($event$jscomp$61$$) {
    13 === $event$jscomp$61$$.keyCode && ($$jscomp$super$this$jscomp$34$$.send(), $event$jscomp$61$$.preventDefault());
  });
  $$jscomp$super$this$jscomp$34$$.$v$ = $JSCompiler_StaticMethods_getObject$$() || [];
  $$jscomp$super$this$jscomp$34$$.$A$ = $JSCompiler_StaticMethods_getInt$$($config$$module$build$ts$ui$component$ChatInputComponent$$, "chat.history.index", 0);
  document.getElementById("send-button").addEventListener("click", function() {
    $$jscomp$super$this$jscomp$34$$.send();
  });
  document.getElementById("keywords-button").addEventListener("click", function() {
    var $dialog$jscomp$inline_647_wstate$jscomp$inline_645$$ = $JSCompiler_StaticMethods_getWindowState$$("shortcuts"), $content$jscomp$inline_646$$ = new $module$build$ts$ui$dialog$ChatOptionsDialog$default$$.$ChatOptionsDialog$();
    $dialog$jscomp$inline_647_wstate$jscomp$inline_645$$ = $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Chat Options", $content$jscomp$inline_646$$, $dialog$jscomp$inline_647_wstate$jscomp$inline_645$$.x, $dialog$jscomp$inline_647_wstate$jscomp$inline_645$$.y);
    $dialog$jscomp$inline_647_wstate$jscomp$inline_645$$.$windowId$ = "shortcuts";
    $content$jscomp$inline_646$$.frame = $dialog$jscomp$inline_647_wstate$jscomp$inline_645$$;
  });
  var $btn_emoji$$ = document.getElementById("emojis-button");
  $btn_emoji$$.innerText = "";
  $btn_emoji$$.appendChild($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/emoji/smile.png").cloneNode());
  $btn_emoji$$.addEventListener("click", function() {
    var $dialog$jscomp$inline_652_wstate$jscomp$inline_650$$ = $JSCompiler_StaticMethods_getWindowState$$("shortcuts"), $content$jscomp$inline_651$$ = new $module$build$ts$ui$dialog$EmojiMapDialog$default$$.$EmojiMapDialog$();
    $dialog$jscomp$inline_652_wstate$jscomp$inline_650$$ = $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Emojis", $content$jscomp$inline_651$$, $dialog$jscomp$inline_652_wstate$jscomp$inline_650$$.x, $dialog$jscomp$inline_652_wstate$jscomp$inline_650$$.y);
    $dialog$jscomp$inline_652_wstate$jscomp$inline_650$$.$windowId$ = "shortcuts";
    $content$jscomp$inline_651$$.frame = $dialog$jscomp$inline_652_wstate$jscomp$inline_650$$;
  });
  return $$jscomp$super$this$jscomp$34$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$ChatInputComponent$default$$.$ChatInputComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$ChatInputComponent$default$$.$ChatInputComponent$.prototype.clear = function() {
  this.$B$.value = "";
};
function $JSCompiler_StaticMethods_setText$$($JSCompiler_StaticMethods_setText$self$$, $text$jscomp$19$$) {
  $JSCompiler_StaticMethods_setText$self$$.$B$.value = $text$jscomp$19$$;
  $JSCompiler_StaticMethods_setText$self$$.$B$.focus();
}
function $JSCompiler_StaticMethods_fromHistory$$($JSCompiler_StaticMethods_fromHistory$self$$, $i$jscomp$85$$) {
  $JSCompiler_StaticMethods_fromHistory$self$$.$A$ += $i$jscomp$85$$;
  0 > $JSCompiler_StaticMethods_fromHistory$self$$.$A$ && ($JSCompiler_StaticMethods_fromHistory$self$$.$A$ = 0);
  $JSCompiler_StaticMethods_fromHistory$self$$.$A$ >= $JSCompiler_StaticMethods_fromHistory$self$$.$v$.length ? ($JSCompiler_StaticMethods_fromHistory$self$$.$A$ = $JSCompiler_StaticMethods_fromHistory$self$$.$v$.length, $JSCompiler_StaticMethods_fromHistory$self$$.clear()) : $JSCompiler_StaticMethods_fromHistory$self$$.$B$.value = $JSCompiler_StaticMethods_fromHistory$self$$.$v$[$JSCompiler_StaticMethods_fromHistory$self$$.$A$];
}
$module$build$ts$ui$component$ChatInputComponent$default$$.$ChatInputComponent$.prototype.$onKeyDown$ = function($event$jscomp$62$$) {
  var $code$jscomp$5$$ = $event$jscomp$62$$.which ? $event$jscomp$62$$.which : $event$jscomp$62$$.keyCode;
  $event$jscomp$62$$.shiftKey && ($code$jscomp$5$$ === $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$.$up$ ? ($event$jscomp$62$$.preventDefault(), $JSCompiler_StaticMethods_fromHistory$$(this, -1)) : $code$jscomp$5$$ === $module$build$ts$ui$KeyHandler$default$$.$KeyHandler$.$v$.$down$ && ($event$jscomp$62$$.preventDefault(), $JSCompiler_StaticMethods_fromHistory$$(this, 1)));
};
$module$build$ts$ui$component$ChatInputComponent$default$$.$ChatInputComponent$.prototype.send = function() {
  var $val$$ = this.$B$.value, $array$jscomp$11$$ = $val$$.split(" ");
  "/choosecharacter" === $array$jscomp$11$$[0] ? $marauroa$$.$clientFramework$.$chooseCharacter$($array$jscomp$11$$[1]) : "/close" === $val$$ ? $marauroa$$.$clientFramework$.close() : $slashActions$$module$build$ts$ui$component$ChatInputComponent$$.$execute$($val$$) && (100 < this.$v$.length && this.$v$.shift(), this.$v$[this.$v$.length] = $val$$, this.$A$ = this.$v$.length, $config$$module$build$ts$ui$component$ChatInputComponent$$.set("chat.history", this.$v$), $config$$module$build$ts$ui$component$ChatInputComponent$$.set("chat.history.index", 
  this.$A$));
  this.clear();
};
var $module$build$ts$ui$component$ChatLogComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$ChatLogComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$ChatLogComponent$default$$.$ChatLogComponent$ = void 0;
$module$build$ts$ui$component$ChatLogComponent$default$$.$ChatLogComponent$ = function() {
  var $$jscomp$super$this$jscomp$35$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "chat") || this;
  $$jscomp$super$this$jscomp$35$$.refresh();
  $$jscomp$super$this$jscomp$35$$.$componentElement$.addEventListener("mouseup", function($evt$jscomp$45$$) {
    $JSCompiler_StaticMethods_JSC$2136_onContextMenu$$($evt$jscomp$45$$);
  });
  return $$jscomp$super$this$jscomp$35$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$ChatLogComponent$default$$.$ChatLogComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$ChatLogComponent$default$$.$ChatLogComponent$.prototype.refresh = function() {
  this.$componentElement$.style.setProperty("font-family", $stendhal$$.$config$.get("ui.font.chat"));
};
function $JSCompiler_StaticMethods_createTimestamp$$() {
  var $date$jscomp$3_timestamp$jscomp$2$$ = new Date(), $time$jscomp$2$$ = "" + $date$jscomp$3_timestamp$jscomp$2$$.getHours() + ":";
  10 > $date$jscomp$3_timestamp$jscomp$2$$.getHours() && ($time$jscomp$2$$ = "0" + $time$jscomp$2$$);
  10 > $date$jscomp$3_timestamp$jscomp$2$$.getMinutes() && ($time$jscomp$2$$ += "0");
  $time$jscomp$2$$ += $date$jscomp$3_timestamp$jscomp$2$$.getMinutes();
  $date$jscomp$3_timestamp$jscomp$2$$ = document.createElement("span");
  $date$jscomp$3_timestamp$jscomp$2$$.className = "logtimestamp";
  $date$jscomp$3_timestamp$jscomp$2$$.innerHTML = "[" + $time$jscomp$2$$ + "]";
  return $date$jscomp$3_timestamp$jscomp$2$$;
}
$module$build$ts$ui$component$ChatLogComponent$default$$.$ChatLogComponent$.prototype.add = function($row$jscomp$5$$) {
  var $chatElement$$ = this.$componentElement$, $isAtBottom$$ = $chatElement$$.scrollHeight - $chatElement$$.clientHeight <= $chatElement$$.scrollTop + 5;
  $chatElement$$.appendChild($row$jscomp$5$$);
  $isAtBottom$$ && ($chatElement$$.scrollTop = $chatElement$$.scrollHeight);
};
function $JSCompiler_StaticMethods_addLine$$($JSCompiler_StaticMethods_addLine$self$$, $res$jscomp$inline_671_type$jscomp$212$$, $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$, $lcol_orator$jscomp$2$$, $rcol_timestamp$jscomp$3$$) {
  $lcol_orator$jscomp$2$$ && ($JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$ = $lcol_orator$jscomp$2$$ + ": " + $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$);
  $lcol_orator$jscomp$2$$ = document.createElement("div");
  $lcol_orator$jscomp$2$$.className = "logcolL";
  void 0 === $rcol_timestamp$jscomp$3$$ || $rcol_timestamp$jscomp$3$$ ? $lcol_orator$jscomp$2$$.appendChild($JSCompiler_StaticMethods_createTimestamp$$()) : $lcol_orator$jscomp$2$$.innerHTML = " ";
  $rcol_timestamp$jscomp$3$$ = document.createElement("div");
  $rcol_timestamp$jscomp$3$$.className = "logcolR log" + $res$jscomp$inline_671_type$jscomp$212$$;
  if ($JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$) {
    $res$jscomp$inline_671_type$jscomp$212$$ = "";
    for (var $delims$jscomp$inline_672$$ = " ,.!?:;".split(""), $length$jscomp$inline_673$$ = $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$.length, $inHighlight$jscomp$inline_674$$ = !1, $inUnderline$jscomp$inline_675$$ = !1, $inHighlightQuote$jscomp$inline_676$$ = !1, $inUnderlineQuote$jscomp$inline_677$$ = !1, $i$jscomp$inline_678$$ = 0; $i$jscomp$inline_678$$ < $length$jscomp$inline_673$$; $i$jscomp$inline_678$$++) {
      var $c$jscomp$inline_679$$ = $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$[$i$jscomp$inline_678$$];
      if ("\\" === $c$jscomp$inline_679$$) {
        $res$jscomp$inline_671_type$jscomp$212$$ += $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$[$i$jscomp$inline_678$$ + 1], $i$jscomp$inline_678$$++;
      } else {
        if ("#" === $c$jscomp$inline_679$$) {
          if ($inHighlight$jscomp$inline_674$$) {
            $res$jscomp$inline_671_type$jscomp$212$$ += $c$jscomp$inline_679$$;
          } else {
            var $n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ = $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$[$i$jscomp$inline_678$$ + 1];
            "#" === $n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ ? ($res$jscomp$inline_671_type$jscomp$212$$ += $c$jscomp$inline_679$$, $i$jscomp$inline_678$$++) : ("'" === $n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ && ($inHighlightQuote$jscomp$inline_676$$ = !0, $i$jscomp$inline_678$$++), $inHighlight$jscomp$inline_674$$ = !0, $res$jscomp$inline_671_type$jscomp$212$$ += '<span class="logh">');
          }
        } else {
          if ("\u00a7" === $c$jscomp$inline_679$$) {
            $inUnderline$jscomp$inline_675$$ ? $res$jscomp$inline_671_type$jscomp$212$$ += $c$jscomp$inline_679$$ : ($n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ = $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$[$i$jscomp$inline_678$$ + 1], "\u00a7" === $n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ ? ($res$jscomp$inline_671_type$jscomp$212$$ += $c$jscomp$inline_679$$, $i$jscomp$inline_678$$++) : ("'" === $n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ && 
            ($inUnderlineQuote$jscomp$inline_677$$ = !0, $i$jscomp$inline_678$$++), $inUnderline$jscomp$inline_675$$ = !0, $res$jscomp$inline_671_type$jscomp$212$$ += '<span class="logi">'));
          } else {
            if ("'" === $c$jscomp$inline_679$$) {
              $inUnderlineQuote$jscomp$inline_677$$ ? ($inUnderlineQuote$jscomp$inline_677$$ = $inUnderline$jscomp$inline_675$$ = !1, $res$jscomp$inline_671_type$jscomp$212$$ += "</span>") : $inHighlightQuote$jscomp$inline_676$$ ? ($inHighlightQuote$jscomp$inline_676$$ = $inHighlight$jscomp$inline_674$$ = !1, $res$jscomp$inline_671_type$jscomp$212$$ += "</span>") : $res$jscomp$inline_671_type$jscomp$212$$ += $c$jscomp$inline_679$$;
            } else {
              if ("<" === $c$jscomp$inline_679$$) {
                $res$jscomp$inline_671_type$jscomp$212$$ += "&lt;";
              } else {
                if (-1 < $delims$jscomp$inline_672$$.indexOf($c$jscomp$inline_679$$) && ($n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ = $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$[$i$jscomp$inline_678$$ + 1], " " === $c$jscomp$inline_679$$ || " " === $n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$ || void 0 == $n$103$jscomp$inline_680_n$104$jscomp$inline_681_n$105$jscomp$inline_682$$)) {
                  if ($inUnderline$jscomp$inline_675$$ && !$inUnderlineQuote$jscomp$inline_677$$ && !$inHighlightQuote$jscomp$inline_676$$) {
                    $inUnderline$jscomp$inline_675$$ = !1;
                    $res$jscomp$inline_671_type$jscomp$212$$ += "</span>" + $c$jscomp$inline_679$$;
                    continue;
                  }
                  if ($inHighlight$jscomp$inline_674$$ && !$inUnderlineQuote$jscomp$inline_677$$ && !$inHighlightQuote$jscomp$inline_676$$) {
                    $inHighlight$jscomp$inline_674$$ = !1;
                    $res$jscomp$inline_671_type$jscomp$212$$ += "</span>" + $c$jscomp$inline_679$$;
                    continue;
                  }
                }
                $res$jscomp$inline_671_type$jscomp$212$$ += $c$jscomp$inline_679$$;
              }
            }
          }
        }
      }
    }
    $inUnderline$jscomp$inline_675$$ && ($res$jscomp$inline_671_type$jscomp$212$$ += "</span>");
    $inHighlight$jscomp$inline_674$$ && ($res$jscomp$inline_671_type$jscomp$212$$ += "</span>");
    $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$ = $res$jscomp$inline_671_type$jscomp$212$$;
  } else {
    $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$ = "";
  }
  $rcol_timestamp$jscomp$3$$.innerHTML += $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$;
  $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$ = document.createElement("div");
  $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$.className = "logrow";
  $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$.appendChild($lcol_orator$jscomp$2$$);
  $JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$.appendChild($rcol_timestamp$jscomp$3$$);
  $JSCompiler_StaticMethods_addLine$self$$.add($JSCompiler_inline_result$jscomp$197_message$jscomp$33_row$jscomp$6$$);
}
$module$build$ts$ui$component$ChatLogComponent$default$$.$ChatLogComponent$.prototype.clear = function() {
  this.$componentElement$.innerHTML = "";
};
function $JSCompiler_StaticMethods_JSC$2136_onContextMenu$$($evt$jscomp$46$$) {
  if ($evt$jscomp$46$$ && 2 == $evt$jscomp$46$$.button && !$stendhal$$.$ui$.$actionContextMenu$.$isOpen$()) {
    var $log$$ = $module$build$ts$ui$UI$default$$.$ui$.get(104), $options$jscomp$50$$ = [{title:"Clear", action:function() {
      $log$$.clear();
    }}];
    navigator && navigator.clipboard && $options$jscomp$50$$.unshift({title:"Copy", action:function() {
      var $clipboard$jscomp$inline_685$$ = void 0 === $clipboard$jscomp$inline_685$$ ? !0 : $clipboard$jscomp$inline_685$$;
      if (!$clipboard$jscomp$inline_685$$ || navigator && navigator.clipboard) {
        var $lines$jscomp$inline_686$$ = [];
        if (window.getSelection) {
          var $children$jscomp$inline_689_sel$jscomp$inline_687$$ = window.getSelection(), $idx$jscomp$inline_690_value$jscomp$inline_688$$;
          $children$jscomp$inline_689_sel$jscomp$inline_687$$ && "Range" == $children$jscomp$inline_689_sel$jscomp$inline_687$$.type && 1 == $children$jscomp$inline_689_sel$jscomp$inline_687$$.rangeCount && ($idx$jscomp$inline_690_value$jscomp$inline_688$$ = $children$jscomp$inline_689_sel$jscomp$inline_687$$.toString());
          $idx$jscomp$inline_690_value$jscomp$inline_688$$ && "" !== $idx$jscomp$inline_690_value$jscomp$inline_688$$ && $lines$jscomp$inline_686$$.push($idx$jscomp$inline_690_value$jscomp$inline_688$$);
        }
        if (0 == $lines$jscomp$inline_686$$.length) {
          for ($children$jscomp$inline_689_sel$jscomp$inline_687$$ = $log$$.$componentElement$.children, $idx$jscomp$inline_690_value$jscomp$inline_688$$ = 0; $idx$jscomp$inline_690_value$jscomp$inline_688$$ < $children$jscomp$inline_689_sel$jscomp$inline_687$$.length; $idx$jscomp$inline_690_value$jscomp$inline_688$$++) {
            var $msg$jscomp$inline_1077_row$jscomp$inline_691$$ = $children$jscomp$inline_689_sel$jscomp$inline_687$$[$idx$jscomp$inline_690_value$jscomp$inline_688$$], $$jscomp$inline_1079_text$jscomp$inline_692$$ = $msg$jscomp$inline_1077_row$jscomp$inline_691$$.children[0].innerHTML.trim() + " ";
            "" === $$jscomp$inline_1079_text$jscomp$inline_692$$.trim() && ($$jscomp$inline_1079_text$jscomp$inline_692$$ = "    ");
            $msg$jscomp$inline_1077_row$jscomp$inline_691$$ = $$jscomp$inline_1079_text$jscomp$inline_692$$ + $msg$jscomp$inline_1077_row$jscomp$inline_691$$.children[1].innerHTML;
            $$jscomp$inline_1079_text$jscomp$inline_692$$ = $$jscomp$makeIterator$$(["span", "div"]);
            for (var $$jscomp$key$tag$jscomp$inline_1080_tag$jscomp$inline_1081$$ = $$jscomp$inline_1079_text$jscomp$inline_692$$.next(); !$$jscomp$key$tag$jscomp$inline_1080_tag$jscomp$inline_1081$$.done; $$jscomp$key$tag$jscomp$inline_1080_tag$jscomp$inline_1081$$ = $$jscomp$inline_1079_text$jscomp$inline_692$$.next()) {
              $$jscomp$key$tag$jscomp$inline_1080_tag$jscomp$inline_1081$$ = $$jscomp$key$tag$jscomp$inline_1080_tag$jscomp$inline_1081$$.value, $msg$jscomp$inline_1077_row$jscomp$inline_691$$ = $msg$jscomp$inline_1077_row$jscomp$inline_691$$.replace(new RegExp("<" + $$jscomp$key$tag$jscomp$inline_1080_tag$jscomp$inline_1081$$ + ".*?>", "g"), "").replace(new RegExp("</" + $$jscomp$key$tag$jscomp$inline_1080_tag$jscomp$inline_1081$$ + ">", "g"), "");
            }
            $$jscomp$inline_1079_text$jscomp$inline_692$$ = $msg$jscomp$inline_1077_row$jscomp$inline_691$$;
            $lines$jscomp$inline_686$$.push($$jscomp$inline_1079_text$jscomp$inline_692$$.replace("&lt;", "<").replace("&gt;", ">"));
          }
        }
        0 < $lines$jscomp$inline_686$$.length && $clipboard$jscomp$inline_685$$ && navigator.clipboard.writeText($lines$jscomp$inline_686$$.join("\n"));
      } else {
        console.warn("copying to clipboard not supported by this browser");
      }
    }});
    var $pos$jscomp$14$$ = $JSCompiler_StaticMethods_extractPosition$$($evt$jscomp$46$$);
    $stendhal$$.$ui$.$actionContextMenu$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Action", new $LogContextMenu$$module$build$ts$ui$component$ChatLogComponent$$($options$jscomp$50$$), $pos$jscomp$14$$.pageX - 50, $pos$jscomp$14$$.pageY - 5));
    $evt$jscomp$46$$.preventDefault();
    $evt$jscomp$46$$.stopPropagation();
  }
}
function $LogContextMenu$$module$build$ts$ui$component$ChatLogComponent$$($content$jscomp$12_options$jscomp$51$$) {
  var $$jscomp$super$this$jscomp$36$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "contextmenu-template") || this;
  $$jscomp$super$this$jscomp$36$$.$v$ = $content$jscomp$12_options$jscomp$51$$;
  $content$jscomp$12_options$jscomp$51$$ = '<div class="actionmenu">';
  for (var $i$jscomp$87$$ = 0; $i$jscomp$87$$ < $$jscomp$super$this$jscomp$36$$.$v$.length; $i$jscomp$87$$++) {
    $content$jscomp$12_options$jscomp$51$$ += '<button id="actionbutton.' + $i$jscomp$87$$ + '">' + $JSCompiler_StaticMethods_esc$$($$jscomp$super$this$jscomp$36$$.$v$[$i$jscomp$87$$].title) + "</button><br>";
  }
  $$jscomp$super$this$jscomp$36$$.$componentElement$.innerHTML = $content$jscomp$12_options$jscomp$51$$ + "</div>";
  $$jscomp$super$this$jscomp$36$$.$componentElement$.addEventListener("click", function($evt$jscomp$47_iStr$jscomp$inline_697$$) {
    var $_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$;
    $evt$jscomp$47_iStr$jscomp$inline_697$$ = null === ($_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$ = $evt$jscomp$47_iStr$jscomp$inline_697$$.target.getAttribute("id")) || void 0 === $_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$ ? void 0 : $_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$.substring(13);
    if (void 0 !== $evt$jscomp$47_iStr$jscomp$inline_697$$ && "" !== $evt$jscomp$47_iStr$jscomp$inline_697$$ && ($_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$ = parseInt($evt$jscomp$47_iStr$jscomp$inline_697$$), !(0 > $_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$))) {
      $$jscomp$super$this$jscomp$36$$.$componentElement$.dispatchEvent(new Event("close"));
      if ($_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$ >= $$jscomp$super$this$jscomp$36$$.$v$.length) {
        throw Error("actions index is larger than number of actions");
      }
      ($_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$ = $$jscomp$super$this$jscomp$36$$.$v$[$_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$].action) ? $_a$jscomp$inline_696_action$jscomp$inline_699_i$jscomp$inline_698$$() : console.error("chat log context menu action failed");
    }
  });
  return $$jscomp$super$this$jscomp$36$$;
}
$$jscomp$inherits$$($LogContextMenu$$module$build$ts$ui$component$ChatLogComponent$$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
var $module$build$ts$ui$component$GroupMemberComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$GroupMemberComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$GroupMemberComponent$default$$.$GroupMemberComponent$ = void 0;
$module$build$ts$ui$component$GroupMemberComponent$default$$.$GroupMemberComponent$ = function($memberName$$, $isUserLeader$$) {
  var $$jscomp$super$this$jscomp$37$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "group-member-template") || this;
  $$jscomp$super$this$jscomp$37$$.$v$ = $memberName$$;
  $$jscomp$super$this$jscomp$37$$.$A$ = $isUserLeader$$;
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$37$$, ".group-member-name").innerText = $memberName$$;
  $$jscomp$super$this$jscomp$37$$.$componentElement$.addEventListener("mouseup", function($event$jscomp$64$$) {
    $$jscomp$super$this$jscomp$37$$.$onMouseUp$($event$jscomp$64$$);
  });
  return $$jscomp$super$this$jscomp$37$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$GroupMemberComponent$default$$.$GroupMemberComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$GroupMemberComponent$default$$.$GroupMemberComponent$.prototype.$buildActions$ = function($actions$jscomp$2$$) {
  var $playerName$$ = this.$v$;
  $actions$jscomp$2$$.push({title:"Talk", action:function() {
    $JSCompiler_StaticMethods_setText$$($module$build$ts$ui$UI$default$$.$ui$.get(103), "/msg " + $playerName$$ + " ");
  }});
  $actions$jscomp$2$$.push({title:"Where", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"where", target:$playerName$$, zone:$marauroa$$.$currentZoneName$});
  }});
  !this.$A$ || $marauroa$$.$me$ && this.$v$ == $marauroa$$.$me$._name || ($actions$jscomp$2$$.push({title:"Kick", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"group_management", action:"kick", params:$playerName$$, zone:$marauroa$$.$currentZoneName$});
  }}), $actions$jscomp$2$$.push({title:"Make leader", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"group_management", action:"leader", params:$playerName$$, zone:$marauroa$$.$currentZoneName$});
  }}));
};
$module$build$ts$ui$component$GroupMemberComponent$default$$.$GroupMemberComponent$.prototype.$onMouseUp$ = function($event$jscomp$65$$) {
  $stendhal$$.$ui$.$actionContextMenu$.set($JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Action", new $module$build$ts$ui$dialog$ActionContextMenu$default$$.$ActionContextMenu$(this), Math.max(10, $event$jscomp$65$$.pageX - 50), $event$jscomp$65$$.pageY - 5));
};
var $module$build$ts$ui$component$GroupPanelComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$GroupPanelComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$GroupPanelComponent$default$$.$GroupPanelComponent$ = void 0;
$module$build$ts$ui$component$GroupPanelComponent$default$$.$GroupPanelComponent$ = function() {
  var $$jscomp$super$this$jscomp$38$$ = $module$build$ts$ui$toolkit$Panel$default$$.$Panel$.call(this, "group-panel") || this;
  $$jscomp$super$this$jscomp$38$$.$v$ = {};
  $$jscomp$super$this$jscomp$38$$.$A$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$38$$, ".group-members");
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$38$$, ".group-lootmode").addEventListener("click", function() {
    var $newMode$jscomp$inline_702$$ = "shared";
    "shared" === $stendhal$$.data.group.$lootmode$ && ($newMode$jscomp$inline_702$$ = "single");
    $marauroa$$.$clientFramework$.$sendAction$({type:"group_management", action:"lootmode", params:$newMode$jscomp$inline_702$$, zone:$marauroa$$.$currentZoneName$});
  });
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$38$$, ".group-chat").addEventListener("click", function() {
    $JSCompiler_StaticMethods_isInGroup$$() ? $JSCompiler_StaticMethods_setText$$($module$build$ts$ui$UI$default$$.$ui$.get(103), "/p ") : $module$build$ts$util$Chat$default$$.$Chat$.log("error", "Please invite someone into a group before trying to send group messages.");
  });
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$38$$, ".group-invite").addEventListener("click", function() {
    $JSCompiler_StaticMethods_isInGroup$$() && $stendhal$$.data.group.$leader$ !== $marauroa$$.$me$._name ? $module$build$ts$util$Chat$default$$.$Chat$.log("error", "Only the leader may invite people into the group.") : ($module$build$ts$util$Chat$default$$.$Chat$.log("client", "Please Fill in the name of the player you want to invite"), $JSCompiler_StaticMethods_setText$$($module$build$ts$ui$UI$default$$.$ui$.get(103), "/group invite "));
  });
  $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$38$$, ".group-part").addEventListener("click", function() {
    $JSCompiler_StaticMethods_isInGroup$$() ? $marauroa$$.$clientFramework$.$sendAction$({type:"group_management", action:"part", params:"", zone:$marauroa$$.$currentZoneName$}) : $module$build$ts$util$Chat$default$$.$Chat$.log("error", "You cannot leave a group because your are not a member of a group");
  });
  return $$jscomp$super$this$jscomp$38$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$GroupPanelComponent$default$$.$GroupPanelComponent$, $module$build$ts$ui$toolkit$Panel$default$$.$Panel$);
function $JSCompiler_StaticMethods_receivedInvite$$($JSCompiler_StaticMethods_receivedInvite$self$$, $leader$jscomp$1$$) {
  if (!$JSCompiler_StaticMethods_receivedInvite$self$$.$v$[$leader$jscomp$1$$]) {
    var $button$jscomp$6$$ = document.createElement("button");
    $button$jscomp$6$$.innerText = "Join " + $leader$jscomp$1$$;
    $button$jscomp$6$$.title = "Join the group led by " + $leader$jscomp$1$$;
    $button$jscomp$6$$.addEventListener("click", function() {
      $marauroa$$.$clientFramework$.$sendAction$({type:"group_management", action:"join", params:$leader$jscomp$1$$, zone:$marauroa$$.$currentZoneName$});
    });
    $JSCompiler_StaticMethods_receivedInvite$self$$.$v$[$leader$jscomp$1$$] = $button$jscomp$6$$;
    $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_receivedInvite$self$$, ".group-invites").append($button$jscomp$6$$);
    $module$build$ts$util$Chat$default$$.$Chat$.log("client", "You received an invite to join a group. Please use the group panel to accept the invite.");
    $JSCompiler_StaticMethods_setCurrentTab$$($module$build$ts$ui$UI$default$$.$ui$.get(111), 1);
  }
}
function $JSCompiler_StaticMethods_isInGroup$$() {
  return $stendhal$$.data.group.$members$ && 0 < Object.keys($stendhal$$.data.group.$members$).length;
}
$module$build$ts$ui$component$GroupPanelComponent$default$$.$GroupPanelComponent$.prototype.$updateGroupStatus$ = function() {
  if ($JSCompiler_StaticMethods_isInGroup$$()) {
    this.$v$ = {};
    $JSCompiler_StaticMethods_child$$(this, ".group-invites").innerHTML = "";
    $JSCompiler_StaticMethods_child$$(this, ".group-nogroup").classList.add("hidden");
    $JSCompiler_StaticMethods_child$$(this, ".group-group").classList.remove("hidden");
    $JSCompiler_StaticMethods_child$$(this, ".group-lootmode").innerText = $stendhal$$.data.group.$lootmode$;
    $JSCompiler_StaticMethods_child$$(this, ".group-leader").innerText = $stendhal$$.data.group.$leader$;
    this.clear();
    for (var $$jscomp$inline_714$$ = $$jscomp$makeIterator$$(Object.keys($stendhal$$.data.group.$members$)), $$jscomp$inline_715$$ = $$jscomp$inline_714$$.next(); !$$jscomp$inline_715$$.done; $$jscomp$inline_715$$ = $$jscomp$inline_714$$.next()) {
      this.add(new $module$build$ts$ui$component$GroupMemberComponent$default$$.$GroupMemberComponent$($$jscomp$inline_715$$.value, $stendhal$$.data.group.$leader$ === $marauroa$$.$me$._name));
    }
  } else {
    $JSCompiler_StaticMethods_child$$(this, ".group-nogroup").classList.remove("hidden"), $JSCompiler_StaticMethods_child$$(this, ".group-group").classList.add("hidden");
  }
};
var $module$build$ts$ui$component$KeyringComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$KeyringComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$KeyringComponent$default$$.$KeyringComponent$ = void 0;
$module$build$ts$ui$component$KeyringComponent$default$$.$KeyringComponent$ = function($$jscomp$super$this$jscomp$39_object$jscomp$11$$, $slot$jscomp$9$$, $sizeX$jscomp$5$$, $sizeY$jscomp$5$$, $quickPickup$jscomp$3$$, $defaultImage$jscomp$3$$) {
  $$jscomp$super$this$jscomp$39_object$jscomp$11$$ = $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.call(this, $$jscomp$super$this$jscomp$39_object$jscomp$11$$, $slot$jscomp$9$$, $sizeX$jscomp$5$$, $sizeY$jscomp$5$$, $quickPickup$jscomp$3$$, $defaultImage$jscomp$3$$) || this;
  $$jscomp$super$this$jscomp$39_object$jscomp$11$$.$C$ = "2 4";
  return $$jscomp$super$this$jscomp$39_object$jscomp$11$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$KeyringComponent$default$$.$KeyringComponent$, $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$);
$module$build$ts$ui$component$KeyringComponent$default$$.$KeyringComponent$.counter = $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.counter;
$module$build$ts$ui$component$KeyringComponent$default$$.$KeyringComponent$.prototype.update = function() {
  var $features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$ = null;
  null != $marauroa$$.$me$ && ($features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$ = $marauroa$$.$me$.features);
  null != $features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$ && (($features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$ = $features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$.keyring) ? (this.isVisible() || $JSCompiler_StaticMethods_setVisible$$(this, !0), this.$C$ != $features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$ && (this.$C$ = $features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$, $features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$ = $features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$.split(" "), 
  this.resize(parseInt($features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$[0], 10), parseInt($features$jscomp$3_size$jscomp$31_sizeArray$jscomp$1$$[1], 10)))) : this.isVisible() && $JSCompiler_StaticMethods_setVisible$$(this, !1), $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.update.call(this));
};
$module$build$ts$ui$component$KeyringComponent$default$$.$KeyringComponent$.prototype.resize = function($sizeX$jscomp$7$$, $sizeY$jscomp$7$$) {
  $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$.prototype.$A$.call(this, $sizeX$jscomp$7$$, $sizeY$jscomp$7$$);
  this.$v$.$init$($sizeX$jscomp$7$$ * $sizeY$jscomp$7$$);
};
var $module$build$ts$util$Direction$default$$ = {};
Object.defineProperty($module$build$ts$util$Direction$default$$, "__esModule", {value:!0});
$module$build$ts$util$Direction$default$$.$Direction$ = void 0;
$module$build$ts$util$Direction$default$$.$Direction$ = function($val$jscomp$1$$, $dx$jscomp$4$$, $dy$jscomp$4$$) {
  this.$dx$ = $dx$jscomp$4$$;
  this.$dy$ = $dy$jscomp$4$$;
};
$module$build$ts$util$Direction$default$$.$Direction$.$C$ = new $module$build$ts$util$Direction$default$$.$Direction$(0, 0, 0);
$module$build$ts$util$Direction$default$$.$Direction$.$D$ = new $module$build$ts$util$Direction$default$$.$Direction$(1, 0, -1);
$module$build$ts$util$Direction$default$$.$Direction$.$B$ = new $module$build$ts$util$Direction$default$$.$Direction$(2, 1, 0);
$module$build$ts$util$Direction$default$$.$Direction$.$v$ = new $module$build$ts$util$Direction$default$$.$Direction$(3, 0, 1);
$module$build$ts$util$Direction$default$$.$Direction$.$A$ = new $module$build$ts$util$Direction$default$$.$Direction$(4, -1, 0);
$module$build$ts$util$Direction$default$$.$Direction$.$F$ = [$module$build$ts$util$Direction$default$$.$Direction$.$C$, $module$build$ts$util$Direction$default$$.$Direction$.$D$, $module$build$ts$util$Direction$default$$.$Direction$.$B$, $module$build$ts$util$Direction$default$$.$Direction$.$v$, $module$build$ts$util$Direction$default$$.$Direction$.$A$];
var $module$build$ts$util$MathUtil$default$$ = {};
Object.defineProperty($module$build$ts$util$MathUtil$default$$, "__esModule", {value:!0});
$module$build$ts$util$MathUtil$default$$.$MathUtil$ = void 0;
$module$build$ts$util$MathUtil$default$$.$MathUtil$ = function() {
};
$module$build$ts$util$MathUtil$default$$.$MathUtil$.$v$ = function($d1$$, $d2$$) {
  var $diff$jscomp$3$$ = 1.0;
  $diff$jscomp$3$$ = void 0 === $diff$jscomp$3$$ ? $module$build$ts$util$MathUtil$default$$.$MathUtil$.EPSILON : $diff$jscomp$3$$;
  return Math.abs($d1$$ - $d2$$) < $diff$jscomp$3$$;
};
$module$build$ts$util$MathUtil$default$$.$MathUtil$.EPSILON = 0.001;
var $module$build$ts$entity$ActiveEntity$default$$ = {};
Object.defineProperty($module$build$ts$entity$ActiveEntity$default$$, "__esModule", {value:!0});
$module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$ = void 0;
$module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$ = function() {
  var $$jscomp$super$this$jscomp$40$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$40$$.$K$ = !1;
  $$jscomp$super$this$jscomp$40$$.direction = $module$build$ts$util$Direction$default$$.$Direction$.$v$;
  $$jscomp$super$this$jscomp$40$$.$dx$ = 0.0;
  $$jscomp$super$this$jscomp$40$$.$dy$ = 0.0;
  return $$jscomp$super$this$jscomp$40$$;
};
$$jscomp$inherits$$($module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$.prototype.set = function($key$jscomp$85$$, $value$jscomp$124$$) {
  if (-1 < ["dir", "speed", "x", "y"].indexOf($key$jscomp$85$$)) {
    var $numberValue$$ = parseFloat($value$jscomp$124$$);
    isNaN(this["_" + $key$jscomp$85$$]) && (this["_" + $key$jscomp$85$$] = $numberValue$$);
    $JSCompiler_StaticMethods_processPositioning$$(this, $key$jscomp$85$$, $numberValue$$);
  }
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$85$$, $value$jscomp$124$$);
};
$module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$.prototype.$unset$ = function($key$jscomp$86$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.$unset$.call(this, $key$jscomp$86$$);
  -1 < ["dir", "speed"].indexOf($key$jscomp$86$$) && $JSCompiler_StaticMethods_processPositioning$$(this, $key$jscomp$86$$, 0);
};
$module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$.prototype.$updatePosition$ = function($delta$jscomp$4_oldX$$) {
  if (0.0 != this.$dx$ || 0.0 != this.$dy$) {
    var $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ = $delta$jscomp$4_oldX$$ / 300.0;
    $delta$jscomp$4_oldX$$ = this._x;
    var $oldY$$ = this._y;
    this._x += this.$dx$ * $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$;
    this._y += this.$dy$ * $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$;
    a: {
      if ("undefined" === typeof this.ignore_collision && "undefined" === typeof this.ghostmode) {
        $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ = Math.floor(this._x);
        for (var $endX$jscomp$inline_719_thisStartY$jscomp$inline_726$$ = Math.ceil(this._x + this.width), $endY$jscomp$inline_720_thisEndX$jscomp$inline_727$$ = Math.ceil(this._y + this.height), $thisEndY$jscomp$inline_728_y$jscomp$inline_721$$ = Math.floor(this._y); $thisEndY$jscomp$inline_728_y$jscomp$inline_721$$ < $endY$jscomp$inline_720_thisEndX$jscomp$inline_727$$; $thisEndY$jscomp$inline_728_y$jscomp$inline_721$$++) {
          for (var $other$jscomp$inline_730_x$jscomp$inline_722$$ = $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$; $other$jscomp$inline_730_x$jscomp$inline_722$$ < $endX$jscomp$inline_719_thisStartY$jscomp$inline_726$$; $other$jscomp$inline_730_x$jscomp$inline_722$$++) {
            if ($JSCompiler_StaticMethods_collision$$($other$jscomp$inline_730_x$jscomp$inline_722$$, $thisEndY$jscomp$inline_728_y$jscomp$inline_721$$)) {
              $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ = !0;
              break a;
            }
          }
        }
      }
      $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ = !1;
    }
    if (!$JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$) {
      a: {
        $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ = Math.floor(this._x);
        $endX$jscomp$inline_719_thisStartY$jscomp$inline_726$$ = Math.floor(this._y);
        $endY$jscomp$inline_720_thisEndX$jscomp$inline_727$$ = Math.ceil(this._x + this.width);
        $thisEndY$jscomp$inline_728_y$jscomp$inline_721$$ = Math.ceil(this._y + this.height);
        for (var $i$jscomp$inline_729$$ in $stendhal$$.$zone$.$entities$) {
          if ($other$jscomp$inline_730_x$jscomp$inline_722$$ = $stendhal$$.$zone$.$entities$[$i$jscomp$inline_729$$], $other$jscomp$inline_730_x$jscomp$inline_722$$ != this && 95 < $other$jscomp$inline_730_x$jscomp$inline_722$$.$getResistance$() / 100 * this.$getResistance$()) {
            var $otherStartX$jscomp$inline_731$$ = Math.floor($other$jscomp$inline_730_x$jscomp$inline_722$$._x), $otherStartY$jscomp$inline_732$$ = Math.floor($other$jscomp$inline_730_x$jscomp$inline_722$$._y), $otherEndY$jscomp$inline_733$$ = Math.ceil($other$jscomp$inline_730_x$jscomp$inline_722$$._y + $other$jscomp$inline_730_x$jscomp$inline_722$$.height);
            if ($JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ < Math.ceil($other$jscomp$inline_730_x$jscomp$inline_722$$._x + $other$jscomp$inline_730_x$jscomp$inline_722$$.width) && $endY$jscomp$inline_720_thisEndX$jscomp$inline_727$$ > $otherStartX$jscomp$inline_731$$ && $endX$jscomp$inline_719_thisStartY$jscomp$inline_726$$ < $otherEndY$jscomp$inline_733$$ && $thisEndY$jscomp$inline_728_y$jscomp$inline_721$$ > $otherStartY$jscomp$inline_732$$) {
              $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ = !0;
              break a;
            }
          }
        }
        $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ = !1;
      }
    }
    $JSCompiler_inline_result$jscomp$200_JSCompiler_temp$jscomp$201_startX$jscomp$inline_718_step_thisStartX$jscomp$inline_725$$ && (this._x = $delta$jscomp$4_oldX$$, this._y = $oldY$$);
  }
};
function $JSCompiler_StaticMethods_processPositioning$$($JSCompiler_StaticMethods_processPositioning$self$$, $key$jscomp$87_speed$jscomp$inline_739$$, $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$) {
  var $newX$jscomp$1$$ = $JSCompiler_StaticMethods_processPositioning$self$$.x, $newY$jscomp$1$$ = $JSCompiler_StaticMethods_processPositioning$self$$.y;
  "x" == $key$jscomp$87_speed$jscomp$inline_739$$ && ($newX$jscomp$1$$ = $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$);
  "y" == $key$jscomp$87_speed$jscomp$inline_739$$ && ($newY$jscomp$1$$ = $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$);
  "dir" == $key$jscomp$87_speed$jscomp$inline_739$$ && ($JSCompiler_StaticMethods_processPositioning$self$$.direction = $module$build$ts$util$Direction$default$$.$Direction$.$F$[$delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$]);
  var $speed$jscomp$1$$ = $JSCompiler_StaticMethods_processPositioning$self$$.speed;
  "speed" == $key$jscomp$87_speed$jscomp$inline_739$$ && ($speed$jscomp$1$$ = $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$);
  $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$ = $newX$jscomp$1$$;
  var $delta$jscomp$inline_1085_y$jscomp$inline_737$$ = $newY$jscomp$1$$, $direction$jscomp$inline_738$$ = $JSCompiler_StaticMethods_processPositioning$self$$.direction;
  $key$jscomp$87_speed$jscomp$inline_739$$ = $speed$jscomp$1$$;
  $JSCompiler_StaticMethods_processPositioning$self$$.$dx$ = $direction$jscomp$inline_738$$.$dx$ * $key$jscomp$87_speed$jscomp$inline_739$$;
  $JSCompiler_StaticMethods_processPositioning$self$$.$dy$ = $direction$jscomp$inline_738$$.$dy$ * $key$jscomp$87_speed$jscomp$inline_739$$;
  $module$build$ts$util$Direction$default$$.$Direction$.$A$ == $direction$jscomp$inline_738$$ || $module$build$ts$util$Direction$default$$.$Direction$.$B$ == $direction$jscomp$inline_738$$ ? ($JSCompiler_StaticMethods_processPositioning$self$$._y = $delta$jscomp$inline_1085_y$jscomp$inline_737$$, $module$build$ts$util$MathUtil$default$$.$MathUtil$.$v$($JSCompiler_StaticMethods_processPositioning$self$$._x, $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$) ? ($delta$jscomp$inline_1085_y$jscomp$inline_737$$ = 
  $direction$jscomp$inline_738$$.$dx$, $JSCompiler_StaticMethods_processPositioning$self$$.$dx$ = ($delta$jscomp$inline_1085_y$jscomp$inline_737$$ + ($delta$jscomp$inline_1085_y$jscomp$inline_737$$ - ($JSCompiler_StaticMethods_processPositioning$self$$._x + 0.1 * $JSCompiler_StaticMethods_processPositioning$self$$.$dx$ - $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$)) / $delta$jscomp$inline_1085_y$jscomp$inline_737$$ * $delta$jscomp$inline_1085_y$jscomp$inline_737$$) / 2 * $key$jscomp$87_speed$jscomp$inline_739$$ || 
  0) : $JSCompiler_StaticMethods_processPositioning$self$$._x = $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$, $JSCompiler_StaticMethods_processPositioning$self$$.$dy$ = 0) : $module$build$ts$util$Direction$default$$.$Direction$.$D$ == $direction$jscomp$inline_738$$ || $module$build$ts$util$Direction$default$$.$Direction$.$v$ == $direction$jscomp$inline_738$$ ? ($JSCompiler_StaticMethods_processPositioning$self$$._x = $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$, 
  $module$build$ts$util$MathUtil$default$$.$MathUtil$.$v$($JSCompiler_StaticMethods_processPositioning$self$$._y, $delta$jscomp$inline_1085_y$jscomp$inline_737$$) ? ($delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$ = $direction$jscomp$inline_738$$.$dy$, $JSCompiler_StaticMethods_processPositioning$self$$.$dy$ = ($delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$ + ($delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$ - ($JSCompiler_StaticMethods_processPositioning$self$$._y + 
  0.1 * $JSCompiler_StaticMethods_processPositioning$self$$.$dy$ - $delta$jscomp$inline_1085_y$jscomp$inline_737$$)) / $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$ * $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$) / 2 * $key$jscomp$87_speed$jscomp$inline_739$$ || 0) : $JSCompiler_StaticMethods_processPositioning$self$$._y = $delta$jscomp$inline_1085_y$jscomp$inline_737$$, $JSCompiler_StaticMethods_processPositioning$self$$.$dx$ = 0) : ($JSCompiler_StaticMethods_processPositioning$self$$._x = 
  $delta$jscomp$inline_1089_value$jscomp$125_x$jscomp$inline_736$$, $JSCompiler_StaticMethods_processPositioning$self$$._y = $delta$jscomp$inline_1085_y$jscomp$inline_737$$);
  if ($JSCompiler_StaticMethods_processPositioning$self$$.direction == $module$build$ts$util$Direction$default$$.$Direction$.$C$ || 0 == $speed$jscomp$1$$) {
    $JSCompiler_StaticMethods_processPositioning$self$$.$dx$ = 0, $JSCompiler_StaticMethods_processPositioning$self$$.$dy$ = 0, $JSCompiler_StaticMethods_processPositioning$self$$._x = $newX$jscomp$1$$, $JSCompiler_StaticMethods_processPositioning$self$$._y = $newY$jscomp$1$$;
  }
}
;var $module$build$ts$util$Nature$default$$ = {};
Object.defineProperty($module$build$ts$util$Nature$default$$, "__esModule", {value:!0});
$module$build$ts$util$Nature$default$$.$Nature$ = void 0;
$module$build$ts$util$Nature$default$$.$Nature$ = function($color$jscomp$4$$, $elem$jscomp$2$$) {
  this.color = $color$jscomp$4$$;
  this.$v$ = $elem$jscomp$2$$;
};
$module$build$ts$util$Nature$default$$.$Nature$.$A$ = new $module$build$ts$util$Nature$default$$.$Nature$("#c0c0c0");
$module$build$ts$util$Nature$default$$.$Nature$.$C$ = new $module$build$ts$util$Nature$default$$.$Nature$("#ff6400", "fire");
$module$build$ts$util$Nature$default$$.$Nature$.$D$ = new $module$build$ts$util$Nature$default$$.$Nature$("#8c8cff", "ice");
$module$build$ts$util$Nature$default$$.$Nature$.$F$ = new $module$build$ts$util$Nature$default$$.$Nature$("#fff08c", "light");
$module$build$ts$util$Nature$default$$.$Nature$.$B$ = new $module$build$ts$util$Nature$default$$.$Nature$("#404040", "dark");
$module$build$ts$util$Nature$default$$.$Nature$.$v$ = [$module$build$ts$util$Nature$default$$.$Nature$.$A$, $module$build$ts$util$Nature$default$$.$Nature$.$C$, $module$build$ts$util$Nature$default$$.$Nature$.$D$, $module$build$ts$util$Nature$default$$.$Nature$.$F$, $module$build$ts$util$Nature$default$$.$Nature$.$B$];
var $module$build$ts$sprite$TextSprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$TextSprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$TextSprite$default$$.$TextSprite$ = void 0;
$module$build$ts$sprite$TextSprite$default$$.$TextSprite$ = function($text$jscomp$22$$, $color$jscomp$5$$, $font$jscomp$3$$) {
  this.text = $text$jscomp$22$$;
  this.color = $color$jscomp$5$$;
  this.font = $font$jscomp$3$$;
};
$module$build$ts$sprite$TextSprite$default$$.$TextSprite$.prototype.$draw$ = function($ctx$jscomp$21$$, $x$jscomp$141$$, $y$jscomp$121$$) {
  $ctx$jscomp$21$$.font = this.font;
  $ctx$jscomp$21$$.lineWidth = 2;
  $ctx$jscomp$21$$.strokeStyle = "black";
  $ctx$jscomp$21$$.fillStyle = this.color;
  $ctx$jscomp$21$$.lineJoin = "round";
  $ctx$jscomp$21$$.strokeText(this.text, $x$jscomp$141$$, $y$jscomp$121$$);
  $ctx$jscomp$21$$.fillText(this.text, $x$jscomp$141$$, $y$jscomp$121$$);
};
function $JSCompiler_StaticMethods_getTextMetrics$$($JSCompiler_StaticMethods_getTextMetrics$self$$, $ctx$jscomp$22$$) {
  $JSCompiler_StaticMethods_getTextMetrics$self$$.$v$ || ($ctx$jscomp$22$$.font = $JSCompiler_StaticMethods_getTextMetrics$self$$.font, $JSCompiler_StaticMethods_getTextMetrics$self$$.$v$ = $ctx$jscomp$22$$.measureText($JSCompiler_StaticMethods_getTextMetrics$self$$.text));
  return $JSCompiler_StaticMethods_getTextMetrics$self$$.$v$;
}
;var $module$build$ts$sprite$Floater$default$$ = {};
Object.defineProperty($module$build$ts$sprite$Floater$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$Floater$default$$.$Floater$ = void 0;
$module$build$ts$sprite$Floater$default$$.$Floater$ = function($message$jscomp$35$$, $color$jscomp$6$$) {
  this.message = $message$jscomp$35$$;
  this.color = $color$jscomp$6$$;
  this.$initTime$ = Date.now();
  this.$v$ = new $module$build$ts$sprite$TextSprite$default$$.$TextSprite$(this.message, this.color, "12px sans-serif");
};
$module$build$ts$sprite$Floater$default$$.$Floater$.prototype.$draw$ = function($ctx$jscomp$23$$, $x$jscomp$142$$, $y$jscomp$122$$) {
  var $textOffset$$ = $JSCompiler_StaticMethods_getTextMetrics$$(this.$v$, $ctx$jscomp$23$$).width / 2, $timeDiff$$ = Date.now() - this.$initTime$;
  this.$v$.$draw$($ctx$jscomp$23$$, $x$jscomp$142$$ - $textOffset$$, $y$jscomp$122$$ - $timeDiff$$ / 50);
  return 2000 < $timeDiff$$;
};
var $module$build$ts$sprite$EmojiSprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$EmojiSprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$EmojiSprite$default$$.$EmojiSprite$ = void 0;
$module$build$ts$sprite$EmojiSprite$default$$.$EmojiSprite$ = function($sprite$jscomp$7$$, $owner$$) {
  this.timeStamp = Date.now();
  this.$v$ = $owner$$;
  this.$sprite$ = $sprite$jscomp$7$$;
};
$module$build$ts$sprite$EmojiSprite$default$$.$EmojiSprite$.prototype.$draw$ = function($ctx$jscomp$24$$) {
  var $x$jscomp$143$$ = 32 * this.$v$._x - 16, $y$jscomp$123$$ = 32 * this.$v$._y - 32;
  0 > $x$jscomp$143$$ && ($x$jscomp$143$$ = 0);
  0 > $y$jscomp$123$$ && ($y$jscomp$123$$ = 0);
  this.$sprite$.height && $ctx$jscomp$24$$.drawImage(this.$sprite$, $x$jscomp$143$$, $y$jscomp$123$$);
  return Date.now() > this.timeStamp + 5000;
};
var $module$build$ts$sprite$action$ActionSprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$action$ActionSprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$ = void 0;
$module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$ = function() {
  this.$initTime$ = Date.now();
};
$module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$.prototype.$expired$ = function() {
  return 180 < Date.now() - this.$initTime$;
};
var $module$build$ts$sprite$action$BarehandAttackSprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$action$BarehandAttackSprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$action$BarehandAttackSprite$default$$.$BarehandAttackSprite$ = void 0;
$module$build$ts$sprite$action$BarehandAttackSprite$default$$.$BarehandAttackSprite$ = function($source$jscomp$19$$, $image$jscomp$9$$) {
  var $$jscomp$super$this$jscomp$41$$ = $module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$.call(this) || this;
  $$jscomp$super$this$jscomp$41$$.dir = $source$jscomp$19$$.dir;
  $$jscomp$super$this$jscomp$41$$.image = $image$jscomp$9$$;
  return $$jscomp$super$this$jscomp$41$$;
};
$$jscomp$inherits$$($module$build$ts$sprite$action$BarehandAttackSprite$default$$.$BarehandAttackSprite$, $module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$);
$module$build$ts$sprite$action$BarehandAttackSprite$default$$.$BarehandAttackSprite$.prototype.$draw$ = function($ctx$jscomp$25$$, $sx$jscomp$4_x$jscomp$144$$, $sy$jscomp$6_y$jscomp$124$$, $entityWidth$$, $entityHeight$$) {
  if (this.image && this.image.height) {
    var $frameIndex$$ = Math.floor(Math.min((Date.now() - this.$initTime$) / 60, 3)), $drawWidth$jscomp$1$$ = this.image.width / 3, $drawHeight$jscomp$1$$ = this.image.height / 4, $centerX$jscomp$1$$ = $sx$jscomp$4_x$jscomp$144$$ + ($entityWidth$$ - $drawWidth$jscomp$1$$) / 2, $centerY$jscomp$1$$ = $sy$jscomp$6_y$jscomp$124$$ + ($entityHeight$$ - $drawHeight$jscomp$1$$) / 2;
    switch(this.dir + "") {
      case "1":
        $sx$jscomp$4_x$jscomp$144$$ = $centerX$jscomp$1$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$6_y$jscomp$124$$ -= 1.5 * $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$;
        break;
      case "3":
        $sx$jscomp$4_x$jscomp$144$$ = $centerX$jscomp$1$$;
        $sy$jscomp$6_y$jscomp$124$$ = $sy$jscomp$6_y$jscomp$124$$ + $entityHeight$$ - $drawHeight$jscomp$1$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2;
        break;
      case "4":
        $sx$jscomp$4_x$jscomp$144$$ -= $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$6_y$jscomp$124$$ = $centerY$jscomp$1$$ - $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2;
        break;
      case "2":
        $sx$jscomp$4_x$jscomp$144$$ = $sx$jscomp$4_x$jscomp$144$$ + $entityWidth$$ - $drawWidth$jscomp$1$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$6_y$jscomp$124$$ = $centerY$jscomp$1$$;
        break;
      default:
        $sx$jscomp$4_x$jscomp$144$$ = $centerX$jscomp$1$$, $sy$jscomp$6_y$jscomp$124$$ = $centerY$jscomp$1$$;
    }
    $ctx$jscomp$25$$.drawImage(this.image, $frameIndex$$ * $drawWidth$jscomp$1$$, (this.dir - 1) * $drawHeight$jscomp$1$$, $drawWidth$jscomp$1$$, $drawHeight$jscomp$1$$, $sx$jscomp$4_x$jscomp$144$$, $sy$jscomp$6_y$jscomp$124$$, $drawWidth$jscomp$1$$, $drawHeight$jscomp$1$$);
  }
};
var $module$build$ts$sprite$action$MeleeAttackSprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$action$MeleeAttackSprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$action$MeleeAttackSprite$default$$.$MeleeAttackSprite$ = void 0;
$module$build$ts$sprite$action$MeleeAttackSprite$default$$.$MeleeAttackSprite$ = function($source$jscomp$20$$, $image$jscomp$10$$) {
  var $$jscomp$super$this$jscomp$42$$ = $module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$.call(this) || this;
  $$jscomp$super$this$jscomp$42$$.dir = $source$jscomp$20$$.dir;
  $$jscomp$super$this$jscomp$42$$.image = $image$jscomp$10$$;
  return $$jscomp$super$this$jscomp$42$$;
};
$$jscomp$inherits$$($module$build$ts$sprite$action$MeleeAttackSprite$default$$.$MeleeAttackSprite$, $module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$);
$module$build$ts$sprite$action$MeleeAttackSprite$default$$.$MeleeAttackSprite$.prototype.$draw$ = function($ctx$jscomp$26$$, $sx$jscomp$5_x$jscomp$145$$, $sy$jscomp$7_y$jscomp$125$$, $entityWidth$jscomp$1$$, $entityHeight$jscomp$1$$) {
  if (this.image && this.image.height) {
    var $drawWidth$jscomp$2$$ = this.image.width, $drawHeight$jscomp$2$$ = this.image.height, $centerX$jscomp$2$$ = $sx$jscomp$5_x$jscomp$145$$ + ($entityWidth$jscomp$1$$ - $drawWidth$jscomp$2$$) / 2, $centerY$jscomp$2$$ = $sy$jscomp$7_y$jscomp$125$$ + ($entityHeight$jscomp$1$$ - $drawHeight$jscomp$2$$) / 2;
    switch(this.dir + "") {
      case "1":
        $sx$jscomp$5_x$jscomp$145$$ = $centerX$jscomp$2$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$7_y$jscomp$125$$ -= 1.5 * $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$;
        break;
      case "3":
        $sx$jscomp$5_x$jscomp$145$$ = $centerX$jscomp$2$$;
        $sy$jscomp$7_y$jscomp$125$$ = $sy$jscomp$7_y$jscomp$125$$ + $entityHeight$jscomp$1$$ - $drawHeight$jscomp$2$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2;
        break;
      case "4":
        $sx$jscomp$5_x$jscomp$145$$ -= $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$7_y$jscomp$125$$ = $centerY$jscomp$2$$ - $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2;
        break;
      case "2":
        $sx$jscomp$5_x$jscomp$145$$ = $sx$jscomp$5_x$jscomp$145$$ + $entityWidth$jscomp$1$$ - $drawWidth$jscomp$2$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$7_y$jscomp$125$$ = $centerY$jscomp$2$$;
        break;
      default:
        $sx$jscomp$5_x$jscomp$145$$ = $centerX$jscomp$2$$, $sy$jscomp$7_y$jscomp$125$$ = $centerY$jscomp$2$$;
    }
    $ctx$jscomp$26$$.drawImage(this.image, 0, 0, $drawWidth$jscomp$2$$, $drawHeight$jscomp$2$$, $sx$jscomp$5_x$jscomp$145$$, $sy$jscomp$7_y$jscomp$125$$, $drawWidth$jscomp$2$$, $drawHeight$jscomp$2$$);
  }
};
var $module$build$ts$sprite$action$RangedAttackSprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$action$RangedAttackSprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$action$RangedAttackSprite$default$$.$RangedAttackSprite$ = void 0;
$module$build$ts$sprite$action$RangedAttackSprite$default$$.$RangedAttackSprite$ = function($source$jscomp$21$$, $target$jscomp$112$$, $color$jscomp$7$$, $weapon$jscomp$1$$) {
  var $$jscomp$super$this$jscomp$43$$ = $module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$.call(this) || this;
  $$jscomp$super$this$jscomp$43$$.image = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/combat/ranged.png");
  $$jscomp$super$this$jscomp$43$$.dir = $source$jscomp$21$$.dir;
  $$jscomp$super$this$jscomp$43$$.$v$ = 32 * ($target$jscomp$112$$.x + $target$jscomp$112$$.width / 2);
  $$jscomp$super$this$jscomp$43$$.$A$ = 32 * ($target$jscomp$112$$.y + $target$jscomp$112$$.height / 2);
  $$jscomp$super$this$jscomp$43$$.color = $color$jscomp$7$$;
  $$jscomp$super$this$jscomp$43$$.$B$ = $weapon$jscomp$1$$;
  return $$jscomp$super$this$jscomp$43$$;
};
$$jscomp$inherits$$($module$build$ts$sprite$action$RangedAttackSprite$default$$.$RangedAttackSprite$, $module$build$ts$sprite$action$ActionSprite$default$$.$ActionSprite$);
$module$build$ts$sprite$action$RangedAttackSprite$default$$.$RangedAttackSprite$.prototype.$draw$ = function($ctx$jscomp$27$$, $sx$jscomp$6_x$jscomp$146$$, $sy$jscomp$8_y$jscomp$126$$, $entityWidth$jscomp$2$$, $entityHeight$jscomp$2$$) {
  var $dtime$jscomp$1_yRow$$ = Date.now() - this.$initTime$, $endX$jscomp$1_frame$jscomp$2$$ = Math.min($dtime$jscomp$1_yRow$$ / 60, 4), $drawWidth$jscomp$3_startX$jscomp$2$$ = $sx$jscomp$6_x$jscomp$146$$ + $entityWidth$jscomp$2$$ / 4, $drawHeight$jscomp$3_startY$jscomp$2$$ = $sy$jscomp$8_y$jscomp$126$$ + $entityHeight$jscomp$2$$ / 4, $centerX$jscomp$3_endY$jscomp$1_yLength$$ = (this.$A$ - $drawHeight$jscomp$3_startY$jscomp$2$$) / 4, $centerY$jscomp$3_xLength$$ = (this.$v$ - $drawWidth$jscomp$3_startX$jscomp$2$$) / 
  4;
  $drawHeight$jscomp$3_startY$jscomp$2$$ += $endX$jscomp$1_frame$jscomp$2$$ * $centerX$jscomp$3_endY$jscomp$1_yLength$$;
  $centerX$jscomp$3_endY$jscomp$1_yLength$$ = $drawHeight$jscomp$3_startY$jscomp$2$$ + $centerX$jscomp$3_endY$jscomp$1_yLength$$;
  $drawWidth$jscomp$3_startX$jscomp$2$$ += $endX$jscomp$1_frame$jscomp$2$$ * $centerY$jscomp$3_xLength$$;
  $endX$jscomp$1_frame$jscomp$2$$ = $drawWidth$jscomp$3_startX$jscomp$2$$ + $centerY$jscomp$3_xLength$$;
  $ctx$jscomp$27$$.strokeStyle = this.color;
  $ctx$jscomp$27$$.lineWidth = 2;
  $ctx$jscomp$27$$.moveTo($drawWidth$jscomp$3_startX$jscomp$2$$, $drawHeight$jscomp$3_startY$jscomp$2$$);
  $ctx$jscomp$27$$.lineTo($endX$jscomp$1_frame$jscomp$2$$, $centerX$jscomp$3_endY$jscomp$1_yLength$$);
  $ctx$jscomp$27$$.stroke();
  if ("ranged" === this.$B$ && this.image.height) {
    $endX$jscomp$1_frame$jscomp$2$$ = Math.floor(Math.min($dtime$jscomp$1_yRow$$ / 60, 3));
    $dtime$jscomp$1_yRow$$ = this.dir - 1;
    $drawWidth$jscomp$3_startX$jscomp$2$$ = this.image.width / 3;
    $drawHeight$jscomp$3_startY$jscomp$2$$ = this.image.height / 4;
    $centerX$jscomp$3_endY$jscomp$1_yLength$$ = $sx$jscomp$6_x$jscomp$146$$ + ($entityWidth$jscomp$2$$ - $drawWidth$jscomp$3_startX$jscomp$2$$) / 2;
    $centerY$jscomp$3_xLength$$ = $sy$jscomp$8_y$jscomp$126$$ + ($entityHeight$jscomp$2$$ - $drawHeight$jscomp$3_startY$jscomp$2$$) / 2;
    switch(this.dir + "") {
      case "1":
        $sx$jscomp$6_x$jscomp$146$$ = $centerX$jscomp$3_endY$jscomp$1_yLength$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$8_y$jscomp$126$$ -= 1.5 * $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$;
        break;
      case "3":
        $sx$jscomp$6_x$jscomp$146$$ = $centerX$jscomp$3_endY$jscomp$1_yLength$$;
        $sy$jscomp$8_y$jscomp$126$$ = $sy$jscomp$8_y$jscomp$126$$ + $entityHeight$jscomp$2$$ - $drawHeight$jscomp$3_startY$jscomp$2$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2;
        break;
      case "4":
        $sx$jscomp$6_x$jscomp$146$$ -= $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$8_y$jscomp$126$$ = $centerY$jscomp$3_xLength$$ - $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$ / 2;
        break;
      case "2":
        $sx$jscomp$6_x$jscomp$146$$ = $sx$jscomp$6_x$jscomp$146$$ + $entityWidth$jscomp$2$$ - $drawWidth$jscomp$3_startX$jscomp$2$$ + $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$ / 2;
        $sy$jscomp$8_y$jscomp$126$$ = $centerY$jscomp$3_xLength$$;
        break;
      default:
        $sx$jscomp$6_x$jscomp$146$$ = $centerX$jscomp$3_endY$jscomp$1_yLength$$, $sy$jscomp$8_y$jscomp$126$$ = $centerY$jscomp$3_xLength$$;
    }
    $ctx$jscomp$27$$.drawImage(this.image, $endX$jscomp$1_frame$jscomp$2$$ * $drawWidth$jscomp$3_startX$jscomp$2$$, $dtime$jscomp$1_yRow$$ * $drawHeight$jscomp$3_startY$jscomp$2$$, $drawWidth$jscomp$3_startX$jscomp$2$$, $drawHeight$jscomp$3_startY$jscomp$2$$, $sx$jscomp$6_x$jscomp$146$$, $sy$jscomp$8_y$jscomp$126$$, $drawWidth$jscomp$3_startX$jscomp$2$$, $drawHeight$jscomp$3_startY$jscomp$2$$);
  }
};
var $module$build$ts$entity$RPEntity$default$$ = {};
Object.defineProperty($module$build$ts$entity$RPEntity$default$$, "__esModule", {value:!0});
$module$build$ts$entity$RPEntity$default$$.$RPEntity$ = void 0;
var $emojiStore$$module$build$ts$entity$RPEntity$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$B$();
$module$build$ts$entity$RPEntity$default$$.$RPEntity$ = function() {
  var $$jscomp$super$this$jscomp$44$$ = $module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$44$$.zIndex = 8000;
  $$jscomp$super$this$jscomp$44$$.$J$ = 0;
  $$jscomp$super$this$jscomp$44$$.$H$ = "";
  $$jscomp$super$this$jscomp$44$$.$G$ = "#FFFFFF";
  $$jscomp$super$this$jscomp$44$$.$attackSprite$ = void 0;
  $$jscomp$super$this$jscomp$44$$.$attackResult$ = void 0;
  $$jscomp$super$this$jscomp$44$$.dir = 3;
  $$jscomp$super$this$jscomp$44$$.$B$ = [];
  $$jscomp$super$this$jscomp$44$$.$D$ = 0;
  $$jscomp$super$this$jscomp$44$$.$F$ = 0;
  $$jscomp$super$this$jscomp$44$$.$v$ = {size:0};
  return $$jscomp$super$this$jscomp$44$$;
};
$$jscomp$inherits$$($module$build$ts$entity$RPEntity$default$$.$RPEntity$, $module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype;
$JSCompiler_prototypeAlias$$.set = function($change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$, $value$jscomp$126$$) {
  var $oldValue$jscomp$1$$ = this[$change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$];
  $module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$.prototype.set.call(this, $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$, $value$jscomp$126$$);
  if ("text" == $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$) {
    this.$say$($value$jscomp$126$$);
  } else {
    if (-1 !== ["hp", "base_hp"].indexOf($change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$)) {
      this[$change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$] = parseInt($value$jscomp$126$$, 10), "hp" === $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ && void 0 != $oldValue$jscomp$1$$ && ($change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ = this[$change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$] - $oldValue$jscomp$1$$, 0 < $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ ? $JSCompiler_StaticMethods_addFloater$$(this, 
      "+" + $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$, "#00ff00") : 0 > $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ && $JSCompiler_StaticMethods_addFloater$$(this, $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$.toString(), "#ff0000"));
    } else {
      if ("id" === $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ && !$oldValue$jscomp$1$$ && this.$_target$) {
        $JSCompiler_StaticMethods_onTargeted$$(this.$_target$, this);
      } else {
        if ("target" === $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ && this.id) {
          this.$_target$ && $JSCompiler_StaticMethods_onAttackStopped$$(this.$_target$, this), (this.$_target$ = $marauroa$$.$currentZone$[$value$jscomp$126$$]) && $JSCompiler_StaticMethods_onTargeted$$(this.$_target$, this);
        } else {
          if ("away" === $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$) {
            $JSCompiler_StaticMethods_addFloater$$(this, "Away", "#ffff00");
          } else {
            if ("grumpy" === $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$) {
              $JSCompiler_StaticMethods_addFloater$$(this, "Grumpy", "#ffff00");
            } else {
              if ("xp" === $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ && "undefined" !== typeof $oldValue$jscomp$1$$) {
                $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ = this[$change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$] - $oldValue$jscomp$1$$, 0 < $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ ? ($JSCompiler_StaticMethods_addFloater$$(this, "+" + $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$, "#4169e1"), $module$build$ts$util$Chat$default$$.$Chat$.log("significant_positive", this.title + " earns " + $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ + 
                " experience points.")) : 0 > $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ && ($JSCompiler_StaticMethods_addFloater$$(this, $change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$.toString(), "#ff8f8f"), $module$build$ts$util$Chat$default$$.$Chat$.log("significant_negative", this.title + " loses " + Math.abs($change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$) + " experience points."));
              } else {
                if (-1 < ["level", "atk", "ratk", "def"].indexOf($change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$) && "undefined" !== typeof $oldValue$jscomp$1$$) {
                  if ($marauroa$$.$me$ && $value$jscomp$126$$ !== $oldValue$jscomp$1$$ && $JSCompiler_StaticMethods_isInHearingRange$$(this)) {
                    var $msg$jscomp$inline_751$$ = this.title, $msgtype$jscomp$inline_752$$ = "significant_positive";
                    $value$jscomp$126$$ > $oldValue$jscomp$1$$ ? $msg$jscomp$inline_751$$ += " reaches " : $value$jscomp$126$$ < $oldValue$jscomp$1$$ && ($msg$jscomp$inline_751$$ += " drops to ", $msgtype$jscomp$inline_752$$ = "significant_negative");
                    $module$build$ts$util$Chat$default$$.$Chat$.$v$($msgtype$jscomp$inline_752$$, $msg$jscomp$inline_751$$ + ($change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$ + " " + $value$jscomp$126$$));
                  }
                } else {
                  -1 < ["title", "name", "class", "type"].indexOf($change$jscomp$inline_742_change$jscomp$inline_745_key$jscomp$88$$) && this.$createTitleTextSprite$();
                }
              }
            }
          }
        }
      }
    }
  }
};
$JSCompiler_prototypeAlias$$.$unset$ = function($key$jscomp$89$$) {
  "target" === $key$jscomp$89$$ && this.$_target$ ? ($JSCompiler_StaticMethods_onAttackStopped$$(this.$_target$, this), this.$_target$ = void 0) : "away" === $key$jscomp$89$$ ? $JSCompiler_StaticMethods_addFloater$$(this, "Back", "#ffff00") : "grumpy" === $key$jscomp$89$$ && $JSCompiler_StaticMethods_addFloater$$(this, "Receptive", "#ffff00");
  $module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$.prototype.$unset$.call(this, $key$jscomp$89$$);
};
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return "undefined" == typeof this.ghostmode || $marauroa$$.$me$ && $JSCompiler_StaticMethods_isAdmin$$($marauroa$$.$me$);
};
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$2$$) {
  $module$build$ts$entity$ActiveEntity$default$$.$ActiveEntity$.prototype.$buildActions$.call(this, $list$jscomp$2$$);
  this.menu || ($marauroa$$.$me$.$_target$ === this ? $list$jscomp$2$$.push({title:"Stop attack", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"stop", zone:$marauroa$$.$currentZoneName$, attack:""});
  }}) : this !== $marauroa$$.$me$ && $list$jscomp$2$$.push({title:"Attack", type:"attack"}));
  this != $marauroa$$.$me$ && $list$jscomp$2$$.push({title:"Push", type:"push"});
};
$JSCompiler_prototypeAlias$$.$say$ = function($sprite$jscomp$inline_1092_text$jscomp$23$$, $JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$) {
  if ($marauroa$$.$me$ && $JSCompiler_StaticMethods_isInHearingRange$$(this, $JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$)) {
    if ($JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$ = $emojiStore$$module$build$ts$entity$RPEntity$$.create($sprite$jscomp$inline_1092_text$jscomp$23$$)) {
      $sprite$jscomp$inline_1092_text$jscomp$23$$ = new $module$build$ts$sprite$EmojiSprite$default$$.$EmojiSprite$($JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$, this), $stendhal$$.$ui$.$gamewindow$.$emojiSprites$.push($sprite$jscomp$inline_1092_text$jscomp$23$$), $module$build$ts$util$Chat$default$$.$Chat$.log("emoji", $JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$, this.title);
    } else {
      if ($sprite$jscomp$inline_1092_text$jscomp$23$$.startsWith("!me")) {
        $module$build$ts$util$Chat$default$$.$Chat$.log("emote", $sprite$jscomp$inline_1092_text$jscomp$23$$.replace(/^!me/, this.title));
      } else {
        $JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$ = $stendhal$$.$ui$.$gamewindow$;
        var $sprite$jscomp$inline_1095$$ = new $module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$($sprite$jscomp$inline_1092_text$jscomp$23$$, this);
        $JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$.$textSprites$.push($sprite$jscomp$inline_1095$$);
        $sprite$jscomp$inline_1095$$.$onAdded$($JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1094_emoji$jscomp$2_rangeSquared$jscomp$1$$.$ctx$);
        $module$build$ts$util$Chat$default$$.$Chat$.log("normal", $sprite$jscomp$inline_1092_text$jscomp$23$$, this.title);
      }
    }
  }
};
$JSCompiler_prototypeAlias$$.$setStatusBarOffset$ = function() {
  var $screenOffsetY$$ = $stendhal$$.$ui$.$gamewindow$.offsetY, $entityBottom$$ = 32 * this._y + 32 * this.height, $entityTop$$ = $entityBottom$$ - this.drawHeight - 6 - 26;
  this.$D$ = $screenOffsetY$$ > $entityTop$$ && $screenOffsetY$$ < $entityBottom$$ ? $screenOffsetY$$ - $entityTop$$ : 0;
};
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$29$$) {
  if ("undefined" == typeof this.ghostmode || !$marauroa$$.$me$ || $JSCompiler_StaticMethods_isAdmin$$($marauroa$$.$me$)) {
    if (0 < this.$v$.size) {
      if ($ctx$jscomp$29$$.lineWidth = 1, $ctx$jscomp$29$$.ellipse instanceof Function) {
        var $xRad$jscomp$inline_778$$ = 16 * this.width, $yRad$jscomp$inline_779$$ = 16 * this.height / Math.SQRT2, $centerX$jscomp$inline_780$$ = 32 * this._x + $xRad$jscomp$inline_778$$, $centerY$jscomp$inline_781$$ = 32 * (this._y + this.height) - $yRad$jscomp$inline_779$$;
        $ctx$jscomp$29$$.strokeStyle = "#4a0000";
        $ctx$jscomp$29$$.beginPath();
        $ctx$jscomp$29$$.ellipse($centerX$jscomp$inline_780$$, $centerY$jscomp$inline_781$$, $xRad$jscomp$inline_778$$, $yRad$jscomp$inline_779$$, 0, 0, Math.PI, !1);
        $ctx$jscomp$29$$.stroke();
        $ctx$jscomp$29$$.strokeStyle = "#e60a0a";
        $ctx$jscomp$29$$.beginPath();
        $ctx$jscomp$29$$.ellipse($centerX$jscomp$inline_780$$, $centerY$jscomp$inline_781$$, $xRad$jscomp$inline_778$$, $yRad$jscomp$inline_779$$, 0, Math.PI, 2 * Math.PI, !1);
        $ctx$jscomp$29$$.stroke();
      } else {
        $ctx$jscomp$29$$.strokeStyle = "#e60a0a", $ctx$jscomp$29$$.strokeRect(32 * this._x, 32 * this._y, 32 * this.width, 32 * this.height);
      }
    }
    $JSCompiler_StaticMethods_getAttackTarget$$(this) === $marauroa$$.$me$ && ($ctx$jscomp$29$$.lineWidth = 1, $ctx$jscomp$29$$.ellipse instanceof Function ? ($xRad$jscomp$inline_778$$ = 16 * this.width - 1, $yRad$jscomp$inline_779$$ = 16 * this.height / Math.SQRT2 - 1, $centerX$jscomp$inline_780$$ = 32 * this._x + $xRad$jscomp$inline_778$$ + 1, $centerY$jscomp$inline_781$$ = 32 * (this._y + this.height) - $yRad$jscomp$inline_779$$ - 1, $ctx$jscomp$29$$.strokeStyle = "#ffc800", $ctx$jscomp$29$$.beginPath(), 
    $ctx$jscomp$29$$.ellipse($centerX$jscomp$inline_780$$, $centerY$jscomp$inline_781$$, $xRad$jscomp$inline_778$$, $yRad$jscomp$inline_779$$, 0, 0, Math.PI, !1), $ctx$jscomp$29$$.stroke(), $ctx$jscomp$29$$.strokeStyle = "#ffdd0a", $ctx$jscomp$29$$.beginPath(), $ctx$jscomp$29$$.ellipse($centerX$jscomp$inline_780$$, $centerY$jscomp$inline_781$$, $xRad$jscomp$inline_778$$, $yRad$jscomp$inline_779$$, 0, Math.PI, 2 * Math.PI, !1), $ctx$jscomp$29$$.stroke()) : ($ctx$jscomp$29$$.strokeStyle = "#ffdd0a", 
    $ctx$jscomp$29$$.strokeRect(32 * this._x + 1, 32 * this._y + 1, 32 * this.width - 2, 32 * this.height - 2)));
    this.$attackResult$ && this.$attackResult$.$draw$($ctx$jscomp$29$$, 32 * (this._x + this.width) - 10, 32 * (this._y + this.height) - 10) && (this.$attackResult$ = void 0);
    this.$drawMain$($ctx$jscomp$29$$);
    null != this.$attackSprite$ && (this.$attackSprite$.$expired$() ? this.$attackSprite$ = null : this.$attackSprite$.$draw$($ctx$jscomp$29$$, 32 * this._x, 32 * this._y, this.width * $stendhal$$.$ui$.$gamewindow$.$targetTileWidth$, this.height * $stendhal$$.$ui$.$gamewindow$.$targetTileHeight$));
    $JSCompiler_StaticMethods_drawStatusIcons$$(this, $ctx$jscomp$29$$);
  }
};
$JSCompiler_prototypeAlias$$.$drawMain$ = function($ctx$jscomp$30$$) {
  if ("undefined" != typeof this.outfit || "undefined" != typeof this.outfit_ext) {
    var $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$ = {};
    if ("outfit_ext" in this) {
      var $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = "body dress head mouth eyes mask hair hat detail".split(" ");
      for (var $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $$jscomp$makeIterator$$(this.outfit_ext.split(",")), $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = 
      $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.next(); !$$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$.done; $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = 
      $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.next()) {
        $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$.value, $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$.includes("=") && 
        ($$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$.split("="), $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$[$$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$[0]] = 
        parseInt($$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$[1], 10));
      }
    } else {
      $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = ["body", "dress", "head", "hair", "detail"], $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.body = this.outfit % 100, $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.dress = Math.floor(this.outfit / 100) % 100, $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.head = Math.floor(this.outfit / 10000) % 100, $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.hair = 
      Math.floor(this.outfit / 1000000) % 100, $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.detail = Math.floor(this.outfit / 100000000) % 100;
    }
    $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.shadows") && "undefined" === typeof this.no_shadow && ($$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $JSCompiler_StaticMethods_getShadow$$("48x64")) && $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.complete && 
    $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.height && $JSCompiler_StaticMethods_drawSpriteImage$$(this, $ctx$jscomp$30$$, $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$);
    this.$A$ && this.$A$.clearRect(0, 0, this.$A$.canvas.width, this.$A$.canvas.height);
    -1 < $stendhal$$.data.$outfit$.$detailRearLayers$.indexOf($filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.detail) && ($$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$.splice(0, 0, "detail-rear"), $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$["detail-rear"] = $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.detail);
    $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = $$jscomp$makeIterator$$($$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$);
    for ($$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$.next(); !$$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.done; $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = 
    $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$.next()) {
      if ($$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.value, "hair" != $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ || 
      0 > $stendhal$$.data.$outfit$.$hats_no_hair$.indexOf($filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.hat)) {
        var $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$;
        $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$;
        $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$[$$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$];
        var $JSCompiler_StaticMethods_getFiltered$self$jscomp$inline_1109_body$jscomp$inline_1100$$ = $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.body;
        if ("undefined" === typeof $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ || 0 > $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$) {
          $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = null;
        } else {
          var $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ = "" + $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$;
          10 > $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ ? $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ = "00" + $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ : 100 > $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ && 
          ($img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ = "0" + $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$);
          if ("body" === $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ && 3 > $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ && $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, 
          "gamescreen.nonude")) {
            $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ += "-nonude";
          } else {
            if ($JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$ = "dress" === $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$) {
              $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$ = $stendhal$$.data.$outfit$, $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$ = 1 == $JSCompiler_StaticMethods_getFiltered$self$jscomp$inline_1109_body$jscomp$inline_1100$$ && -1 < $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$.$v$.indexOf($$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$);
            }
            $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$ ? $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ += "b" : $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.endsWith("-rear") && 
            ($img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ += "-rear", $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.replace(/-rear$/, 
            ""));
          }
          $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$ = $stendhal$$.$paths$.$sprites$ + "/outfit/" + $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ + "/" + $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ + 
          ".png";
          $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = this.outfit_colors;
          $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = "body" === $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ || 
          "head" === $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ ? "skin" : $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$;
          if ("undefined" !== typeof $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ && "undefined" !== typeof $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$[$$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$]) {
            if ($$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$[$$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$], 
            $JSCompiler_StaticMethods_getFiltered$self$jscomp$inline_1109_body$jscomp$inline_1100$$ = $stendhal$$.data.$sprites$, $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ = $JSCompiler_StaticMethods_getFiltered$self$jscomp$inline_1109_body$jscomp$inline_1100$$.get($JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$), ($$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = 
            $JSCompiler_StaticMethods_getFiltered$self$jscomp$inline_1109_body$jscomp$inline_1100$$.filter.trueColor) && $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.complete && 0 !== $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.width && 0 !== $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.height) {
              $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$ = $JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$ + " trueColor " + $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$;
              var $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$ = $JSCompiler_StaticMethods_getFiltered$self$jscomp$inline_1109_body$jscomp$inline_1100$$.images[$JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$];
              if ("undefined" === typeof $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$) {
                $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$ = document.createElement("canvas");
                $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$.width = $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.width;
                $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$.height = $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.height;
                var $ctx$jscomp$inline_1115$$ = $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$.getContext("2d");
                $ctx$jscomp$inline_1115$$.drawImage($img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$, 0, 0);
                $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$ = $ctx$jscomp$inline_1115$$.getImageData(0, 0, $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.width, $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.height);
                $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$($img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$.data, $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$);
                $ctx$jscomp$inline_1115$$.putImageData($img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$, 0, 0);
                $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$.complete = !0;
                $JSCompiler_StaticMethods_getFiltered$self$jscomp$inline_1109_body$jscomp$inline_1100$$.images[$JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$] = $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$;
              }
              $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $canvas$jscomp$inline_1114_filtered$jscomp$inline_1113$$;
            } else {
              $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $img$jscomp$inline_1110_imgData$jscomp$inline_1116_n$jscomp$inline_1101$$;
            }
          } else {
            $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ = $stendhal$$.data.$sprites$.get($JSCompiler_StaticMethods_drawBustyDress$self$jscomp$inline_1103_JSCompiler_temp$jscomp$inline_1102_filename$jscomp$inline_1104_filteredName$jscomp$inline_1112$$);
          }
        }
        $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$ && $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.complete && 
        $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.height && (this.$A$ || ($$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$ = document.createElement("canvas"), 
        this.$A$ = $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$.getContext("2d"), $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$.width = $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.width, 
        $$jscomp$inline_791_colors$jscomp$inline_1105_index$jscomp$inline_1099_layer$jscomp$inline_797_ocanvas$jscomp$inline_799_param$jscomp$inline_1108_part$jscomp$inline_792_tmp$jscomp$inline_793$$.height = $$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$.height), this.$A$.drawImage($$jscomp$inline_790_$jscomp$key$layer$jscomp$inline_796_JSCompiler_inline_result$jscomp$1002_JSCompiler_temp$jscomp$inline_1107_colorname$jscomp$inline_1106_filterFn$jscomp$inline_1111_lsprite$jscomp$inline_798_part$jscomp$inline_1098_shadow$jscomp$inline_794$$, 
        0, 0));
      }
    }
    this.$A$ && $JSCompiler_StaticMethods_drawSpriteImage$$(this, $ctx$jscomp$30$$, this.$A$.canvas);
  } else {
    $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$ = $stendhal$$.$paths$.$sprites$ + "/" + this.$H$ + "/" + this["class"];
    "undefined" != typeof this.subclass && ($filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$ = $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$ + "/" + this.subclass);
    if ($$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = !$JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.blood")) {
      $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = 1 == $stendhal$$.data.$sprites$.$knownSafeSprites$[$filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$];
    }
    $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$ = $stendhal$$.data.$sprites$.get($$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ ? $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$ + "-safe.png" : $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$ + ".png");
    $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.shadows") && "undefined" === typeof this.no_shadow && ($$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = this.shadow_style, "undefined" === typeof $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ && ($$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.width / 
    3 + "x" + $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$.height / 4), $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ = $JSCompiler_StaticMethods_getShadow$$($$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$), "undefined" !== typeof $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$ && $JSCompiler_StaticMethods_drawSpriteImage$$(this, 
    $ctx$jscomp$30$$, $$jscomp$inline_795_JSCompiler_temp$jscomp$175_layers$jscomp$inline_788_shadow$jscomp$1_shadow_style$$));
    $JSCompiler_StaticMethods_drawSpriteImage$$(this, $ctx$jscomp$30$$, $filename$jscomp$16_image$jscomp$11_outfit$jscomp$inline_789$$);
  }
};
function $JSCompiler_StaticMethods_drawStatusIcons$$($JSCompiler_StaticMethods_drawStatusIcons$self$$, $ctx$jscomp$31$$) {
  function $drawAnimatedIcon$$($icon$jscomp$1_iconPath$$, $delay$jscomp$2$$, $x$jscomp$149$$, $y$jscomp$129$$, $dimW_fWidth$$) {
    $icon$jscomp$1_iconPath$$ = $stendhal$$.data.$sprites$.get($icon$jscomp$1_iconPath$$);
    var $dimH$$ = $icon$jscomp$1_iconPath$$.height;
    $dimW_fWidth$$ = "undefined" !== typeof $dimW_fWidth$$ ? $dimW_fWidth$$ : $dimH$$;
    $ctx$jscomp$31$$.drawImage($icon$jscomp$1_iconPath$$, Math.floor(Date.now() / $delay$jscomp$2$$) % ($icon$jscomp$1_iconPath$$.width / $dimW_fWidth$$) * $dimW_fWidth$$, 0, $dimW_fWidth$$, $dimH$$, $x$jscomp$149$$, $y$jscomp$129$$, $dimW_fWidth$$, $dimH$$);
  }
  function $drawAnimatedIconWithFrames$$($icon$jscomp$2_iconPath$jscomp$1$$, $nFrames$jscomp$2$$, $delay$jscomp$3$$, $x$jscomp$150$$, $y$jscomp$130$$) {
    $icon$jscomp$2_iconPath$jscomp$1$$ = $stendhal$$.data.$sprites$.get($icon$jscomp$2_iconPath$jscomp$1$$);
    var $xdim$jscomp$inline_815$$ = $icon$jscomp$2_iconPath$jscomp$1$$.width / $nFrames$jscomp$2$$, $ydim$jscomp$inline_816$$ = $icon$jscomp$2_iconPath$jscomp$1$$.height;
    $ctx$jscomp$31$$.drawImage($icon$jscomp$2_iconPath$jscomp$1$$, Math.floor(Date.now() / $delay$jscomp$3$$) % $nFrames$jscomp$2$$ * $xdim$jscomp$inline_815$$, 0, $xdim$jscomp$inline_815$$, $ydim$jscomp$inline_816$$, $x$jscomp$150$$, $y$jscomp$130$$, $xdim$jscomp$inline_815$$, $ydim$jscomp$inline_816$$);
  }
  var $nextX$jscomp$1_x$jscomp$147$$ = 32 * $JSCompiler_StaticMethods_drawStatusIcons$self$$._x - 10, $y$jscomp$127$$ = 32 * ($JSCompiler_StaticMethods_drawStatusIcons$self$$._y + 1);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("choking") ? $ctx$jscomp$31$$.drawImage($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/ideas/choking.png"), $nextX$jscomp$1_x$jscomp$147$$, $y$jscomp$127$$ - 10) : $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("eating") && $ctx$jscomp$31$$.drawImage($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/ideas/eat.png"), $nextX$jscomp$1_x$jscomp$147$$, $y$jscomp$127$$ - 10);
  if ($JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("idea")) {
    var $idea$$ = $stendhal$$.$paths$.$sprites$ + "/ideas/" + $JSCompiler_StaticMethods_drawStatusIcons$self$$.idea + ".png", $ani$jscomp$1$$ = $stendhal$$.data.$sprites$.$animations$.$idea$[$JSCompiler_StaticMethods_drawStatusIcons$self$$.idea];
    $ani$jscomp$1$$ ? $drawAnimatedIcon$$($idea$$, $ani$jscomp$1$$.delay, $nextX$jscomp$1_x$jscomp$147$$ + $ani$jscomp$1$$.offsetX * $JSCompiler_StaticMethods_drawStatusIcons$self$$.width, $y$jscomp$127$$ - $JSCompiler_StaticMethods_drawStatusIcons$self$$.drawHeight + $ani$jscomp$1$$.offsetY) : $ctx$jscomp$31$$.drawImage($stendhal$$.data.$sprites$.get($idea$$), $nextX$jscomp$1_x$jscomp$147$$ + 32 * $JSCompiler_StaticMethods_drawStatusIcons$self$$.width, $y$jscomp$127$$ - $JSCompiler_StaticMethods_drawStatusIcons$self$$.drawHeight);
  }
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("away") && $drawAnimatedIcon$$($stendhal$$.$paths$.$sprites$ + "/ideas/away.png", 1500, $nextX$jscomp$1_x$jscomp$147$$ + 32 * $JSCompiler_StaticMethods_drawStatusIcons$self$$.width, $y$jscomp$127$$ - $JSCompiler_StaticMethods_drawStatusIcons$self$$.drawHeight);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("grumpy") && $drawAnimatedIcon$$($stendhal$$.$paths$.$sprites$ + "/ideas/grumpy.png", 1000, $nextX$jscomp$1_x$jscomp$147$$ + 5, $y$jscomp$127$$ - $JSCompiler_StaticMethods_drawStatusIcons$self$$.drawHeight);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("last_player_kill_time") && $drawAnimatedIconWithFrames$$($stendhal$$.$paths$.$sprites$ + "/ideas/pk.png", 12, 300, $nextX$jscomp$1_x$jscomp$147$$, $y$jscomp$127$$ - $JSCompiler_StaticMethods_drawStatusIcons$self$$.drawHeight);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("poisoned") && $drawAnimatedIcon$$($stendhal$$.$paths$.$sprites$ + "/status/poison.png", 100, $nextX$jscomp$1_x$jscomp$147$$ + 32 * $JSCompiler_StaticMethods_drawStatusIcons$self$$.width - 10, $y$jscomp$127$$ - $JSCompiler_StaticMethods_drawStatusIcons$self$$.drawHeight);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("status_confuse") && $drawAnimatedIcon$$($stendhal$$.$paths$.$sprites$ + "/status/confuse.png", 200, $nextX$jscomp$1_x$jscomp$147$$ + 32 * $JSCompiler_StaticMethods_drawStatusIcons$self$$.width - 14, $y$jscomp$127$$ - $JSCompiler_StaticMethods_drawStatusIcons$self$$.drawHeight + 16);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("status_shock") && $drawAnimatedIcon$$($stendhal$$.$paths$.$sprites$ + "/status/shock.png", 200, $nextX$jscomp$1_x$jscomp$147$$ + 32 * $JSCompiler_StaticMethods_drawStatusIcons$self$$.width - 25, $y$jscomp$127$$ - 32, 38);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("job_healer") && ($ctx$jscomp$31$$.drawImage($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/status/healer.png"), $nextX$jscomp$1_x$jscomp$147$$, $y$jscomp$127$$ - 10), $nextX$jscomp$1_x$jscomp$147$$ += 12);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("job_merchant") && ($ctx$jscomp$31$$.drawImage($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/status/merchant.png"), $nextX$jscomp$1_x$jscomp$147$$, $y$jscomp$127$$ - 10), $nextX$jscomp$1_x$jscomp$147$$ += 12);
  $JSCompiler_StaticMethods_drawStatusIcons$self$$.hasOwnProperty("job_producer") && ($ctx$jscomp$31$$.drawImage($stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/status/producer.png"), $nextX$jscomp$1_x$jscomp$147$$, $y$jscomp$127$$ - 16), $nextX$jscomp$1_x$jscomp$147$$ += 16);
}
function $JSCompiler_StaticMethods_drawSpriteImage$$($JSCompiler_StaticMethods_drawSpriteImage$self$$, $ctx$jscomp$34$$, $image$jscomp$12$$) {
  var $localX$jscomp$1$$ = 32 * $JSCompiler_StaticMethods_drawSpriteImage$self$$._x, $localY$jscomp$1$$ = 32 * $JSCompiler_StaticMethods_drawSpriteImage$self$$._y;
  if ($image$jscomp$12$$.height) {
    var $drawY$jscomp$2_nFrames$jscomp$3$$ = 3, $drawX$jscomp$2_nDirections$$ = 4, $yRow$jscomp$1$$ = $JSCompiler_StaticMethods_drawSpriteImage$self$$.dir - 1, $frame$jscomp$4$$ = 1;
    "ent" == $JSCompiler_StaticMethods_drawSpriteImage$self$$["class"] && ($drawY$jscomp$2_nFrames$jscomp$3$$ = 1, $drawX$jscomp$2_nDirections$$ = 2, $yRow$jscomp$1$$ = Math.floor(($JSCompiler_StaticMethods_drawSpriteImage$self$$.dir - 1) / 2), $frame$jscomp$4$$ = 0);
    $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawHeight = $image$jscomp$12$$.height / $drawX$jscomp$2_nDirections$$;
    $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawWidth = $image$jscomp$12$$.width / $drawY$jscomp$2_nFrames$jscomp$3$$;
    $drawX$jscomp$2_nDirections$$ = (32 * $JSCompiler_StaticMethods_drawSpriteImage$self$$.width - $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawWidth) / 2;
    if ((0 < $JSCompiler_StaticMethods_drawSpriteImage$self$$.speed || $JSCompiler_StaticMethods_drawSpriteImage$self$$.hasOwnProperty("active_idle")) && 1 != $drawY$jscomp$2_nFrames$jscomp$3$$) {
      var $animLength_opacity$$ = 2 * $drawY$jscomp$2_nFrames$jscomp$3$$ - 2;
      $frame$jscomp$4$$ = Math.floor(Date.now() / 100) % $animLength_opacity$$;
      $frame$jscomp$4$$ >= $drawY$jscomp$2_nFrames$jscomp$3$$ && ($frame$jscomp$4$$ = $animLength_opacity$$ - $frame$jscomp$4$$);
    }
    $drawY$jscomp$2_nFrames$jscomp$3$$ = 32 * $JSCompiler_StaticMethods_drawSpriteImage$self$$.height - $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawHeight;
    $animLength_opacity$$ = parseInt($JSCompiler_StaticMethods_drawSpriteImage$self$$.visibility, 10);
    $animLength_opacity$$ = isNaN($animLength_opacity$$) ? 100 : $animLength_opacity$$;
    $JSCompiler_StaticMethods_drawSpriteImage$self$$.hasOwnProperty("ghostmode") && $JSCompiler_StaticMethods_drawSpriteImage$self$$ === $marauroa$$.$me$ && 50 < $animLength_opacity$$ && ($animLength_opacity$$ = 50);
    var $opacity_orig$$ = $ctx$jscomp$34$$.globalAlpha;
    100 > $animLength_opacity$$ && ($ctx$jscomp$34$$.globalAlpha = 0.01 * $animLength_opacity$$);
    $ctx$jscomp$34$$.drawImage($image$jscomp$12$$, $frame$jscomp$4$$ * $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawWidth, $yRow$jscomp$1$$ * $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawHeight, $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawWidth, $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawHeight, $localX$jscomp$1$$ + $drawX$jscomp$2_nDirections$$, $localY$jscomp$1$$ + $drawY$jscomp$2_nFrames$jscomp$3$$, $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawWidth, 
    $JSCompiler_StaticMethods_drawSpriteImage$self$$.drawHeight);
    $ctx$jscomp$34$$.globalAlpha = $opacity_orig$$;
  }
}
$JSCompiler_prototypeAlias$$.$drawTop$ = function($ctx$jscomp$35$$) {
  for (var $localX$jscomp$2$$ = 32 * this._x, $localY$jscomp$2$$ = 32 * this._y, $centerX$jscomp$inline_822$$ = 32 * (this._x + this.width / 2), $topY$jscomp$inline_823$$ = 32 * (this._y + 1) - this.drawHeight, $currentFloaters$jscomp$inline_824$$ = this.$B$, $i$jscomp$inline_825$$ = 0; $i$jscomp$inline_825$$ < $currentFloaters$jscomp$inline_824$$.length; $i$jscomp$inline_825$$++) {
    var $floater$jscomp$inline_826$$ = $currentFloaters$jscomp$inline_824$$[$i$jscomp$inline_825$$];
    $floater$jscomp$inline_826$$.$draw$($ctx$jscomp$35$$, $centerX$jscomp$inline_822$$, $topY$jscomp$inline_823$$) && (this.$B$ = this.$B$.slice(), this.$B$.splice(this.$B$.indexOf($floater$jscomp$inline_826$$), 1));
  }
  $JSCompiler_StaticMethods_drawHealthBar$$(this, $ctx$jscomp$35$$, $localX$jscomp$2$$, $localY$jscomp$2$$ + this.$D$);
  $JSCompiler_StaticMethods_drawTitle$$(this, $ctx$jscomp$35$$, $localX$jscomp$2$$, $localY$jscomp$2$$ + this.$D$);
};
function $JSCompiler_StaticMethods_drawHealthBar$$($JSCompiler_StaticMethods_drawHealthBar$self$$, $ctx$jscomp$36$$, $drawX$jscomp$3_x$jscomp$151$$, $drawY$jscomp$3_y$jscomp$131$$) {
  $drawX$jscomp$3_x$jscomp$151$$ += (32 * $JSCompiler_StaticMethods_drawHealthBar$self$$.width - $JSCompiler_StaticMethods_drawHealthBar$self$$.drawWidth) / 2;
  $drawY$jscomp$3_y$jscomp$131$$ = $drawY$jscomp$3_y$jscomp$131$$ + 32 * $JSCompiler_StaticMethods_drawHealthBar$self$$.height - $JSCompiler_StaticMethods_drawHealthBar$self$$.drawHeight - 6 + $JSCompiler_StaticMethods_drawHealthBar$self$$.$F$;
  $ctx$jscomp$36$$.strokeStyle = "#000000";
  $ctx$jscomp$36$$.lineWidth = 2;
  $ctx$jscomp$36$$.beginPath();
  $ctx$jscomp$36$$.rect($drawX$jscomp$3_x$jscomp$151$$, $drawY$jscomp$3_y$jscomp$131$$, $JSCompiler_StaticMethods_drawHealthBar$self$$.drawWidth, 4);
  $ctx$jscomp$36$$.stroke();
  $ctx$jscomp$36$$.fillStyle = "#808080";
  $ctx$jscomp$36$$.fillRect($drawX$jscomp$3_x$jscomp$151$$, $drawY$jscomp$3_y$jscomp$131$$, $JSCompiler_StaticMethods_drawHealthBar$self$$.drawWidth, 4);
  var $hpRatio$$ = $JSCompiler_StaticMethods_drawHealthBar$self$$.hp / $JSCompiler_StaticMethods_drawHealthBar$self$$.base_hp;
  $ctx$jscomp$36$$.fillStyle = $module$build$ts$util$Color$default$$.$Color$.$F$($hpRatio$$);
  $ctx$jscomp$36$$.fillRect($drawX$jscomp$3_x$jscomp$151$$, $drawY$jscomp$3_y$jscomp$131$$, $JSCompiler_StaticMethods_drawHealthBar$self$$.drawWidth * $hpRatio$$, 4);
}
$JSCompiler_prototypeAlias$$.$createTitleTextSprite$ = function() {
  var $title$jscomp$22$$ = this.title;
  $title$jscomp$22$$ || ($title$jscomp$22$$ = this._name, $title$jscomp$22$$ || ($title$jscomp$22$$ = this["class"]) || ($title$jscomp$22$$ = this.type));
  $title$jscomp$22$$ && (this.$I$ = new $module$build$ts$sprite$TextSprite$default$$.$TextSprite$($title$jscomp$22$$, this.$G$, "14px sans-serif"));
};
function $JSCompiler_StaticMethods_drawTitle$$($JSCompiler_StaticMethods_drawTitle$self$$, $ctx$jscomp$37$$, $x$jscomp$152$$, $y$jscomp$132$$) {
  if ($JSCompiler_StaticMethods_drawTitle$self$$.$I$) {
    var $textMetrics$$ = $JSCompiler_StaticMethods_getTextMetrics$$($JSCompiler_StaticMethods_drawTitle$self$$.$I$, $ctx$jscomp$37$$);
    $JSCompiler_StaticMethods_drawTitle$self$$.$I$.$draw$($ctx$jscomp$37$$, $x$jscomp$152$$ + (32 * $JSCompiler_StaticMethods_drawTitle$self$$.width - $textMetrics$$.width) / 2, $y$jscomp$132$$ + 32 * $JSCompiler_StaticMethods_drawTitle$self$$.height - $JSCompiler_StaticMethods_drawTitle$self$$.drawHeight - 6 + $JSCompiler_StaticMethods_drawTitle$self$$.$F$ - 5 - 6);
  }
}
function $JSCompiler_StaticMethods_getAttackTarget$$($JSCompiler_StaticMethods_getAttackTarget$self$$) {
  !$JSCompiler_StaticMethods_getAttackTarget$self$$.$_target$ && $JSCompiler_StaticMethods_getAttackTarget$self$$.target && ($JSCompiler_StaticMethods_getAttackTarget$self$$.$_target$ = $marauroa$$.$currentZone$[$JSCompiler_StaticMethods_getAttackTarget$self$$.target], $JSCompiler_StaticMethods_getAttackTarget$self$$.$_target$ && $JSCompiler_StaticMethods_onTargeted$$($JSCompiler_StaticMethods_getAttackTarget$self$$.$_target$, $JSCompiler_StaticMethods_getAttackTarget$self$$));
  return $JSCompiler_StaticMethods_getAttackTarget$self$$.$_target$;
}
function $JSCompiler_StaticMethods_addFloater$$($JSCompiler_StaticMethods_addFloater$self$$, $message$jscomp$36$$, $color$jscomp$8$$) {
  $JSCompiler_StaticMethods_addFloater$self$$.$B$.push(new $module$build$ts$sprite$Floater$default$$.$Floater$($message$jscomp$36$$, $color$jscomp$8$$));
}
function $JSCompiler_StaticMethods_createResultIcon$$($imagePath$$) {
  return {$initTime$:Date.now(), image:$stendhal$$.data.$sprites$.get($imagePath$$), $draw$:function($ctx$jscomp$39$$, $x$jscomp$153$$, $y$jscomp$133$$) {
    $ctx$jscomp$39$$.drawImage(this.image, $x$jscomp$153$$, $y$jscomp$133$$);
    return 1200 < Date.now() - this.$initTime$;
  }};
}
function $JSCompiler_StaticMethods_onTargeted$$($JSCompiler_StaticMethods_onTargeted$self$$, $attacker$$) {
  $attacker$$.id in $JSCompiler_StaticMethods_onTargeted$self$$.$v$ || ($JSCompiler_StaticMethods_onTargeted$self$$.$v$[$attacker$$.id] = !0, $JSCompiler_StaticMethods_onTargeted$self$$.$v$.size += 1);
}
function $JSCompiler_StaticMethods_onAttackStopped$$($JSCompiler_StaticMethods_onAttackStopped$self$$, $attacker$jscomp$1$$) {
  $attacker$jscomp$1$$.id in $JSCompiler_StaticMethods_onAttackStopped$self$$.$v$ && (delete $JSCompiler_StaticMethods_onAttackStopped$self$$.$v$[$attacker$jscomp$1$$.id], --$JSCompiler_StaticMethods_onAttackStopped$self$$.$v$.size);
}
$JSCompiler_prototypeAlias$$.$destroy$ = function() {
  this.$_target$ && $JSCompiler_StaticMethods_onAttackStopped$$(this.$_target$, this);
};
$module$build$ts$entity$RPEntity$default$$.$RPEntity$.$v$ = $module$build$ts$ui$SoundManager$default$$.$SoundManager$.get();
var $module$build$ts$entity$Player$default$$ = {};
Object.defineProperty($module$build$ts$entity$Player$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Player$default$$.$Player$ = void 0;
$module$build$ts$entity$Player$default$$.$Player$ = function() {
  var $$jscomp$super$this$jscomp$45$$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$45$$.$minimapShow$ = !0;
  $$jscomp$super$this$jscomp$45$$.$minimapStyle$ = $module$build$ts$util$Color$default$$.$Color$.$C$;
  $$jscomp$super$this$jscomp$45$$.dir = 3;
  $$jscomp$super$this$jscomp$45$$.$F$ = 6;
  return $$jscomp$super$this$jscomp$45$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Player$default$$.$Player$, $module$build$ts$entity$RPEntity$default$$.$RPEntity$);
$module$build$ts$entity$Player$default$$.$Player$.$v$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.$v$;
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Player$default$$.$Player$.prototype;
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$90$$, $value$jscomp$127$$) {
  $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype.set.call(this, $key$jscomp$90$$, $value$jscomp$127$$);
  "ghostmode" === $key$jscomp$90$$ && (this.$minimapShow$ = !1);
};
$JSCompiler_prototypeAlias$$.$createTitleTextSprite$ = function() {
  $JSCompiler_StaticMethods_isAdmin$$(this) && (this.$G$ = "#FFFF00");
  $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype.$createTitleTextSprite$.call(this);
};
function $JSCompiler_StaticMethods_isAdmin$$($JSCompiler_StaticMethods_isAdmin$self$$) {
  return "undefined" !== typeof $JSCompiler_StaticMethods_isAdmin$self$$.adminlevel && 600 < $JSCompiler_StaticMethods_isAdmin$self$$.adminlevel;
}
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$3$$) {
  var $$jscomp$this$jscomp$38$$ = this;
  $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype.$buildActions$.call(this, $list$jscomp$3$$);
  var $playerName$jscomp$1$$ = this._name, $isUnknown$$ = $marauroa$$.$me$ !== this && (null == $marauroa$$.$me$.buddies || !($playerName$jscomp$1$$ in $marauroa$$.$me$.buddies));
  $isUnknown$$ && $list$jscomp$3$$.push({title:"Add to buddies", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"addbuddy", zone:$marauroa$$.$currentZoneName$, target:$playerName$jscomp$1$$});
  }});
  $JSCompiler_StaticMethods_isIgnored$$(this) ? $list$jscomp$3$$.push({title:"Remove ignore", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"unignore", zone:$marauroa$$.$currentZoneName$, target:$playerName$jscomp$1$$});
  }}) : $isUnknown$$ && $list$jscomp$3$$.push({title:"Ignore", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"ignore", zone:$marauroa$$.$currentZoneName$, target:$playerName$jscomp$1$$});
  }});
  $marauroa$$.$me$ !== this && ($list$jscomp$3$$.push({title:"Trade", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"trade", action:"offer_trade", zone:$marauroa$$.$currentZoneName$, target:$playerName$jscomp$1$$});
  }}), $JSCompiler_StaticMethods_canInviteToGroup$$() && $list$jscomp$3$$.push({title:"Invite", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"group_management", action:"invite", params:$$jscomp$this$jscomp$38$$.name});
  }}));
};
function $JSCompiler_StaticMethods_isIgnored$$($JSCompiler_StaticMethods_isIgnored$self$$) {
  if (!$marauroa$$.$me$ || !$marauroa$$.$me$["!ignore"]) {
    return !1;
  }
  var $temp$jscomp$3$$ = $marauroa$$.$me$["!ignore"].$_objects$;
  return 0 < $temp$jscomp$3$$.length && "_" + $JSCompiler_StaticMethods_isIgnored$self$$._name in $temp$jscomp$3$$[0];
}
$JSCompiler_prototypeAlias$$.$onMiniMapDraw$ = function() {
  this !== $marauroa$$.$me$ && (this.$minimapStyle$ = $stendhal$$.data.group.$members$[this.name] ? "rgb(99, 61, 139)" : $module$build$ts$util$Color$default$$.$Color$.$C$);
};
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$40$$) {
  $JSCompiler_StaticMethods_isIgnored$$(this) || $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype.$draw$.call(this, $ctx$jscomp$40$$);
};
$JSCompiler_prototypeAlias$$.$getResistance$ = function() {
  return "undefined" !== typeof this.ghostmode ? 0 : this.resistance;
};
$JSCompiler_prototypeAlias$$.$say$ = function($text$jscomp$25$$, $rangeSquared$jscomp$2$$) {
  $JSCompiler_StaticMethods_isIgnored$$(this) || $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype.$say$.call(this, $text$jscomp$25$$, $rangeSquared$jscomp$2$$);
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  return this.$isVisibleToAction$() ? "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/look.png) 1 3, auto" : "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/walk.png) 1 3, auto";
};
var $module$build$ts$ui$component$MiniMapComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$MiniMapComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$MiniMapComponent$default$$.$MiniMapComponent$ = void 0;
$module$build$ts$ui$component$MiniMapComponent$default$$.$MiniMapComponent$ = function() {
  var $$jscomp$super$this$jscomp$46$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "minimap") || this;
  $$jscomp$super$this$jscomp$46$$.width = 128;
  $$jscomp$super$this$jscomp$46$$.height = 128;
  $$jscomp$super$this$jscomp$46$$.$H$ = 2;
  $$jscomp$super$this$jscomp$46$$.$A$ = 1;
  $$jscomp$super$this$jscomp$46$$.$B$ = 1;
  $$jscomp$super$this$jscomp$46$$.$D$ = 1;
  $$jscomp$super$this$jscomp$46$$.$C$ = 1;
  $$jscomp$super$this$jscomp$46$$.scale = 1;
  $$jscomp$super$this$jscomp$46$$.$componentElement$.addEventListener("click", function($event$jscomp$66$$) {
    $JSCompiler_StaticMethods_JSC$2219_onClick$$($$jscomp$super$this$jscomp$46$$, $event$jscomp$66$$);
  });
  $$jscomp$super$this$jscomp$46$$.$componentElement$.addEventListener("dblclick", function($event$jscomp$67$$) {
    $JSCompiler_StaticMethods_JSC$2219_onClick$$($$jscomp$super$this$jscomp$46$$, $event$jscomp$67$$);
  });
  return $$jscomp$super$this$jscomp$46$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$MiniMapComponent$default$$.$MiniMapComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$MiniMapComponent$default$$.$MiniMapComponent$.prototype.$zoneChange$ = function() {
  this.$D$ = $stendhal$$.data.map.$zoneSizeX$;
  this.$C$ = $stendhal$$.data.map.$zoneSizeY$;
  this.scale = Math.max(this.$H$, Math.min(this.height / this.$C$, this.width / this.$D$));
  var $width$jscomp$inline_829$$ = this.$D$, $height$jscomp$inline_830$$ = this.$C$;
  if (!(0 >= $width$jscomp$inline_829$$ || 0 >= $height$jscomp$inline_830$$) && $stendhal$$.data.map.$collisionData$ !== this.$G$) {
    this.$G$ = $stendhal$$.data.map.$collisionData$;
    this.$v$ = document.createElement("canvas");
    for (var $ctx$jscomp$inline_831$$ = this.$v$.getContext("2d"), $imgData$jscomp$inline_832$$ = $ctx$jscomp$inline_831$$.createImageData($width$jscomp$inline_829$$, $height$jscomp$inline_830$$), $y$jscomp$inline_833$$ = 0; $y$jscomp$inline_833$$ < $height$jscomp$inline_830$$; $y$jscomp$inline_833$$++) {
      for (var $x$jscomp$inline_834$$ = 0; $x$jscomp$inline_834$$ < $width$jscomp$inline_829$$; $x$jscomp$inline_834$$++) {
        var $pos$jscomp$inline_835$$ = 4 * ($y$jscomp$inline_833$$ * $width$jscomp$inline_829$$ + $x$jscomp$inline_834$$);
        if ($JSCompiler_StaticMethods_collision$$($x$jscomp$inline_834$$, $y$jscomp$inline_833$$)) {
          $imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$] = 255;
        } else {
          var $JSCompiler_StaticMethods_isProtected$self$jscomp$inline_1121$$ = $stendhal$$.data.map;
          0 != $JSCompiler_StaticMethods_isProtected$self$jscomp$inline_1121$$.$C$[$y$jscomp$inline_833$$ * $JSCompiler_StaticMethods_isProtected$self$jscomp$inline_1121$$.$zoneSizeX$ + $x$jscomp$inline_834$$] ? ($imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$] = 202, $imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$ + 1] = 230, $imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$ + 2] = 202) : ($imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$] = 224, $imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$ + 
          1] = 224, $imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$ + 2] = 224);
        }
        $imgData$jscomp$inline_832$$.data[$pos$jscomp$inline_835$$ + 3] = 255;
      }
    }
    this.$v$.width = $width$jscomp$inline_829$$;
    this.$v$.height = $height$jscomp$inline_830$$;
    $ctx$jscomp$inline_831$$.putImageData($imgData$jscomp$inline_832$$, 0, 0);
  }
};
$module$build$ts$ui$component$MiniMapComponent$default$$.$MiniMapComponent$.prototype.$draw$ = function() {
  if ($marauroa$$.$currentZoneName$ === $stendhal$$.data.map.$currentZoneName$ || "int_vault" === $stendhal$$.data.map.$currentZoneName$ || "int_adventure_island" === $stendhal$$.data.map.$currentZoneName$ || "tutorial_island" === $stendhal$$.data.map.$currentZoneName$) {
    this.scale = 10;
    this.$zoneChange$();
    if ($marauroa$$.$me$) {
      this.$B$ = this.$A$ = 0;
      var $ctx$jscomp$41_imageWidth$jscomp$inline_838$$ = this.$D$ * this.scale, $imageHeight$jscomp$inline_839$$ = this.$C$ * this.scale, $xpos$jscomp$inline_840$$ = Math.round($marauroa$$.$me$.x * this.scale + 0.5) - this.width / 2, $ypos$jscomp$inline_841$$ = Math.round($marauroa$$.$me$.y * this.scale + 0.5) - this.width / 2;
      $ctx$jscomp$41_imageWidth$jscomp$inline_838$$ > this.width && ($xpos$jscomp$inline_840$$ + this.width > $ctx$jscomp$41_imageWidth$jscomp$inline_838$$ ? this.$A$ = $ctx$jscomp$41_imageWidth$jscomp$inline_838$$ - this.width : 0 < $xpos$jscomp$inline_840$$ && (this.$A$ = $xpos$jscomp$inline_840$$));
      $imageHeight$jscomp$inline_839$$ > this.height && ($ypos$jscomp$inline_841$$ + this.height > $imageHeight$jscomp$inline_839$$ ? this.$B$ = $imageHeight$jscomp$inline_839$$ - this.height : 0 < $ypos$jscomp$inline_841$$ && (this.$B$ = $ypos$jscomp$inline_841$$));
    }
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$ = this.$componentElement$.getContext("2d");
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.setTransform(1, 0, 0, 1, 0, 0);
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.fillStyle = "#606060";
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.fillRect(0, 0, this.width, this.height);
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.translate(Math.round(-this.$A$), Math.round(-this.$B$));
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.save();
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.imageSmoothingEnabled = !1;
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.scale(this.scale, this.scale);
    this.$v$ && $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.drawImage(this.$v$, 0, 0);
    $ctx$jscomp$41_imageWidth$jscomp$inline_838$$.restore();
    this.$drawEntities$($ctx$jscomp$41_imageWidth$jscomp$inline_838$$);
  }
};
$module$build$ts$ui$component$MiniMapComponent$default$$.$MiniMapComponent$.prototype.$drawEntities$ = function($ctx$jscomp$44$$) {
  $ctx$jscomp$44$$.fillStyle = "rgb(255,0,0)";
  $ctx$jscomp$44$$.strokeStyle = "rgb(0,0,0)";
  var $isAdmin$$ = $marauroa$$.$me$.adminlevel && 600 <= $marauroa$$.$me$.adminlevel, $i$jscomp$91$$;
  for ($i$jscomp$91$$ in $marauroa$$.$currentZone$) {
    var $o$jscomp$7_vc$$ = $marauroa$$.$currentZone$[$i$jscomp$91$$];
    if ("undefined" != typeof $o$jscomp$7_vc$$.x && "undefined" != typeof $o$jscomp$7_vc$$.y && ($o$jscomp$7_vc$$.$minimapShow$ || $isAdmin$$)) {
      if ($o$jscomp$7_vc$$.$onMiniMapDraw$(), $o$jscomp$7_vc$$.$minimapStyle$ ? $ctx$jscomp$44$$.strokeStyle = $o$jscomp$7_vc$$.$minimapStyle$ : $ctx$jscomp$44$$.strokeStyle = "rgb(128, 128, 128)", $o$jscomp$7_vc$$ instanceof $module$build$ts$entity$Player$default$$.$Player$) {
        var $adj_scale_vo$$ = this.scale;
        6 > $adj_scale_vo$$ && ($adj_scale_vo$$ = 6);
        var $ho$$ = $o$jscomp$7_vc$$.width * $adj_scale_vo$$ / 2;
        $adj_scale_vo$$ = $o$jscomp$7_vc$$.height * $adj_scale_vo$$ / 2;
        var $hc$$ = $o$jscomp$7_vc$$.x * this.scale + $ho$$;
        $o$jscomp$7_vc$$ = $o$jscomp$7_vc$$.y * this.scale + $adj_scale_vo$$;
        $ctx$jscomp$44$$.beginPath();
        $ctx$jscomp$44$$.moveTo($hc$$ - $ho$$, $o$jscomp$7_vc$$);
        $ctx$jscomp$44$$.lineTo($hc$$ + $ho$$, $o$jscomp$7_vc$$);
        $ctx$jscomp$44$$.moveTo($hc$$, $o$jscomp$7_vc$$ - $adj_scale_vo$$);
        $ctx$jscomp$44$$.lineTo($hc$$, $o$jscomp$7_vc$$ + $adj_scale_vo$$);
        $ctx$jscomp$44$$.stroke();
        $ctx$jscomp$44$$.closePath();
      } else {
        $ctx$jscomp$44$$.strokeRect($o$jscomp$7_vc$$.x * this.scale, $o$jscomp$7_vc$$.y * this.scale, $o$jscomp$7_vc$$.width * this.scale, $o$jscomp$7_vc$$.height * this.scale);
      }
    }
  }
};
function $JSCompiler_StaticMethods_JSC$2219_onClick$$($JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$, $event$jscomp$68$$) {
  if ($JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "client.pathfinding")) {
    var $pos$jscomp$16$$ = $JSCompiler_StaticMethods_extractPosition$$($event$jscomp$68$$), $action$jscomp$77_x$jscomp$155$$ = Math.floor(($pos$jscomp$16$$.$canvasRelativeX$ + $JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$.$A$) / $JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$.scale);
    $JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$ = Math.floor(($pos$jscomp$16$$.$canvasRelativeY$ + $JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$.$B$) / $JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$.scale);
    $JSCompiler_StaticMethods_collision$$($action$jscomp$77_x$jscomp$155$$, $JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$) || ($action$jscomp$77_x$jscomp$155$$ = {type:"moveto", x:$action$jscomp$77_x$jscomp$155$$.toString(), y:$JSCompiler_StaticMethods_JSC$2219_onClick$self_y$jscomp$135$$.toString()}, "type" in $event$jscomp$68$$ && "dblclick" === $event$jscomp$68$$.type && ($action$jscomp$77_x$jscomp$155$$.double_click = ""), $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$77_x$jscomp$155$$));
  }
}
;var $module$build$ts$ui$component$ZoneInfoComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$ZoneInfoComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$ZoneInfoComponent$default$$.$ZoneInfoComponent$ = void 0;
$module$build$ts$ui$component$ZoneInfoComponent$default$$.$ZoneInfoComponent$ = function() {
  var $$jscomp$super$this$jscomp$47$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "zoneinfo") || this;
  $$jscomp$super$this$jscomp$47$$.$v$ = "The area feels safe.;The area feels relatively safe.;The area feels somewhat dangerous.;The area feels dangerous.;The area feels very dangerous!;The area feels extremely dangerous. Run away!".split(";");
  return $$jscomp$super$this$jscomp$47$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$ZoneInfoComponent$default$$.$ZoneInfoComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$ZoneInfoComponent$default$$.$ZoneInfoComponent$.prototype.$zoneChange$ = function($dangerLevel_skulls_zoneinfo$$) {
  document.getElementById("zonename").textContent = $dangerLevel_skulls_zoneinfo$$.readable_name;
  if ($marauroa$$.$me$) {
    $dangerLevel_skulls_zoneinfo$$ = Number.parseFloat($dangerLevel_skulls_zoneinfo$$.danger_level);
    $dangerLevel_skulls_zoneinfo$$ = Math.min(5, Math.round(2 * $dangerLevel_skulls_zoneinfo$$ / (Number.parseInt($marauroa$$.$me$.level, 10) + 3)));
    var $div$$ = document.getElementById("skulls");
    $div$$.style.height = 0 === $dangerLevel_skulls_zoneinfo$$ ? "0" : "16px";
    $div$$.style.width = 20 * $dangerLevel_skulls_zoneinfo$$ + "px";
    this.$componentElement$.title = this.$v$[$dangerLevel_skulls_zoneinfo$$];
  }
};
var $module$build$ts$ui$component$PlayerEquipmentComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$PlayerEquipmentComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$PlayerEquipmentComponent$default$$.$PlayerEquipmentComponent$ = void 0;
$module$build$ts$ui$component$PlayerEquipmentComponent$default$$.$PlayerEquipmentComponent$ = function() {
  var $$jscomp$super$this$jscomp$48$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "equipment") || this;
  $$jscomp$super$this$jscomp$48$$.$B$ = "head lhand rhand finger armor cloak legs feet pouch".split(" ");
  $$jscomp$super$this$jscomp$48$$.$D$ = [1, 1, 1, 1, 1, 1, 1, 1, 1];
  $$jscomp$super$this$jscomp$48$$.$C$ = "slot-helmet.png slot-shield.png slot-weapon.png slot-ring.png slot-armor.png slot-cloak.png slot-legs.png slot-boots.png slot-pouch.png".split(" ");
  $$jscomp$super$this$jscomp$48$$.$A$ = !1;
  $$jscomp$super$this$jscomp$48$$.$v$ = [];
  for (var $i$jscomp$92$$ in $$jscomp$super$this$jscomp$48$$.$B$) {
    $$jscomp$super$this$jscomp$48$$.$v$.push(new $module$build$ts$ui$component$ItemContainerImplementation$default$$.$ItemContainerImplementation$(document, $$jscomp$super$this$jscomp$48$$.$B$[$i$jscomp$92$$], $$jscomp$super$this$jscomp$48$$.$D$[$i$jscomp$92$$], null, "", !1, $$jscomp$super$this$jscomp$48$$.$C$[$i$jscomp$92$$]));
  }
  $JSCompiler_StaticMethods_showPouch$$($$jscomp$super$this$jscomp$48$$, !1);
  return $$jscomp$super$this$jscomp$48$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$PlayerEquipmentComponent$default$$.$PlayerEquipmentComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$PlayerEquipmentComponent$default$$.$PlayerEquipmentComponent$.prototype.update = function() {
  for (var $features$jscomp$4_i$jscomp$93$$ in this.$v$) {
    this.$v$[$features$jscomp$4_i$jscomp$93$$].update();
  }
  this.$A$ || ($features$jscomp$4_i$jscomp$93$$ = null, null != $marauroa$$.$me$ && ($features$jscomp$4_i$jscomp$93$$ = $marauroa$$.$me$.features), null != $features$jscomp$4_i$jscomp$93$$ && null != $features$jscomp$4_i$jscomp$93$$.pouch && $JSCompiler_StaticMethods_showPouch$$(this, !0));
};
$module$build$ts$ui$component$PlayerEquipmentComponent$default$$.$PlayerEquipmentComponent$.prototype.$markDirty$ = function() {
  for (var $$jscomp$iter$77$$ = $$jscomp$makeIterator$$(this.$v$), $$jscomp$key$inv$$ = $$jscomp$iter$77$$.next(); !$$jscomp$key$inv$$.done; $$jscomp$key$inv$$ = $$jscomp$iter$77$$.next()) {
    $$jscomp$key$inv$$.value.$markDirty$();
  }
};
function $JSCompiler_StaticMethods_showPouch$$($JSCompiler_StaticMethods_showPouch$self$$, $show$jscomp$2$$) {
  var $slot$jscomp$inline_849$$ = document.getElementById("pouch0"), $prevState$jscomp$inline_850$$ = $slot$jscomp$inline_849$$.style.display;
  $slot$jscomp$inline_849$$.style.display = !0 === $show$jscomp$2$$ ? "block" : "none";
  $prevState$jscomp$inline_850$$ != $slot$jscomp$inline_849$$.style.display && (document.getElementById("equipment").style.height = $show$jscomp$2$$ ? "200px" : "160px", $JSCompiler_StaticMethods_showPouch$self$$.$A$ = $show$jscomp$2$$);
}
;var $module$build$ts$ui$component$StatusesListComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$StatusesListComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$ = void 0;
$module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$ = function() {
  var $$jscomp$super$this$jscomp$49$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "statuses-list") || this;
  $$jscomp$super$this$jscomp$49$$.active = [];
  return $$jscomp$super$this$jscomp$49$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$.prototype.update = function($$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$) {
  var $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$ = [];
  $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = $$jscomp$makeIterator$$(Object.keys($$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$));
  for (var $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$.next(); !$$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.done; $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = 
  $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$.next()) {
    $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.value, -1 !== Object.keys($module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$.$v$).indexOf($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$) ? $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$.push($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$) : 
    "poisoned" === $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ ? $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$.push("poison") : $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.startsWith("status_") && ($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.substring($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.indexOf("_") + 
    1), "" !== $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ && $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$.push($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$));
  }
  a: {
    if ($$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$.length != this.active.length) {
      $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = !0;
    } else {
      for ($$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = 0; $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ < $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$.length; $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$++) {
        if ($$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$[$$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$] !== this.active[$$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$]) {
          $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = !0;
          break a;
        }
      }
      $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = !1;
    }
  }
  if ($$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$) {
    this.active = $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$;
    $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$ = $$jscomp$makeIterator$$(Array.from(this.$componentElement$.children));
    for ($$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$.next(); !$$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$.done; $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$.next()) {
      this.$componentElement$.removeChild($$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$.value);
    }
    $$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$ = -1 !== this.active.indexOf("choking");
    $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$ = $$jscomp$makeIterator$$(this.active);
    for ($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$.next(); !$$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.done; $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = 
    $$jscomp$inline_858_$jscomp$inline_860_$jscomp$iter$78_JSCompiler_inline_result$jscomp$204_idx$jscomp$inline_854_user$$.next()) {
      if ($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.value, "eating" !== $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ || !$$jscomp$inline_857_changes$jscomp$2_choking$jscomp$inline_859$$) {
        var $iconPath$jscomp$inline_863$$ = void 0;
        -1 !== Object.keys($module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$.$v$).indexOf($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$) ? $iconPath$jscomp$inline_863$$ = $stendhal$$.$paths$.$sprites$ + $module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$.$v$[$$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$] : 
        $iconPath$jscomp$inline_863$$ = $stendhal$$.$paths$.$sprites$ + "/status/panel/" + $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ + ".png";
        $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$ = $stendhal$$.data.$sprites$.get($iconPath$jscomp$inline_863$$);
        $$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$.className = "status-icon";
        this.$componentElement$.appendChild($$jscomp$inline_861_$jscomp$key$key$jscomp$3_icon$106$jscomp$inline_864_id$jscomp$33_id$jscomp$inline_862_key$jscomp$91$$);
      }
    }
  }
};
$module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$.$v$ = {choking:"/ideas/choking.png", eating:"/ideas/eat.png"};
var $module$build$ts$ui$component$StatBarComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$StatBarComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$StatBarComponent$default$$.$StatBarComponent$ = void 0;
$module$build$ts$ui$component$StatBarComponent$default$$.$StatBarComponent$ = function($$jscomp$super$this$jscomp$50_id$jscomp$35$$) {
  $$jscomp$super$this$jscomp$50_id$jscomp$35$$ || ($$jscomp$super$this$jscomp$50_id$jscomp$35$$ = "statbar-template");
  $$jscomp$super$this$jscomp$50_id$jscomp$35$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, $$jscomp$super$this$jscomp$50_id$jscomp$35$$) || this;
  $$jscomp$super$this$jscomp$50_id$jscomp$35$$.canvas = $$jscomp$super$this$jscomp$50_id$jscomp$35$$.$componentElement$;
  $$jscomp$super$this$jscomp$50_id$jscomp$35$$.$ctx$ = $$jscomp$super$this$jscomp$50_id$jscomp$35$$.canvas.getContext("2d");
  return $$jscomp$super$this$jscomp$50_id$jscomp$35$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$StatBarComponent$default$$.$StatBarComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$StatBarComponent$default$$.$StatBarComponent$.prototype.$draw$ = function($ratio$jscomp$1$$) {
  this.$ctx$.beginPath();
  this.$ctx$.fillStyle = "#808080";
  this.$ctx$.fillRect(0, 0, this.canvas.width, this.canvas.height);
  this.$ctx$.fillStyle = $module$build$ts$util$Color$default$$.$Color$.$F$($ratio$jscomp$1$$);
  this.$ctx$.fillRect(0, 0, this.canvas.width * $ratio$jscomp$1$$, this.canvas.height);
};
var $module$build$ts$ui$component$KarmaBarComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$KarmaBarComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$KarmaBarComponent$default$$.$KarmaBarComponent$ = void 0;
$module$build$ts$ui$component$KarmaBarComponent$default$$.$KarmaBarComponent$ = function() {
  var $$jscomp$super$this$jscomp$51$$ = $module$build$ts$ui$component$StatBarComponent$default$$.$StatBarComponent$.call(this, "karmabar") || this;
  $$jscomp$super$this$jscomp$51$$.$v$ = 0;
  $$jscomp$super$this$jscomp$51$$.$B$ = 0;
  $$jscomp$super$this$jscomp$51$$.$C$ = 0.5;
  $$jscomp$super$this$jscomp$51$$.$A$ = !1;
  return $$jscomp$super$this$jscomp$51$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$KarmaBarComponent$default$$.$KarmaBarComponent$, $module$build$ts$ui$component$StatBarComponent$default$$.$StatBarComponent$);
$module$build$ts$ui$component$KarmaBarComponent$default$$.$KarmaBarComponent$.prototype.$draw$ = function($grad_newKarma$$) {
  var $cycleTime$jscomp$1$$ = Date.now();
  this.$A$ ? $grad_newKarma$$ != this.$v$ ? (this.canvas.style.setProperty("outline", "1px solid #ffffff"), this.$v$ = $grad_newKarma$$, this.canvas.title = $JSCompiler_StaticMethods_describeKarma$$($grad_newKarma$$), $JSCompiler_StaticMethods_calculateRepresentation$$(this, $grad_newKarma$$), this.$B$ = $cycleTime$jscomp$1$$) : 1000 <= $cycleTime$jscomp$1$$ - this.$B$ && this.canvas.style.setProperty("outline", "1px solid #000000") : (this.$v$ = $grad_newKarma$$, this.canvas.title = $JSCompiler_StaticMethods_describeKarma$$($grad_newKarma$$), 
  $JSCompiler_StaticMethods_calculateRepresentation$$(this, $grad_newKarma$$), this.$A$ = !0);
  this.$ctx$.beginPath();
  this.$ctx$.fillStyle = "#000000";
  this.$ctx$.fillRect(0, 0, this.canvas.width, this.canvas.height);
  $grad_newKarma$$ = this.$ctx$.createLinearGradient(0, 0, this.canvas.width, 0);
  $grad_newKarma$$.addColorStop(0, "#ff0000");
  $grad_newKarma$$.addColorStop(0.5, "#ffffff");
  $grad_newKarma$$.addColorStop(1, "#0000ff");
  this.$ctx$.fillStyle = $grad_newKarma$$;
  this.$ctx$.fillRect(0, 0, this.$C$, this.canvas.height);
};
function $JSCompiler_StaticMethods_describeKarma$$($karma$$) {
  return 499 < $karma$$ ? "You have unusually good karma" : 99 < $karma$$ ? "You have great karma" : 5 < $karma$$ ? "You have good karma" : -5 < $karma$$ ? "You have average karma" : -99 < $karma$$ ? "You have bad karma" : -499 < $karma$$ ? "You have terrible karma" : "You have disastrously bad karma";
}
function $JSCompiler_StaticMethods_calculateRepresentation$$($JSCompiler_StaticMethods_calculateRepresentation$self$$, $karma$jscomp$1$$) {
  $JSCompiler_StaticMethods_calculateRepresentation$self$$.$C$ = $JSCompiler_StaticMethods_calculateRepresentation$self$$.canvas.width * Math.max(Math.min(0.5 + Math.atan(0.02 * $karma$jscomp$1$$) / Math.PI, 1), 0);
}
;var $module$build$ts$entity$ItemMap$default$$ = {};
Object.defineProperty($module$build$ts$entity$ItemMap$default$$, "__esModule", {value:!0});
$module$build$ts$entity$ItemMap$default$$.$ItemMap$ = void 0;
var $config$$module$build$ts$entity$ItemMap$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$A$(), $defaultUse$$module$build$ts$entity$ItemMap$$ = {title:"Use", type:"use", index:0}, $$jscomp$compprop2$$ = {}, $$jscomp$compprop3$$ = {}, $$jscomp$compprop4$$ = {};
$module$build$ts$entity$ItemMap$default$$.$ItemMap$ = ($$jscomp$compprop4$$["class"] = ($$jscomp$compprop2$$.box = {cursor:"bag", actions:[{title:"Open", type:"use", index:0}]}, $$jscomp$compprop2$$.drink = {cursor:"itemuse"}, $$jscomp$compprop2$$.food = {cursor:"itemuse"}, $$jscomp$compprop2$$.scroll = {cursor:"itemuse"}, $$jscomp$compprop2$$), $$jscomp$compprop4$$.name = ($$jscomp$compprop3$$["ashen holy water"] = {actions:[$defaultUse$$module$build$ts$entity$ItemMap$$]}, $$jscomp$compprop3$$.bestiary = 
{cursor:"itemuse"}, $$jscomp$compprop3$$.bulb = {cursor:"itemuse"}, $$jscomp$compprop3$$["empty scroll"] = {actions:function($e$jscomp$88$$) {
  var $count$jscomp$41$$ = parseInt($e$jscomp$88$$.quantity, 10);
  if (1 < $count$jscomp$41$$ && $e$jscomp$88$$.$_parent$) {
    return [{title:"Mark all", index:1, action:function() {
      $marauroa$$.$clientFramework$.$sendAction$({type:"markscroll", quantity:"" + $count$jscomp$41$$});
    }}];
  }
}}, $$jscomp$compprop3$$["food mill"] = {cursor:"itemuse"}, $$jscomp$compprop3$$["metal detector"] = {cursor:"itemuse"}, $$jscomp$compprop3$$["picture in wooden frame"] = {cursor:"itemuse"}, $$jscomp$compprop3$$["rotary cutter"] = {cursor:"itemuse"}, $$jscomp$compprop3$$["scroll eraser"] = {cursor:"itemuse"}, $$jscomp$compprop3$$.seed = {cursor:"itemuse"}, $$jscomp$compprop3$$.snowglobe = {cursor:"itemuse"}, $$jscomp$compprop3$$["sugar mill"] = {cursor:"itemuse"}, $$jscomp$compprop3$$.teddy = {cursor:"itemuse"}, 
$$jscomp$compprop3$$["wedding ring"] = {cursor:"itemuse", actions:[$defaultUse$$module$build$ts$entity$ItemMap$$]}, $$jscomp$compprop3$$), $$jscomp$compprop4$$.$getCursor$ = function($$jscomp$iter$81_clazz$$, $$jscomp$key$imap_imap_name$jscomp$87$$) {
  var $cursor$jscomp$2$$ = "normal";
  $$jscomp$iter$81_clazz$$ = $$jscomp$makeIterator$$([$module$build$ts$entity$ItemMap$default$$.$ItemMap$["class"][$$jscomp$iter$81_clazz$$], $module$build$ts$entity$ItemMap$default$$.$ItemMap$.name[$$jscomp$key$imap_imap_name$jscomp$87$$]]);
  for ($$jscomp$key$imap_imap_name$jscomp$87$$ = $$jscomp$iter$81_clazz$$.next(); !$$jscomp$key$imap_imap_name$jscomp$87$$.done; $$jscomp$key$imap_imap_name$jscomp$87$$ = $$jscomp$iter$81_clazz$$.next()) {
    ($$jscomp$key$imap_imap_name$jscomp$87$$ = $$jscomp$key$imap_imap_name$jscomp$87$$.value) && $$jscomp$key$imap_imap_name$jscomp$87$$.cursor && ($cursor$jscomp$2$$ = $$jscomp$key$imap_imap_name$jscomp$87$$.cursor);
  }
  "itemuse" !== $cursor$jscomp$2$$ || $JSCompiler_StaticMethods_getBoolean$$($config$$module$build$ts$entity$ItemMap$$, "action.item.doubleclick") || ($cursor$jscomp$2$$ = "activity");
  return $cursor$jscomp$2$$;
}, $$jscomp$compprop4$$.$getActions$ = function($item$jscomp$12$$) {
  var $actions$jscomp$3$$ = [];
  if (!$item$jscomp$12$$) {
    return $actions$jscomp$3$$;
  }
  for (var $$jscomp$iter$82$$ = $$jscomp$makeIterator$$([$module$build$ts$entity$ItemMap$default$$.$ItemMap$["class"][$item$jscomp$12$$["class"]], $module$build$ts$entity$ItemMap$default$$.$ItemMap$.name[$item$jscomp$12$$.name]]), $$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$ = $$jscomp$iter$82$$.next(); !$$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$.done; $$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$ = $$jscomp$iter$82$$.next()) {
    ($$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$ = $$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$.value) && $$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$.actions && ($$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$ = "function" === typeof $$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$.actions ? $$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$.actions($item$jscomp$12$$) || [] : $$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$.actions, $actions$jscomp$3$$ = 
    $actions$jscomp$3$$.concat($$jscomp$key$imap$jscomp$1_a$jscomp$39_imap$jscomp$1$$));
  }
  return $actions$jscomp$3$$;
}, $$jscomp$compprop4$$);
var $module$build$ts$entity$Item$default$$ = {};
Object.defineProperty($module$build$ts$entity$Item$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Item$default$$.$Item$ = void 0;
$module$build$ts$entity$Item$default$$.$Item$ = function() {
  var $$jscomp$super$this$jscomp$52$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$52$$.$minimapShow$ = !1;
  $$jscomp$super$this$jscomp$52$$.$minimapStyle$ = "rgb(0,255,0)";
  $$jscomp$super$this$jscomp$52$$.zIndex = 7000;
  $$jscomp$super$this$jscomp$52$$.$v$ = 0;
  $$jscomp$super$this$jscomp$52$$.$animated$ = null;
  $$jscomp$super$this$jscomp$52$$.$B$ = null;
  $$jscomp$super$this$jscomp$52$$.$C$ = null;
  $$jscomp$super$this$jscomp$52$$.$sprite$ = {height:32, width:32};
  $$jscomp$super$this$jscomp$52$$.$A$ = new $module$build$ts$sprite$TextSprite$default$$.$TextSprite$("", "white", "10px sans-serif");
  return $$jscomp$super$this$jscomp$52$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Item$default$$.$Item$, $module$build$ts$entity$Entity$default$$.$Entity$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Item$default$$.$Item$.prototype;
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !0;
};
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$4$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.$buildActions$.call(this, $list$jscomp$4$$);
  for (var $$jscomp$iter$83$$ = $$jscomp$makeIterator$$($module$build$ts$entity$ItemMap$default$$.$ItemMap$.$getActions$(this)), $$jscomp$key$mi_mi$$ = $$jscomp$iter$83$$.next(); !$$jscomp$key$mi_mi$$.done; $$jscomp$key$mi_mi$$ = $$jscomp$iter$83$$.next()) {
    $$jscomp$key$mi_mi$$ = $$jscomp$key$mi_mi$$.value, "number" === typeof $$jscomp$key$mi_mi$$.index ? $list$jscomp$4$$.splice($$jscomp$key$mi_mi$$.index, 0, $$jscomp$key$mi_mi$$) : $list$jscomp$4$$.push($$jscomp$key$mi_mi$$);
  }
};
$JSCompiler_prototypeAlias$$.$getDefaultAction$ = function() {
  return {type:"equip", source_path:$JSCompiler_StaticMethods_getIdPath$$(this), target_path:"[" + $marauroa$$.$me$.id + "\tbag]", clicked:"", zone:$marauroa$$.$currentZoneName$};
};
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$92$$, $value$jscomp$128$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$92$$, $value$jscomp$128$$);
  if ("class" === $key$jscomp$92$$ || "subclass" === $key$jscomp$92$$) {
    this.$sprite$.filename = $stendhal$$.$paths$.$sprites$ + "/items/" + this["class"] + "/" + this.subclass + ".png";
  }
  "quantity" === $key$jscomp$92$$ && (this.$A$ = new $module$build$ts$sprite$TextSprite$default$$.$TextSprite$($JSCompiler_StaticMethods_formatQuantity$$(this), "white", "10px sans-serif"));
};
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$45$$) {
  this.$sprite$.offsetY = 32 * (this.state || 0);
  $JSCompiler_StaticMethods_stepAnimation$$(this);
  var $x$jscomp$inline_868$$ = 32 * this.x, $y$jscomp$inline_869$$ = 32 * this.y;
  if (this.$sprite$) {
    this.$drawSpriteAt$($ctx$jscomp$45$$, $x$jscomp$inline_868$$, $y$jscomp$inline_869$$);
    var $textMetrics$jscomp$inline_870$$ = $JSCompiler_StaticMethods_getTextMetrics$$(this.$A$, $ctx$jscomp$45$$);
    this.$A$.$draw$($ctx$jscomp$45$$, $x$jscomp$inline_868$$ + (32 - $textMetrics$jscomp$inline_870$$.width), $y$jscomp$inline_869$$ + 6);
  }
};
function $JSCompiler_StaticMethods_stepAnimation$$($JSCompiler_StaticMethods_stepAnimation$self$$) {
  var $currentTimeStamp$$ = +new Date();
  if (0 == $JSCompiler_StaticMethods_stepAnimation$self$$.$v$) {
    $JSCompiler_StaticMethods_stepAnimation$self$$.$v$ = $currentTimeStamp$$, $JSCompiler_StaticMethods_stepAnimation$self$$.$sprite$.offsetX = 0, $JSCompiler_StaticMethods_stepAnimation$self$$.$sprite$.offsetY = 0;
  } else {
    if (100 <= $currentTimeStamp$$ - $JSCompiler_StaticMethods_stepAnimation$self$$.$v$) {
      var $idx$jscomp$inline_873$$ = ($JSCompiler_StaticMethods_stepAnimation$self$$.$sprite$.offsetX || 0) / 32 + 1;
      null == $JSCompiler_StaticMethods_stepAnimation$self$$.$B$ && ($JSCompiler_StaticMethods_stepAnimation$self$$.$B$ = $stendhal$$.data.$sprites$.get($JSCompiler_StaticMethods_stepAnimation$self$$.$sprite$.filename).width / 32);
      $idx$jscomp$inline_873$$ >= $JSCompiler_StaticMethods_stepAnimation$self$$.$B$ && ($idx$jscomp$inline_873$$ = 0);
      $JSCompiler_StaticMethods_stepAnimation$self$$.$sprite$.offsetX = 32 * $idx$jscomp$inline_873$$;
      $JSCompiler_StaticMethods_stepAnimation$self$$.$v$ = $currentTimeStamp$$;
    }
  }
}
function $JSCompiler_StaticMethods_formatQuantity$$($JSCompiler_StaticMethods_formatQuantity$self$$) {
  return $JSCompiler_StaticMethods_formatQuantity$self$$.quantity && "1" !== $JSCompiler_StaticMethods_formatQuantity$self$$.quantity ? 10000000 < $JSCompiler_StaticMethods_formatQuantity$self$$.quantity ? Math.floor($JSCompiler_StaticMethods_formatQuantity$self$$.quantity / 1000000) + "m" : 10000 < $JSCompiler_StaticMethods_formatQuantity$self$$.quantity ? Math.floor($JSCompiler_StaticMethods_formatQuantity$self$$.quantity / 1000) + "k" : $JSCompiler_StaticMethods_formatQuantity$self$$.quantity : 
  "";
}
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  var $cursor$jscomp$3$$;
  this.$_parent$ ? $cursor$jscomp$3$$ = $module$build$ts$entity$ItemMap$default$$.$ItemMap$.$getCursor$(this["class"], this.name) : $cursor$jscomp$3$$ = "itempickupfromslot";
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/" + $cursor$jscomp$3$$ + ".png) 1 3, auto";
};
$JSCompiler_prototypeAlias$$.$isDraggable$ = function() {
  return !0;
};
var $module$build$ts$ui$component$PlayerStatsComponent$default$$ = {};
Object.defineProperty($module$build$ts$ui$component$PlayerStatsComponent$default$$, "__esModule", {value:!0});
$module$build$ts$ui$component$PlayerStatsComponent$default$$.$PlayerStatsComponent$ = void 0;
$module$build$ts$ui$component$PlayerStatsComponent$default$$.$PlayerStatsComponent$ = function() {
  var $$jscomp$super$this$jscomp$53$$ = $module$build$ts$ui$toolkit$Component$default$$.$Component$.call(this, "stats") || this;
  $$jscomp$super$this$jscomp$53$$.keys = "hp base_hp atk atk_item atk_xp def def_item def_xp xp level".split(" ");
  $$jscomp$super$this$jscomp$53$$.$B$ = 598;
  $$jscomp$super$this$jscomp$53$$.$C$ = ["pouch", "bag", "lhand", "rhand"];
  $$jscomp$super$this$jscomp$53$$.$A$ = {};
  $$jscomp$super$this$jscomp$53$$.$v$ = [$$jscomp$super$this$jscomp$53$$.$B$ + 1];
  $$jscomp$super$this$jscomp$53$$.$v$[0] = 0;
  $$jscomp$super$this$jscomp$53$$.$v$[1] = 50;
  $$jscomp$super$this$jscomp$53$$.$v$[2] = 100;
  $$jscomp$super$this$jscomp$53$$.$v$[3] = 200;
  $$jscomp$super$this$jscomp$53$$.$v$[4] = 400;
  $$jscomp$super$this$jscomp$53$$.$v$[5] = 800;
  for (var $i$jscomp$94_statuses$$ = 5; $i$jscomp$94_statuses$$ < $$jscomp$super$this$jscomp$53$$.$B$; $i$jscomp$94_statuses$$++) {
    $$jscomp$super$this$jscomp$53$$.$v$[$i$jscomp$94_statuses$$ + 1] = 100 * Math.floor((16 * $i$jscomp$94_statuses$$ + $i$jscomp$94_statuses$$ * $i$jscomp$94_statuses$$ * 5 + $i$jscomp$94_statuses$$ * $i$jscomp$94_statuses$$ * $i$jscomp$94_statuses$$ * 10 + 300) / 100);
  }
  $i$jscomp$94_statuses$$ = new $module$build$ts$ui$component$StatusesListComponent$default$$.$StatusesListComponent$();
  $JSCompiler_StaticMethods_registerComponent$$(112, $i$jscomp$94_statuses$$);
  $JSCompiler_StaticMethods_enableCharName$$($JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "ui.stats.charname"));
  $$jscomp$super$this$jscomp$53$$.$D$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$53$$, "#hptext");
  $$jscomp$super$this$jscomp$53$$.$G$ = $JSCompiler_StaticMethods_child$$($$jscomp$super$this$jscomp$53$$, "#otherstats");
  $$jscomp$super$this$jscomp$53$$.$A$.karma = new $module$build$ts$ui$component$KarmaBarComponent$default$$.$KarmaBarComponent$();
  $JSCompiler_StaticMethods_enableBar$$($$jscomp$super$this$jscomp$53$$, "karma", !1);
  $$jscomp$super$this$jscomp$53$$.$A$.hp = new $module$build$ts$ui$component$StatBarComponent$default$$.$StatBarComponent$("hpbar");
  $JSCompiler_StaticMethods_enableBar$$($$jscomp$super$this$jscomp$53$$, "hp", $JSCompiler_StaticMethods_getBoolean$$($module$build$ts$SingletonRepo$default$$.$singletons$.$A$(), "ui.stats.hpbar"));
  return $$jscomp$super$this$jscomp$53$$;
};
$$jscomp$inherits$$($module$build$ts$ui$component$PlayerStatsComponent$default$$.$PlayerStatsComponent$, $module$build$ts$ui$toolkit$Component$default$$.$Component$);
$module$build$ts$ui$component$PlayerStatsComponent$default$$.$PlayerStatsComponent$.prototype.update = function($features$jscomp$5_key$jscomp$93_object$jscomp$12$$) {
  if (!(-1 > this.keys.indexOf($features$jscomp$5_key$jscomp$93_object$jscomp$12$$))) {
    $features$jscomp$5_key$jscomp$93_object$jscomp$12$$ = $marauroa$$.$me$;
    var $JSCompiler_temp_const$jscomp$1005_hp$jscomp$inline_879_karma$jscomp$inline_876$$ = $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.karma;
    $JSCompiler_StaticMethods_isBarEnabled$$(this, "karma") && this.$A$.karma.$draw$($JSCompiler_temp_const$jscomp$1005_hp$jscomp$inline_879_karma$jscomp$inline_876$$);
    $JSCompiler_temp_const$jscomp$1005_hp$jscomp$inline_879_karma$jscomp$inline_876$$ = $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.hp;
    var $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$ = $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.base_hp;
    this.$D$.innerText = "HP: " + $JSCompiler_temp_const$jscomp$1005_hp$jscomp$inline_879_karma$jscomp$inline_876$$ + " / " + $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$;
    $JSCompiler_StaticMethods_isBarEnabled$$(this, "hp") && this.$A$.hp.$draw$($JSCompiler_temp_const$jscomp$1005_hp$jscomp$inline_879_karma$jscomp$inline_876$$ / $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$);
    $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$ = $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.atk;
    var $def$jscomp$inline_885_mo$jscomp$inline_1124$$ = $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.def, $$jscomp$inline_1125_lvl$jscomp$inline_886$$ = parseInt($features$jscomp$5_key$jscomp$93_object$jscomp$12$$.level, 10), $$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$ = $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.xp;
    $JSCompiler_temp_const$jscomp$1005_hp$jscomp$inline_879_karma$jscomp$inline_876$$ = this.$G$;
    $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$ = "ATK: " + $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$ + " x " + $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.atk_item + "\r\n  (" + ($JSCompiler_StaticMethods_getReqXP$$(this, $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$ - 9) - $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.atk_xp) + ")\r\nDEF: " + $def$jscomp$inline_885_mo$jscomp$inline_1124$$ + 
    " x " + $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.def_item + "\r\n  (" + ($JSCompiler_StaticMethods_getReqXP$$(this, $def$jscomp$inline_885_mo$jscomp$inline_1124$$ - 9) - $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.def_xp) + ")\r\nXP: " + $$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$ + "\r\nLevel: " + $$jscomp$inline_1125_lvl$jscomp$inline_886$$ + "\r\n  (" + ($$jscomp$inline_1125_lvl$jscomp$inline_886$$ < this.$B$ - 1 ? $JSCompiler_StaticMethods_getReqXP$$(this, 
    $$jscomp$inline_1125_lvl$jscomp$inline_886$$ + 1) - $$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$ : "-") + ")\r\nMoney: ";
    $def$jscomp$inline_885_mo$jscomp$inline_1124$$ = 0;
    if ($marauroa$$.$me$) {
      for ($$jscomp$inline_1125_lvl$jscomp$inline_886$$ = $$jscomp$makeIterator$$(this.$C$), $$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$ = $$jscomp$inline_1125_lvl$jscomp$inline_886$$.next(); !$$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$.done; $$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$ = $$jscomp$inline_1125_lvl$jscomp$inline_886$$.next()) {
        if ($$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$ = $marauroa$$.$me$[$$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$.value]) {
          for (var $idx$jscomp$inline_1128$$ = 0; $idx$jscomp$inline_1128$$ < $$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$.count(); $idx$jscomp$inline_1128$$++) {
            var $i$jscomp$inline_1130_o$jscomp$inline_1129$$ = $$jscomp$inline_1126_slot$jscomp$inline_1127_xp$jscomp$inline_887$$.$getByIndex$($idx$jscomp$inline_1128$$);
            $i$jscomp$inline_1130_o$jscomp$inline_1129$$ instanceof $module$build$ts$entity$Item$default$$.$Item$ && "money" === $i$jscomp$inline_1130_o$jscomp$inline_1129$$.name && ($def$jscomp$inline_885_mo$jscomp$inline_1124$$ += parseInt($i$jscomp$inline_1130_o$jscomp$inline_1129$$.quantity));
          }
        }
      }
    }
    $JSCompiler_temp_const$jscomp$1005_hp$jscomp$inline_879_karma$jscomp$inline_876$$.innerText = $JSCompiler_temp_const$jscomp$1004_atk$jscomp$inline_884_base_hp$jscomp$inline_880$$ + $def$jscomp$inline_885_mo$jscomp$inline_1124$$;
    !$JSCompiler_StaticMethods_isBarEnabled$$(this, "karma") && $features$jscomp$5_key$jscomp$93_object$jscomp$12$$ && ($features$jscomp$5_key$jscomp$93_object$jscomp$12$$ = $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.features) && null != $features$jscomp$5_key$jscomp$93_object$jscomp$12$$.karma_indicator && $JSCompiler_StaticMethods_enableBar$$(this, "karma");
  }
};
function $JSCompiler_StaticMethods_getReqXP$$($JSCompiler_StaticMethods_getReqXP$self$$, $lvl$jscomp$3$$) {
  return 0 <= $lvl$jscomp$3$$ && $lvl$jscomp$3$$ < $JSCompiler_StaticMethods_getReqXP$self$$.$v$.length ? $JSCompiler_StaticMethods_getReqXP$self$$.$v$[$lvl$jscomp$3$$] : -1;
}
function $JSCompiler_StaticMethods_enableCharName$$($visible$jscomp$1$$) {
  document.getElementById("charname").style.display = void 0 === $visible$jscomp$1$$ || $visible$jscomp$1$$ ? "block" : "none";
}
function $JSCompiler_StaticMethods_enableBar$$($JSCompiler_StaticMethods_enableBar$self_bar$$, $id$jscomp$36$$, $visible$jscomp$2$$) {
  ($JSCompiler_StaticMethods_enableBar$self_bar$$ = $JSCompiler_StaticMethods_enableBar$self_bar$$.$A$[$id$jscomp$36$$]) && $JSCompiler_StaticMethods_setVisible$$($JSCompiler_StaticMethods_enableBar$self_bar$$, void 0 === $visible$jscomp$2$$ ? !0 : $visible$jscomp$2$$);
}
function $JSCompiler_StaticMethods_isBarEnabled$$($JSCompiler_StaticMethods_isBarEnabled$self_bar$jscomp$1$$, $id$jscomp$37$$) {
  return ($JSCompiler_StaticMethods_isBarEnabled$self_bar$jscomp$1$$ = $JSCompiler_StaticMethods_isBarEnabled$self_bar$jscomp$1$$.$A$[$id$jscomp$37$$]) ? $JSCompiler_StaticMethods_isBarEnabled$self_bar$jscomp$1$$.isVisible() : !1;
}
;var $module$build$ts$ui$factory$DesktopUserInterfaceFactory$default$$ = {};
Object.defineProperty($module$build$ts$ui$factory$DesktopUserInterfaceFactory$default$$, "__esModule", {value:!0});
$module$build$ts$ui$factory$DesktopUserInterfaceFactory$default$$.$DesktopUserInterfaceFactory$ = void 0;
$module$build$ts$ui$factory$DesktopUserInterfaceFactory$default$$.$DesktopUserInterfaceFactory$ = function() {
};
$module$build$ts$ui$factory$DesktopUserInterfaceFactory$default$$.$DesktopUserInterfaceFactory$.prototype.create = function() {
  var $bottomPanel_leftPanel_rightPanel_topPanel$$ = new $module$build$ts$ui$toolkit$Panel$default$$.$Panel$("topPanel");
  $JSCompiler_StaticMethods_registerComponent$$(1, $bottomPanel_leftPanel_rightPanel_topPanel$$);
  $bottomPanel_leftPanel_rightPanel_topPanel$$ = new $module$build$ts$ui$toolkit$Panel$default$$.$Panel$("leftColumn");
  $JSCompiler_StaticMethods_registerComponent$$(1, $bottomPanel_leftPanel_rightPanel_topPanel$$);
  var $keyring_socialPanel$$ = new $module$build$ts$ui$toolkit$TabPanelComponent$default$$.$TabPanelComponent$();
  $JSCompiler_StaticMethods_registerComponent$$(111, $keyring_socialPanel$$);
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 108, new $module$build$ts$ui$component$MiniMapComponent$default$$.$MiniMapComponent$());
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 109, new $module$build$ts$ui$component$ZoneInfoComponent$default$$.$ZoneInfoComponent$());
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 110, new $module$build$ts$ui$component$PlayerStatsComponent$default$$.$PlayerStatsComponent$());
  $bottomPanel_leftPanel_rightPanel_topPanel$$.add($keyring_socialPanel$$);
  this.add($keyring_socialPanel$$, 102, new $module$build$ts$ui$component$BuddyListComponent$default$$.$BuddyListComponent$());
  this.add($keyring_socialPanel$$, 105, new $module$build$ts$ui$component$GroupPanelComponent$default$$.$GroupPanelComponent$());
  $JSCompiler_StaticMethods_addTab$$($keyring_socialPanel$$, "Friends");
  $JSCompiler_StaticMethods_addTab$$($keyring_socialPanel$$, "Group");
  $bottomPanel_leftPanel_rightPanel_topPanel$$ = new $module$build$ts$ui$toolkit$Panel$default$$.$Panel$("rightColumn");
  $JSCompiler_StaticMethods_registerComponent$$(2, $bottomPanel_leftPanel_rightPanel_topPanel$$);
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 106, new $module$build$ts$ui$component$PlayerEquipmentComponent$default$$.$PlayerEquipmentComponent$());
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 101, new $module$build$ts$ui$component$BagComponent$default$$.$BagComponent$(void 0, "bag", 3, 4, !1, void 0));
  $keyring_socialPanel$$ = new $module$build$ts$ui$component$KeyringComponent$default$$.$KeyringComponent$(void 0, "keyring", 2, 4, !1, "slot-key.png");
  $JSCompiler_StaticMethods_setVisible$$($keyring_socialPanel$$, !1);
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 107, $keyring_socialPanel$$);
  $bottomPanel_leftPanel_rightPanel_topPanel$$ = new $module$build$ts$ui$toolkit$Panel$default$$.$Panel$("bottomPanel", !0);
  $JSCompiler_StaticMethods_registerComponent$$(3, $bottomPanel_leftPanel_rightPanel_topPanel$$);
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 103, new $module$build$ts$ui$component$ChatInputComponent$default$$.$ChatInputComponent$());
  this.add($bottomPanel_leftPanel_rightPanel_topPanel$$, 104, new $module$build$ts$ui$component$ChatLogComponent$default$$.$ChatLogComponent$());
};
$module$build$ts$ui$factory$DesktopUserInterfaceFactory$default$$.$DesktopUserInterfaceFactory$.prototype.add = function($panel$$, $uiComponentEnum$jscomp$2$$, $component$jscomp$4$$) {
  $panel$$.add($component$jscomp$4$$);
  $JSCompiler_StaticMethods_registerComponent$$($uiComponentEnum$jscomp$2$$, $component$jscomp$4$$);
};
var $module$build$ts$util$DialogHandler$default$$ = {};
Object.defineProperty($module$build$ts$util$DialogHandler$default$$, "__esModule", {value:!0});
$module$build$ts$util$DialogHandler$default$$.$DialogHandler$ = void 0;
$module$build$ts$util$DialogHandler$default$$.$DialogHandler$ = function() {
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$util$DialogHandler$default$$.$DialogHandler$.prototype;
$JSCompiler_prototypeAlias$$.set = function($c$jscomp$36$$) {
  this.$isOpen$() && this.close();
  this.content = $c$jscomp$36$$;
  this.content.$componentElement$.addEventListener("mousedown", function($e$jscomp$89$$) {
    $e$jscomp$89$$.preventDefault();
    $e$jscomp$89$$.stopPropagation();
  });
};
$JSCompiler_prototypeAlias$$.$unset$ = function() {
  this.content = void 0;
};
$JSCompiler_prototypeAlias$$.get = function() {
  return this.content;
};
$JSCompiler_prototypeAlias$$.$isOpen$ = function() {
  return "undefined" !== typeof this.content && this.content.$isOpen$();
};
$JSCompiler_prototypeAlias$$.close = function($unset$$) {
  $unset$$ = void 0 === $unset$$ ? !1 : $unset$$;
  this.content && (this.content.close(), $unset$$ && this.$unset$());
};
var $module$build$ts$Client$default$$ = {};
Object.defineProperty($module$build$ts$Client$default$$, "__esModule", {value:!0});
$module$build$ts$Client$default$$.$Client$ = void 0;
$module$build$ts$Client$default$$.$Client$ = function() {
  this.$v$ = this.$A$ = !1;
};
$module$build$ts$Client$default$$.$Client$.get = function() {
  $module$build$ts$Client$default$$.$Client$.$v$ || ($module$build$ts$Client$default$$.$Client$.$v$ = new $module$build$ts$Client$default$$.$Client$());
  return $module$build$ts$Client$default$$.$Client$.$v$;
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$Client$default$$.$Client$.prototype;
$JSCompiler_prototypeAlias$$.$init$ = function() {
  this.$A$ ? console.warn("tried to re-initialize client") : (this.$A$ = !0, $stendhal$$.$paths$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$P$(), $stendhal$$.$config$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$A$(), $stendhal$$.$session$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$F$(), this.initData(), $stendhal$$.$ui$ = $stendhal$$.$ui$ || {}, $stendhal$$.$ui$.$equip$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$getInventory$(), $stendhal$$.$ui$.$html$ = 
  $module$build$ts$SingletonRepo$default$$.$singletons$.$K$(), $stendhal$$.$ui$.$touch$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$T$(), $stendhal$$.$ui$.$soundMan$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$v$(), $stendhal$$.$ui$.$gamewindow$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$U$(), $stendhal$$.$zone$ = new $module$build$ts$entity$Zone$default$$.$Zone$(), $stendhal$$.$zone$.$ground$ = new $module$build$ts$entity$Ground$default$$.$Ground$());
};
$JSCompiler_prototypeAlias$$.initData = function() {
  $stendhal$$.data = $stendhal$$.data || {};
  $stendhal$$.data.cache = $module$build$ts$SingletonRepo$default$$.$singletons$.$I$();
  $stendhal$$.data.cache.$init$();
  $stendhal$$.data.$cstatus$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$H$();
  $stendhal$$.data.$cstatus$.$init$();
  $stendhal$$.data.group = $module$build$ts$SingletonRepo$default$$.$singletons$.$D$();
  $stendhal$$.data.$outfit$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$O$();
  $stendhal$$.data.$sprites$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$R$();
  $stendhal$$.data.map = $module$build$ts$SingletonRepo$default$$.$singletons$.$N$();
};
$JSCompiler_prototypeAlias$$.$startup$ = function() {
  $JSCompiler_StaticMethods_devWarning$$();
  var $sparams_ws$$ = (new URL(document.URL)).searchParams;
  $stendhal$$.$config$.$init$($sparams_ws$$);
  $stendhal$$.$session$.$init$($sparams_ws$$);
  $JSCompiler_StaticMethods_refreshTheme$$();
  document.getElementById("body").style.setProperty("font-family", $stendhal$$.$config$.get("ui.font.body"));
  $module$build$ts$SingletonRepo$default$$.$singletons$.$J$().$init$();
  $module$build$ts$SingletonRepo$default$$.$singletons$.$S$().$init$();
  $module$build$ts$SingletonRepo$default$$.$singletons$.$B$().$init$();
  $stendhal$$.data.$outfit$.$init$();
  (new $module$build$ts$ui$factory$DesktopUserInterfaceFactory$default$$.$DesktopUserInterfaceFactory$()).create();
  $module$build$ts$util$Chat$default$$.$Chat$.log("client", "Client loaded. Connecting...");
  $JSCompiler_StaticMethods_registerMarauroaEventHandlers$$();
  $JSCompiler_StaticMethods_registerBrowserEventHandlers$$();
  $sparams_ws$$ = $module$build$ts$data$Paths$default$$.$Paths$.$B$.substring(1);
  "/testdata" !== $stendhal$$.$paths$.data || $stendhal$$.$session$.$server_default$ || ($sparams_ws$$ = $sparams_ws$$.replace(/t/, "s"), $module$build$ts$util$Chat$default$$.$Chat$.log("warning", "WARNING: You are connecting to the production server with a development build of the test client which may contain bugs or not function as intented. Proceeed with caution."));
  $marauroa$$.$clientFramework$.connect(null, null, $sparams_ws$$);
  $stendhal$$.$ui$.$actionContextMenu$ = new $module$build$ts$util$DialogHandler$default$$.$DialogHandler$();
  $stendhal$$.$ui$.$globalInternalWindow$ = new $module$build$ts$util$DialogHandler$default$$.$DialogHandler$();
  $stendhal$$.data.$sprites$.$startupCache$();
  $module$build$ts$SingletonRepo$default$$.$singletons$.$v$().$startupCache$();
  document.getElementById("gamewindow") && ($stendhal$$.$ui$.$gamewindow$.$draw$.apply($stendhal$$.$ui$.$gamewindow$, arguments), $JSCompiler_StaticMethods_updateJoystick$$());
  if ($JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "input.movecont")) {
    var $checkConnection$$ = function() {
      setTimeout(function() {
        $tries$$++;
        $socket$jscomp$1$$.readyState === WebSocket.OPEN ? $marauroa$$.$clientFramework$.$sendAction$({type:"move.continuous", "move.continuous":""}) : 5 < $tries$$ ? console.warn('could not set "move.continuous" attribute, gave up after ' + $tries$$ + " tries") : $checkConnection$$();
      }, 3000);
    }, $socket$jscomp$1$$ = $marauroa$$.$clientFramework$.$socket$, $tries$$ = 0;
    $checkConnection$$();
  }
};
function $JSCompiler_StaticMethods_devWarning$$() {
  console.log("%c ", "padding: 30px; background: url(" + window.location.protocol + "://" + window.location.host + "/images/buttons/devtools-warning.png) no-repeat; color: #AF0");
  console.log("%cIf someone told you, to copy and paste something here, it's a scam and will give them access to your account.", "color:#A00; background-color:#FFF; font-size:150%");
  console.log("If you are a developer and curious about Stendhal, have a look at https://stendhalgame.org/development/introduction.html to get the source code. And perhaps, contribute a feature or a bugfix. ");
  console.log(" ");
  console.log(" ");
  window.eval = function() {
  };
}
function $JSCompiler_StaticMethods_registerMarauroaEventHandlers$$() {
  $marauroa$$.$clientFramework$.$onDisconnect$ = function() {
    $module$build$ts$Client$default$$.$Client$.$v$.$v$ || $module$build$ts$util$Chat$default$$.$Chat$.$v$("error", "Disconnected from server.");
  };
  $marauroa$$.$clientFramework$.$onLoginRequired$ = function($config$jscomp$2_url$jscomp$25$$) {
    if ($config$jscomp$2_url$jscomp$25$$.client_login_url) {
      $module$build$ts$Client$default$$.$Client$.$v$.$v$ = !0;
      var $currentUrl$$ = encodeURI(window.location.pathname + window.location.hash);
      $config$jscomp$2_url$jscomp$25$$ = $config$jscomp$2_url$jscomp$25$$.client_login_url.replace("[url]", $currentUrl$$);
      window.location.href = $config$jscomp$2_url$jscomp$25$$;
    } else {
      document.getElementById("body").style.cursor = "auto", document.getElementById("loginpopup").style.display = "none", $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Login", new $module$build$ts$ui$dialog$LoginDialog$default$$.$LoginDialog$(), 100, 50);
    }
  };
  $marauroa$$.$clientFramework$.$onCreateAccountAck$ = function() {
    alert("Account succesfully created, please login.");
    window.location.reload();
  };
  $marauroa$$.$clientFramework$.$onCreateCharacterAck$ = function() {
  };
  $marauroa$$.$clientFramework$.$onLoginFailed$ = function($_reason$jscomp$1$$, $_text$$) {
    alert("Login failed. " + $_text$$);
    window.location.reload();
  };
  $marauroa$$.$clientFramework$.$onAvailableCharacterDetails$ = function($characters$jscomp$2$$) {
    $module$build$ts$ui$toolkit$SingletonFloatingWindow$default$$.$SingletonFloatingWindow$.$B$();
    if (!Object.keys($characters$jscomp$2$$).length && this.username) {
      $marauroa$$.$clientFramework$.$createCharacter$(this.username, {});
    } else {
      window.location.hash && $JSCompiler_StaticMethods_setCharName$$($stendhal$$.$session$, window.location.hash.substring(1));
      var $name$jscomp$88$$ = $stendhal$$.$session$.$charname$ || "";
      $name$jscomp$88$$ ? $module$build$ts$Client$default$$.$Client$.get().$chooseCharacter$($name$jscomp$88$$) : (document.getElementById("body").style.cursor = "auto", document.getElementById("loginpopup").style.display = "none", $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Choose Character", new $module$build$ts$ui$dialog$ChooseCharacterDialog$default$$.$ChooseCharacterDialog$($characters$jscomp$2$$), 100, 50));
    }
  };
  $marauroa$$.$clientFramework$.$onTransferREQ$ = function($items$jscomp$6$$) {
    for (var $i$jscomp$96$$ in $items$jscomp$6$$) {
      "undefined" != typeof $items$jscomp$6$$[$i$jscomp$96$$].name && ($items$jscomp$6$$[$i$jscomp$96$$].ack = !0);
    }
  };
  $marauroa$$.$clientFramework$.$onTransfer$ = function($items$jscomp$7$$) {
    var $data$jscomp$87$$ = {}, $zoneName$jscomp$1$$ = "", $i$jscomp$97$$;
    for ($i$jscomp$97$$ in $items$jscomp$7$$) {
      var $name$jscomp$89_zoneinfo$jscomp$inline_895$$ = $items$jscomp$7$$[$i$jscomp$97$$].name;
      $zoneName$jscomp$1$$ = $name$jscomp$89_zoneinfo$jscomp$inline_895$$.substring(0, $name$jscomp$89_zoneinfo$jscomp$inline_895$$.indexOf("."));
      $name$jscomp$89_zoneinfo$jscomp$inline_895$$ = $name$jscomp$89_zoneinfo$jscomp$inline_895$$.substring($name$jscomp$89_zoneinfo$jscomp$inline_895$$.indexOf(".") + 1);
      $data$jscomp$87$$[$name$jscomp$89_zoneinfo$jscomp$inline_895$$] = $items$jscomp$7$$[$i$jscomp$97$$].data;
      "data_map" === $name$jscomp$89_zoneinfo$jscomp$inline_895$$ && ($name$jscomp$89_zoneinfo$jscomp$inline_895$$ = {}, $marauroa$$.$Deserializer$.$fromBase64$($items$jscomp$7$$[$i$jscomp$97$$].data).$readAttributes$($name$jscomp$89_zoneinfo$jscomp$inline_895$$), $module$build$ts$ui$UI$default$$.$ui$.get(109).$zoneChange$($name$jscomp$89_zoneinfo$jscomp$inline_895$$), $module$build$ts$SingletonRepo$default$$.$singletons$.$C$().update($name$jscomp$89_zoneinfo$jscomp$inline_895$$.weather));
    }
    $stendhal$$.data.map.$onTransfer$($zoneName$jscomp$1$$, $data$jscomp$87$$);
  };
  document.getElementById("gamewindow") && ($marauroa$$.$perceptionListener$ = new $module$build$ts$PerceptionListener$default$$.$PerceptionListener$($marauroa$$.$perceptionListener$), $marauroa$$.$perceptionListener$.$onPerceptionEnd$ = function() {
    $JSCompiler_StaticMethods_sortEntities$$();
    $module$build$ts$ui$UI$default$$.$ui$.get(108).$draw$();
    $module$build$ts$ui$UI$default$$.$ui$.get(102).update();
    $stendhal$$.$ui$.$equip$.update();
    $module$build$ts$ui$UI$default$$.$ui$.get(106).update();
    this.loaded || (this.loaded = !0, setTimeout(function() {
      document.getElementById("body").style.cursor = "auto";
      document.getElementById("client").style.display = "block";
      document.getElementById("loginpopup").style.display = "none";
    }, 300));
  });
}
$JSCompiler_prototypeAlias$$.$chooseCharacter$ = function($name$jscomp$90$$) {
  $JSCompiler_StaticMethods_setCharName$$($stendhal$$.$session$, $name$jscomp$90$$);
  $marauroa$$.$clientFramework$.$chooseCharacter$($name$jscomp$90$$);
  $module$build$ts$util$Chat$default$$.$Chat$.log("client", "Loading world...");
  $JSCompiler_StaticMethods_playGlobalizedEffect$$($module$build$ts$SingletonRepo$default$$.$singletons$.$v$(), "ui/login");
};
function $JSCompiler_StaticMethods_registerBrowserEventHandlers$$() {
  var $gamewindow$jscomp$3_keyHandler$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$L$();
  document.addEventListener("keydown", $gamewindow$jscomp$3_keyHandler$$.$onKeyDown$);
  document.addEventListener("keyup", $gamewindow$jscomp$3_keyHandler$$.$onKeyUp$);
  document.addEventListener("contextmenu", $stendhal$$.$main$.$preventContextMenu$);
  document.addEventListener("mousedown", function($e$jscomp$91$$) {
    $stendhal$$.$ui$.$actionContextMenu$.$isOpen$() && ($stendhal$$.$ui$.$actionContextMenu$.close(!0), $e$jscomp$91$$.preventDefault(), $e$jscomp$91$$.stopPropagation());
  });
  window.addEventListener("beforeunload", function() {
    $module$build$ts$Client$default$$.$Client$.$v$.$v$ = !0;
  });
  document.getElementById("body").addEventListener("mouseenter", $stendhal$$.$main$.$onMouseEnter$);
  $gamewindow$jscomp$3_keyHandler$$ = document.getElementById("gamewindow");
  $gamewindow$jscomp$3_keyHandler$$.setAttribute("draggable", "true");
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("mousedown", $stendhal$$.$ui$.$gamewindow$.$onMouseDown$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("dblclick", $stendhal$$.$ui$.$gamewindow$.$onMouseDown$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("dragstart", $stendhal$$.$ui$.$gamewindow$.$onDragStart$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("mousemove", $stendhal$$.$ui$.$gamewindow$.$onMouseMove$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("touchstart", $stendhal$$.$ui$.$gamewindow$.$onMouseDown$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("touchend", $stendhal$$.$ui$.$gamewindow$.$onTouchEnd$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("dragover", $stendhal$$.$ui$.$gamewindow$.$onDragOver$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("drop", $stendhal$$.$ui$.$gamewindow$.$onDrop$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("contextmenu", $stendhal$$.$ui$.$gamewindow$.$onContentMenu$);
  $gamewindow$jscomp$3_keyHandler$$.addEventListener("wheel", $stendhal$$.$ui$.$gamewindow$.$onMouseWheel$);
  document.body.addEventListener("mouseup", function($e$jscomp$92$$) {
    0 == $e$jscomp$92$$.button && $stendhal$$.$ui$.$gamewindow$.$joystick$.reset();
  });
  document.getElementById("menubutton").addEventListener("click", function() {
    var $dialogState_menuFrame$$ = $JSCompiler_StaticMethods_getWindowState$$("menu"), $menuContent$$ = new $module$build$ts$ui$dialog$ApplicationMenuDialog$default$$.$ApplicationMenuDialog$();
    $dialogState_menuFrame$$ = $JSCompiler_StaticMethods_createSingletonFloatingWindow$$("Menu", $menuContent$$, $dialogState_menuFrame$$.x, $dialogState_menuFrame$$.y);
    $dialogState_menuFrame$$.$windowId$ = "menu";
    $menuContent$$.frame = $dialogState_menuFrame$$;
  });
  document.getElementById("soundbutton").addEventListener("click", $stendhal$$.$main$.$toggleSound$);
  $JSCompiler_StaticMethods_onSoundToggled$$();
  var $click_indicator$$ = document.getElementById("click-indicator");
  $click_indicator$$.onload = function() {
    $click_indicator$$.onload = null;
    document.addEventListener("click", $module$build$ts$Client$default$$.$Client$.$B$);
    document.addEventListener("touchend", $module$build$ts$Client$default$$.$Client$.$B$);
  };
  $click_indicator$$.src = $stendhal$$.$paths$.$gui$ + "/click_indicator.png";
}
$JSCompiler_prototypeAlias$$.$toggleSound$ = function() {
  $stendhal$$.$config$.set("ui.sound", !$JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "ui.sound"));
  $JSCompiler_StaticMethods_onSoundToggled$$();
};
function $JSCompiler_StaticMethods_onSoundToggled$$() {
  var $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$v$(), $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ = document.getElementById("soundbutton");
  if ($JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "ui.sound")) {
    $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$.textContent = "\ud83d\udd0a";
    $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ = !0;
    for (var $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$ = $$jscomp$makeIterator$$($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.$layers$), $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$ = $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next(); !$$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.done; $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$ = 
    $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next()) {
      $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$ = $$jscomp$makeIterator$$($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.active[$$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.value]);
      for (var $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$ = $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.next(); !$$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.done; $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$ = $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.next()) {
        $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$ = $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.value, $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.muted = !1, $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ = $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ && 
        !$$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.paused && !$$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.muted;
      }
    }
    if (!$errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$) {
      $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ = "Failed to unmute sounds:";
      $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getActive$$($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$));
      for ($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$ = $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next(); !$$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.done; $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$ = $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next()) {
        ($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$ = $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.value) && $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.src && $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.muted && ($errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ += "\n- " + $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.src);
      }
      console.warn($errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$);
    }
  } else {
    $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$.textContent = "\ud83d\udd07";
    $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ = !0;
    $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$ = $$jscomp$makeIterator$$($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.$layers$);
    for ($$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$ = $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next(); !$$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.done; $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$ = $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next()) {
      for ($$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$ = $$jscomp$makeIterator$$($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.active[$$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.value]), $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$ = $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.next(); !$$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.done; $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$ = 
      $$jscomp$inline_909_$jscomp$inline_917_$jscomp$key$layerName$jscomp$inline_908_$jscomp$key$layerName$jscomp$inline_916$$.next()) {
        $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$ = $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.value, $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.muted = !0, $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ = $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ && 
        $$jscomp$key$snd$jscomp$inline_910_$jscomp$key$snd$jscomp$inline_918_snd$jscomp$inline_911_snd$jscomp$inline_919$$.muted;
      }
    }
    if (!$errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$) {
      $errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ = "Failed to mute sounds:";
      $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getActive$$($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$));
      for ($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$ = $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next(); !$$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.done; $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$ = $$jscomp$inline_907_$jscomp$inline_915_$jscomp$iter$85_$jscomp$iter$86$$.next()) {
        ($$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$ = $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.value) && $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.src && !$$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.muted && ($errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$ += "\n- " + $$jscomp$key$snd$jscomp$4_snd$109_snd$jscomp$11_soundMan$jscomp$1$$.src);
      }
      console.warn($errmsg$108_errmsg$jscomp$1_muted$jscomp$inline_914_soundbutton$jscomp$1_unmuted$jscomp$inline_906$$);
    }
  }
}
$JSCompiler_prototypeAlias$$.$preventContextMenu$ = function($e$jscomp$93$$) {
  $e$jscomp$93$$.preventDefault();
};
$JSCompiler_prototypeAlias$$.$onMouseEnter$ = function($e$jscomp$94$$) {
  $e$jscomp$94$$.target.style.cursor = "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/normal.png) 1 3, auto";
};
$module$build$ts$Client$default$$.$Client$.$B$ = function($e$jscomp$95_pos$jscomp$17$$) {
  if ($JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "input.click.indicator")) {
    void 0 !== $module$build$ts$Client$default$$.$Client$.$A$ && (clearTimeout($module$build$ts$Client$default$$.$Client$.$A$), $module$build$ts$Client$default$$.$Client$.$A$ = void 0);
    $e$jscomp$95_pos$jscomp$17$$ = $JSCompiler_StaticMethods_extractPosition$$($e$jscomp$95_pos$jscomp$17$$);
    var $click_indicator$jscomp$1$$ = document.getElementById("click-indicator");
    $click_indicator$jscomp$1$$.style.left = $e$jscomp$95_pos$jscomp$17$$.pageX - $click_indicator$jscomp$1$$.width / 2 + "px";
    $click_indicator$jscomp$1$$.style.top = $e$jscomp$95_pos$jscomp$17$$.pageY - $click_indicator$jscomp$1$$.height / 2 + "px";
    $click_indicator$jscomp$1$$.style.display = "inline";
    $module$build$ts$Client$default$$.$Client$.$A$ = setTimeout(function() {
      $click_indicator$jscomp$1$$.style.display = "none";
    }, 300);
  }
};
$module$build$ts$Client$default$$.$Client$.$A$ = void 0;
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$main$ = $module$build$ts$Client$default$$.$Client$.get();
$stendhal$$.$main$.$init$();
document.addEventListener("DOMContentLoaded", $stendhal$$.$main$.$startup$);
window.addEventListener("error", $stendhal$$.$main$.onerror);
var $module$build$ts$action$MenuItem$default$$ = {};
Object.defineProperty($module$build$ts$action$MenuItem$default$$, "__esModule", {value:!0});
$module$build$ts$action$MenuItem$default$$.$MenuItem$ = void 0;
$module$build$ts$action$MenuItem$default$$.$MenuItem$ = function() {
};
Object.defineProperty({}, "__esModule", {value:!0});
var $module$build$ts$entity$Blood$default$$ = {};
Object.defineProperty($module$build$ts$entity$Blood$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Blood$default$$.$Blood$ = void 0;
$module$build$ts$entity$Blood$default$$.$Blood$ = function() {
  var $$jscomp$super$this$jscomp$54$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$54$$.$minimapShow$ = !1;
  $$jscomp$super$this$jscomp$54$$.zIndex = 2000;
  $$jscomp$super$this$jscomp$54$$.$sprite$ = {height:32, width:32, filename:$stendhal$$.$paths$.$sprites$ + "/combat/blood_red.png"};
  return $$jscomp$super$this$jscomp$54$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Blood$default$$.$Blood$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$Blood$default$$.$Blood$.prototype.set = function($key$jscomp$94$$, $value$jscomp$129$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$94$$, $value$jscomp$129$$);
  "amount" === $key$jscomp$94$$ ? this.$sprite$.offsetY = 32 * parseInt($value$jscomp$129$$, 10) : "class" === $key$jscomp$94$$ && (this.$sprite$.filename = $stendhal$$.$paths$.$sprites$ + "/combat/blood_" + $value$jscomp$129$$ + ".png");
};
$module$build$ts$entity$Blood$default$$.$Blood$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/walk.png) 1 3, auto";
};
$module$build$ts$entity$Blood$default$$.$Blood$.prototype.$drawSpriteAt$ = function($ctx$jscomp$47$$, $x$jscomp$158$$, $y$jscomp$138$$) {
  $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.blood") && $module$build$ts$entity$Entity$default$$.$Entity$.prototype.$drawSpriteAt$.call(this, $ctx$jscomp$47$$, $x$jscomp$158$$, $y$jscomp$138$$);
};
var $module$build$ts$entity$PopupInventory$default$$ = {};
Object.defineProperty($module$build$ts$entity$PopupInventory$default$$, "__esModule", {value:!0});
$module$build$ts$entity$PopupInventory$default$$.$PopupInventory$ = void 0;
$module$build$ts$entity$PopupInventory$default$$.$PopupInventory$ = function() {
  return $module$build$ts$entity$Entity$default$$.$Entity$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$entity$PopupInventory$default$$.$PopupInventory$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.prototype.$draw$ = function($JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.$draw$.call(this, $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$);
  if (this.$v$ && this.$v$.$isOpen$() && $marauroa$$.$me$) {
    $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ = $marauroa$$.$me$;
    if ($JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ && this.x && $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.x) {
      var $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$ = this.x + (this.width || 1) - 1;
      var $ox_right$jscomp$inline_1160_ty_bottom$jscomp$inline_1164$$ = $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.x + ($JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.width || 1) - 1;
      $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$ = this.x > $ox_right$jscomp$inline_1160_ty_bottom$jscomp$inline_1164$$ ? Math.abs(this.x - $ox_right$jscomp$inline_1160_ty_bottom$jscomp$inline_1164$$) : $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.x > $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$ ? Math.abs($JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.x - 
      $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$) : 0;
    } else {
      $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$ = -1;
    }
    if ($JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ && this.y && $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.y) {
      $ox_right$jscomp$inline_1160_ty_bottom$jscomp$inline_1164$$ = this.y + (this.height || 1) - 1;
      var $oy_bottom$jscomp$inline_1165$$ = $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.y + ($JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.height || 1) - 1;
      $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ = this.y > $oy_bottom$jscomp$inline_1165$$ ? Math.abs(this.y - $oy_bottom$jscomp$inline_1165$$) : $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.y > $ox_right$jscomp$inline_1160_ty_bottom$jscomp$inline_1164$$ ? Math.abs($JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$.y - $ox_right$jscomp$inline_1160_ty_bottom$jscomp$inline_1164$$) : 
      0;
    } else {
      $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ = -1;
    }
    $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ = 0 > $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$ && 0 > $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ ? -1 : (-1 < $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$ ? $tx_right$jscomp$inline_1159_x_dist$jscomp$inline_1134$$ : 0) + (-1 < $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ ? 
    $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ : 0);
    (5 <= $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$ || 0 > $JSCompiler_inline_result$jscomp$994_ctx$jscomp$48_other$jscomp$inline_1133_y_dist$jscomp$inline_1135$$) && this.$closeInventoryWindow$();
  }
};
$module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.prototype.$closeInventoryWindow$ = function() {
};
$module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.prototype.$destroy$ = function($_parent$jscomp$1$$) {
  this.$closeInventoryWindow$();
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.$destroy$.call(this, $_parent$jscomp$1$$);
};
var $module$build$ts$entity$Chest$default$$ = {};
Object.defineProperty($module$build$ts$entity$Chest$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Chest$default$$.$Chest$ = void 0;
var $OPEN_SPRITE$$module$build$ts$entity$Chest$$ = {filename:$stendhal$$.$paths$.$sprites$ + "/chest.png", height:32, width:32, offsetY:32}, $CLOSED_SPRITE$$module$build$ts$entity$Chest$$ = {filename:$stendhal$$.$paths$.$sprites$ + "/chest.png", height:32, width:32};
$module$build$ts$entity$Chest$default$$.$Chest$ = function() {
  var $$jscomp$super$this$jscomp$55$$ = $module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$55$$.$minimapShow$ = !0;
  $$jscomp$super$this$jscomp$55$$.$minimapStyle$ = $module$build$ts$util$Color$default$$.$Color$.$I$;
  $$jscomp$super$this$jscomp$55$$.zIndex = 5000;
  $$jscomp$super$this$jscomp$55$$.$sprite$ = $CLOSED_SPRITE$$module$build$ts$entity$Chest$$;
  $$jscomp$super$this$jscomp$55$$.$A$ = !1;
  return $$jscomp$super$this$jscomp$55$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Chest$default$$.$Chest$, $module$build$ts$entity$PopupInventory$default$$.$PopupInventory$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Chest$default$$.$Chest$.prototype;
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$95$$, $value$jscomp$130$$) {
  $module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.prototype.set.call(this, $key$jscomp$95$$, $value$jscomp$130$$);
  "open" === $key$jscomp$95$$ && (this.$sprite$ = $OPEN_SPRITE$$module$build$ts$entity$Chest$$, this.$A$ = !0);
  $JSCompiler_StaticMethods_isNextTo$$(this, $marauroa$$.$me$) && $JSCompiler_StaticMethods_openInventoryWindow$$(this);
};
$JSCompiler_prototypeAlias$$.$unset$ = function($key$jscomp$96$$) {
  $module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.prototype.$unset$.call(this, $key$jscomp$96$$);
  "open" === $key$jscomp$96$$ && (this.$sprite$ = $CLOSED_SPRITE$$module$build$ts$entity$Chest$$, this.$A$ = !1, this.$v$ && this.$v$.$isOpen$() && (this.$v$.close(), this.$v$ = void 0));
};
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !0;
};
$JSCompiler_prototypeAlias$$.onclick = function() {
  $JSCompiler_StaticMethods_isNextTo$$($marauroa$$.$me$, this) ? $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + this.id, zone:$marauroa$$.$currentZoneName$}) : this.$A$ && $JSCompiler_StaticMethods_openInventoryWindow$$(this);
};
function $JSCompiler_StaticMethods_openInventoryWindow$$($JSCompiler_StaticMethods_openInventoryWindow$self$$) {
  if (!$JSCompiler_StaticMethods_openInventoryWindow$self$$.$v$ || !$JSCompiler_StaticMethods_openInventoryWindow$self$$.$v$.$isOpen$()) {
    var $invComponent$$ = new $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$($JSCompiler_StaticMethods_openInventoryWindow$self$$, "content", 5, 6, $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "action.inventory.quickpickup"), void 0);
    $invComponent$$.$cid$ = "chest";
    var $dstate$jscomp$2$$ = $JSCompiler_StaticMethods_getWindowState$$("chest");
    $JSCompiler_StaticMethods_openInventoryWindow$self$$.$v$ = new $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$("Chest", $invComponent$$, $dstate$jscomp$2$$.x, $dstate$jscomp$2$$.y);
    $JSCompiler_StaticMethods_openInventoryWindow$self$$.$v$.$windowId$ = "chest";
  }
}
$JSCompiler_prototypeAlias$$.$closeInventoryWindow$ = function() {
  this.$v$ && this.$v$.$isOpen$() && (this.$v$.close(), this.$v$ = void 0);
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/bag.png) 1 3, auto";
};
var $module$build$ts$entity$Corpse$default$$ = {};
Object.defineProperty($module$build$ts$entity$Corpse$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Corpse$default$$.$Corpse$ = void 0;
$module$build$ts$entity$Corpse$default$$.$Corpse$ = function() {
  var $$jscomp$super$this$jscomp$56$$ = $module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$56$$.$minimapShow$ = !1;
  $$jscomp$super$this$jscomp$56$$.zIndex = 5500;
  $$jscomp$super$this$jscomp$56$$.$A$ = !1;
  return $$jscomp$super$this$jscomp$56$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Corpse$default$$.$Corpse$, $module$build$ts$entity$PopupInventory$default$$.$PopupInventory$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Corpse$default$$.$Corpse$.prototype;
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$97$$, $value$jscomp$131$$) {
  $module$build$ts$entity$PopupInventory$default$$.$PopupInventory$.prototype.set.call(this, $key$jscomp$97$$, $value$jscomp$131$$);
  this.$sprite$ = this.$sprite$ || {};
  var $bloodEnabled$$ = $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.blood");
  $bloodEnabled$$ && "image" === $key$jscomp$97$$ ? this.$sprite$.filename = $stendhal$$.$paths$.$sprites$ + "/corpse/" + $value$jscomp$131$$ + ".png" : $bloodEnabled$$ || "harmless_image" !== $key$jscomp$97$$ || (this.$sprite$.filename = $stendhal$$.$paths$.$sprites$ + "/corpse/" + $value$jscomp$131$$ + ".png");
};
$JSCompiler_prototypeAlias$$.$createSlot$ = function($name$jscomp$91$$) {
  var $slot$jscomp$12$$ = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpslotFactory$._default, {add:function($object$jscomp$14$$) {
    $marauroa$$.$rpslotFactory$._default.add.apply(this, arguments);
    0 < this.$_objects$.length && $JSCompiler_StaticMethods_autoOpenIfDesired$$(this.$_parent$);
  }, $del$:function($key$jscomp$98$$) {
    $marauroa$$.$rpslotFactory$._default.$del$.apply(this, arguments);
    0 == this.$_objects$.length && $JSCompiler_StaticMethods_closeCorpseInventory$$(this.$_parent$);
  }});
  $slot$jscomp$12$$.$_name$ = $name$jscomp$91$$;
  $slot$jscomp$12$$.$_objects$ = [];
  $slot$jscomp$12$$.$_parent$ = this;
  return $slot$jscomp$12$$;
};
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !0;
};
function $JSCompiler_StaticMethods_closeCorpseInventory$$($JSCompiler_StaticMethods_closeCorpseInventory$self$$) {
  $JSCompiler_StaticMethods_closeCorpseInventory$self$$.$v$ && $JSCompiler_StaticMethods_closeCorpseInventory$self$$.$v$.$isOpen$() && ($JSCompiler_StaticMethods_closeCorpseInventory$self$$.$v$.close(), $JSCompiler_StaticMethods_closeCorpseInventory$self$$.$v$ = void 0);
}
function $JSCompiler_StaticMethods_openCorpseInventory$$($JSCompiler_StaticMethods_openCorpseInventory$self$$) {
  if (!$JSCompiler_StaticMethods_openCorpseInventory$self$$.$v$ || !$JSCompiler_StaticMethods_openCorpseInventory$self$$.$v$.$isOpen$()) {
    var $content_row_invComponent$jscomp$1$$ = 2;
    $JSCompiler_StaticMethods_openCorpseInventory$self$$.content && 4 < $JSCompiler_StaticMethods_openCorpseInventory$self$$.content.$_objects$.length && ($content_row_invComponent$jscomp$1$$ = 3);
    $content_row_invComponent$jscomp$1$$ = new $module$build$ts$ui$component$ItemInventoryComponent$default$$.$ItemInventoryComponent$($JSCompiler_StaticMethods_openCorpseInventory$self$$, "content", $content_row_invComponent$jscomp$1$$, 2, $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "action.inventory.quickpickup"), void 0);
    $content_row_invComponent$jscomp$1$$.$cid$ = "corpse";
    var $dstate$jscomp$3$$ = $JSCompiler_StaticMethods_getWindowState$$("corpse");
    $JSCompiler_StaticMethods_openCorpseInventory$self$$.$v$ = new $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$("Corpse", $content_row_invComponent$jscomp$1$$, $dstate$jscomp$3$$.x, $dstate$jscomp$3$$.y);
    $JSCompiler_StaticMethods_openCorpseInventory$self$$.$v$.$windowId$ = "corpse";
  }
}
function $JSCompiler_StaticMethods_autoOpenIfDesired$$($JSCompiler_StaticMethods_autoOpenIfDesired$self$$) {
  $JSCompiler_StaticMethods_autoOpenIfDesired$self$$.$A$ || ($JSCompiler_StaticMethods_autoOpenIfDesired$self$$.$A$ = !0, $marauroa$$.$me$ && $JSCompiler_StaticMethods_autoOpenIfDesired$self$$.corpse_owner == $marauroa$$.$me$._name && window.setTimeout(function() {
    $JSCompiler_StaticMethods_openCorpseInventory$$($JSCompiler_StaticMethods_autoOpenIfDesired$self$$);
  }, 1));
}
$JSCompiler_prototypeAlias$$.$closeInventoryWindow$ = function() {
  $JSCompiler_StaticMethods_closeCorpseInventory$$(this);
};
$JSCompiler_prototypeAlias$$.onclick = function() {
  $JSCompiler_StaticMethods_openCorpseInventory$$(this);
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  return this.content && 0 !== this.content.$_objects$.length ? !this.corpse_owner || this.corpse_owner == $marauroa$$.$me$._name || "shared" === $stendhal$$.data.group.$lootmode$ && $stendhal$$.data.group.$members$[this.corpse_owner] ? "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/bag.png) 1 3, auto" : "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/lockedbag.png) 1 3, auto" : "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/emptybag.png) 1 3, auto";
};
$JSCompiler_prototypeAlias$$.$isDraggable$ = function() {
  return !0;
};
var $module$build$ts$entity$Creature$default$$ = {};
Object.defineProperty($module$build$ts$entity$Creature$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Creature$default$$.$Creature$ = void 0;
$module$build$ts$entity$Creature$default$$.$Creature$ = function() {
  var $$jscomp$super$this$jscomp$57$$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$57$$.$minimapStyle$ = $module$build$ts$util$Color$default$$.$Color$.$J$;
  $$jscomp$super$this$jscomp$57$$.$H$ = "monsters";
  $$jscomp$super$this$jscomp$57$$.$G$ = "#ffc8c8";
  return $$jscomp$super$this$jscomp$57$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Creature$default$$.$Creature$, $module$build$ts$entity$RPEntity$default$$.$RPEntity$);
$module$build$ts$entity$Creature$default$$.$Creature$.$v$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.$v$;
$module$build$ts$entity$Creature$default$$.$Creature$.prototype.onclick = function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"attack", target:"#" + this.id});
};
$module$build$ts$entity$Creature$default$$.$Creature$.prototype.$say$ = function($sprite$jscomp$inline_1138_text$jscomp$27$$) {
  if ($JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.speech.creature")) {
    var $JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1137$$ = $stendhal$$.$ui$.$gamewindow$;
    $sprite$jscomp$inline_1138_text$jscomp$27$$ = new $module$build$ts$sprite$SpeechBubble$default$$.$SpeechBubble$($sprite$jscomp$inline_1138_text$jscomp$27$$, this);
    $JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1137$$.$textSprites$.push($sprite$jscomp$inline_1138_text$jscomp$27$$);
    $sprite$jscomp$inline_1138_text$jscomp$27$$.$onAdded$($JSCompiler_StaticMethods_addTextSprite$self$jscomp$inline_1137$$.$ctx$);
  }
};
$module$build$ts$entity$Creature$default$$.$Creature$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/attack.png) 1 3, auto";
};
var $module$build$ts$entity$DomesticAnimal$default$$ = {};
Object.defineProperty($module$build$ts$entity$DomesticAnimal$default$$, "__esModule", {value:!0});
$module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$ = void 0;
$module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$ = function() {
  var $$jscomp$super$this$jscomp$58$$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$58$$.$minimapStyle$ = $module$build$ts$util$Color$default$$.$Color$.$K$;
  return $$jscomp$super$this$jscomp$58$$;
};
$$jscomp$inherits$$($module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$, $module$build$ts$entity$RPEntity$default$$.$RPEntity$);
$module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$.$v$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.$v$;
$module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$.prototype.$drawMain$ = function($ctx$jscomp$49$$) {
  !this.$C$ && this._rpclass && (this.largeWeight |= 20, "sheep" == this._rpclass && (this.largeWeight = 60), this.$C$ = $stendhal$$.$paths$.$sprites$ + "/" + this._rpclass + ".png");
  var $localX$jscomp$4$$ = 32 * this._x, $localY$jscomp$4$$ = 32 * this._y, $image$jscomp$13$$ = $stendhal$$.data.$sprites$.get(this.$C$);
  if ($image$jscomp$13$$.height) {
    var $yRow$jscomp$2$$ = this.dir - 1;
    this.weight >= this.largeWeight && ($yRow$jscomp$2$$ += 4);
    this.drawHeight = $image$jscomp$13$$.height / 4 / 2;
    this.drawWidth = $image$jscomp$13$$.width / 3;
    var $drawX$jscomp$4$$ = (32 * this.width - this.drawWidth) / 2, $frame$jscomp$5$$ = 0;
    0 < this.speed && ($frame$jscomp$5$$ = Math.floor(Date.now() / 100) % 3);
    $ctx$jscomp$49$$.drawImage($image$jscomp$13$$, $frame$jscomp$5$$ * this.drawWidth, $yRow$jscomp$2$$ * this.drawHeight, this.drawWidth, this.drawHeight, $localX$jscomp$4$$ + $drawX$jscomp$4$$, $localY$jscomp$4$$ + (32 * this.height - this.drawHeight), this.drawWidth, this.drawHeight);
  }
};
$module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/look.png) 1 3, auto";
};
$module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$.prototype.$buildActions$ = function($list$jscomp$5$$) {
  var $species$$ = "pet";
  "sheep" === this._rpclass && ($species$$ = "sheep");
  var $playerOwned$$ = $marauroa$$.$me$[$species$$];
  $playerOwned$$ || $list$jscomp$5$$.push({title:"Own", action:function($_entity$jscomp$7$$) {
    $marauroa$$.$clientFramework$.$sendAction$({type:"own", zone:$marauroa$$.$currentZoneName$, target:"#" + $_entity$jscomp$7$$.id});
  }});
  $playerOwned$$ === this.id && $list$jscomp$5$$.push({title:"Leave", action:function($_entity$jscomp$8$$) {
    $marauroa$$.$clientFramework$.$sendAction$({type:"forsake", zone:$marauroa$$.$currentZoneName$, species:$species$$, target:"#" + $_entity$jscomp$8$$.id});
  }});
  $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype.$buildActions$.call(this, $list$jscomp$5$$);
};
var $module$build$ts$entity$Portal$default$$ = {};
Object.defineProperty($module$build$ts$entity$Portal$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Portal$default$$.$Portal$ = void 0;
$module$build$ts$entity$Portal$default$$.$Portal$ = function() {
  var $$jscomp$super$this$jscomp$59$$ = $module$build$ts$entity$Entity$default$$.$Entity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$59$$.$minimapShow$ = !0;
  $$jscomp$super$this$jscomp$59$$.$minimapStyle$ = $module$build$ts$util$Color$default$$.$Color$.$A$;
  $$jscomp$super$this$jscomp$59$$.zIndex = 5000;
  return $$jscomp$super$this$jscomp$59$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Portal$default$$.$Portal$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$Portal$default$$.$Portal$.prototype.$buildActions$ = function($list$jscomp$6$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.$buildActions$.call(this, $list$jscomp$6$$);
  "house_portal" == this._rpclass ? ($list$jscomp$6$$.push({title:"Use", type:"use"}), $list$jscomp$6$$.push({title:"Knock", type:"knock"})) : ($list$jscomp$6$$.splice($list$jscomp$6$$.indexOf({title:"Look", type:"look"}), 1), $list$jscomp$6$$.push({title:"Use", type:"use"}));
};
$module$build$ts$entity$Portal$default$$.$Portal$.prototype.$isVisibleToAction$ = function() {
  return !0;
};
$module$build$ts$entity$Portal$default$$.$Portal$.prototype.$getDefaultAction$ = function() {
  return {type:"moveto", x:"" + this.x, y:"" + this.y, zone:$marauroa$$.$currentZoneName$};
};
$module$build$ts$entity$Portal$default$$.$Portal$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/portal.png) 1 3, auto";
};
var $module$build$ts$entity$Door$default$$ = {};
Object.defineProperty($module$build$ts$entity$Door$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Door$default$$.$Door$ = void 0;
$module$build$ts$entity$Door$default$$.$Door$ = function() {
  var $$jscomp$super$this$jscomp$60$$ = $module$build$ts$entity$Portal$default$$.$Portal$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$60$$.zIndex = 5000;
  return $$jscomp$super$this$jscomp$60$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Door$default$$.$Door$, $module$build$ts$entity$Portal$default$$.$Portal$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Door$default$$.$Door$.prototype;
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$50$$) {
  var $image$jscomp$14$$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/doors/" + this["class"] + ".png");
  if ($image$jscomp$14$$.height) {
    var $x$jscomp$159$$ = 32 * (this.x - 1), $y$jscomp$139$$ = 32 * (this.y - 1), $height$jscomp$31$$ = $image$jscomp$14$$.height / 2, $offsetY$jscomp$3$$ = $height$jscomp$31$$;
    "" === this.open && ($offsetY$jscomp$3$$ = 0);
    $ctx$jscomp$50$$.drawImage($image$jscomp$14$$, 0, $offsetY$jscomp$3$$, $image$jscomp$14$$.width, $height$jscomp$31$$, $x$jscomp$159$$, $y$jscomp$139$$, $image$jscomp$14$$.width, $height$jscomp$31$$);
  }
};
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$7$$) {
  $list$jscomp$7$$.push({title:"Look", type:"look"});
  $list$jscomp$7$$.push({title:"Use", type:"use"});
};
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !0;
};
$JSCompiler_prototypeAlias$$.$getDefaultAction$ = function() {
  return {type:"moveto", x:"" + this.x, y:"" + this.y, zone:$marauroa$$.$currentZoneName$};
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/portal.png) 1 3, auto";
};
var $module$build$ts$entity$Food$default$$ = {};
Object.defineProperty($module$build$ts$entity$Food$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Food$default$$.$Food$ = void 0;
$module$build$ts$entity$Food$default$$.$Food$ = function() {
  var $$jscomp$super$this$jscomp$61$$ = $module$build$ts$entity$Entity$default$$.$Entity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$61$$.zIndex = 5000;
  return $$jscomp$super$this$jscomp$61$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Food$default$$.$Food$, $module$build$ts$entity$Entity$default$$.$Entity$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Food$default$$.$Food$.prototype;
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$99$$, $value$jscomp$132$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$99$$, $value$jscomp$132$$);
  "amount" === $key$jscomp$99$$ && (this.$v$ = parseInt($value$jscomp$132$$, 10));
};
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$51$$) {
  var $image$jscomp$15$$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/food.png");
  $image$jscomp$15$$.height && $ctx$jscomp$51$$.drawImage($image$jscomp$15$$, 0, 32 * this.$v$, 32, 32, 32 * this.x, 32 * this.y, 32, 32);
};
$JSCompiler_prototypeAlias$$.onclick = function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"look", target:"#" + this.id});
};
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !0;
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/look.png) 1 3, auto";
};
var $module$build$ts$entity$GameBoard$default$$ = {};
Object.defineProperty($module$build$ts$entity$GameBoard$default$$, "__esModule", {value:!0});
$module$build$ts$entity$GameBoard$default$$.$GameBoard$ = void 0;
$module$build$ts$entity$GameBoard$default$$.$GameBoard$ = function() {
  var $$jscomp$super$this$jscomp$62$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$62$$.$minimapShow$ = !1;
  $$jscomp$super$this$jscomp$62$$.zIndex = 100;
  $$jscomp$super$this$jscomp$62$$.$sprite$ = {height:96, width:96};
  return $$jscomp$super$this$jscomp$62$$;
};
$$jscomp$inherits$$($module$build$ts$entity$GameBoard$default$$.$GameBoard$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$GameBoard$default$$.$GameBoard$.prototype.set = function($key$jscomp$100$$, $value$jscomp$133$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$100$$, $value$jscomp$133$$);
  "class" === $key$jscomp$100$$ && (this.$sprite$.filename = $stendhal$$.$paths$.$sprites$ + "/gameboard/" + this["class"] + ".png");
};
$module$build$ts$entity$GameBoard$default$$.$GameBoard$.prototype.$isVisibleToAction$ = function() {
  return !1;
};
$module$build$ts$entity$GameBoard$default$$.$GameBoard$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/walk.png) 1 3, auto";
};
var $module$build$ts$entity$Gate$default$$ = {};
Object.defineProperty($module$build$ts$entity$Gate$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Gate$default$$.$Gate$ = void 0;
$module$build$ts$entity$Gate$default$$.$Gate$ = function() {
  var $$jscomp$super$this$jscomp$63$$ = $module$build$ts$entity$Entity$default$$.$Entity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$63$$.zIndex = 5000;
  return $$jscomp$super$this$jscomp$63$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Gate$default$$.$Gate$, $module$build$ts$entity$Entity$default$$.$Entity$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$Gate$default$$.$Gate$.prototype;
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$101$$, $value$jscomp$134$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$101$$, $value$jscomp$134$$);
  "resistance" === $key$jscomp$101$$ ? this.locked = 0 !== parseInt($value$jscomp$134$$, 10) : ("image" === $key$jscomp$101$$ || "orientation" === $key$jscomp$101$$) && delete this._image;
};
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$8$$) {
  var $id$jscomp$38$$ = this.id;
  $list$jscomp$8$$.push({title:this.locked ? "Open" : "Close", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + $id$jscomp$38$$, zone:$marauroa$$.$currentZoneName$,});
  }});
};
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$52$$) {
  void 0 == this.$v$ && (this.$v$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/doors/" + this.image + "_" + this.orientation + ".png"));
  if (this.$v$.height) {
    var $height$jscomp$32$$ = this.$v$.height / 2;
    $ctx$jscomp$52$$.drawImage(this.$v$, 0, this.locked ? $height$jscomp$32$$ : 0, this.$v$.width, $height$jscomp$32$$, 32 * this._x + -32 * Math.floor(this.$v$.width / 32 / 2), 32 * this._y + -32 * Math.floor($height$jscomp$32$$ / 32 / 2), this.$v$.width, $height$jscomp$32$$);
  }
};
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !0;
};
$JSCompiler_prototypeAlias$$.onclick = function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + this.id, zone:$marauroa$$.$currentZoneName$});
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/activity.png) 1 3, auto";
};
var $module$build$ts$entity$GrowingEntitySpawner$default$$ = {};
Object.defineProperty($module$build$ts$entity$GrowingEntitySpawner$default$$, "__esModule", {value:!0});
$module$build$ts$entity$GrowingEntitySpawner$default$$.$GrowingEntitySpawner$ = void 0;
$module$build$ts$entity$GrowingEntitySpawner$default$$.$GrowingEntitySpawner$ = function() {
  var $$jscomp$super$this$jscomp$64$$ = $module$build$ts$entity$Entity$default$$.$Entity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$64$$.zIndex = 3000;
  return $$jscomp$super$this$jscomp$64$$;
};
$$jscomp$inherits$$($module$build$ts$entity$GrowingEntitySpawner$default$$.$GrowingEntitySpawner$, $module$build$ts$entity$Entity$default$$.$Entity$);
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$GrowingEntitySpawner$default$$.$GrowingEntitySpawner$.prototype;
$JSCompiler_prototypeAlias$$.$isVisibleToAction$ = function() {
  return !0;
};
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$9$$) {
  this.menu || $list$jscomp$9$$.push({title:"Harvest", type:"use",});
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.$buildActions$.call(this, $list$jscomp$9$$);
};
$JSCompiler_prototypeAlias$$.onclick = function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + this.id, zone:$marauroa$$.$currentZoneName$});
};
$JSCompiler_prototypeAlias$$.$draw$ = function($ctx$jscomp$53$$) {
  var $localX$jscomp$7$$ = 32 * this.x, $localY$jscomp$7$$ = 32 * this.y, $class_name_image$jscomp$16$$ = this["class"];
  $class_name_image$jscomp$16$$.includes(" ") && ($class_name_image$jscomp$16$$ = $class_name_image$jscomp$16$$.replace(" ", "_"));
  $class_name_image$jscomp$16$$ = $stendhal$$.data.$sprites$.get($stendhal$$.$paths$.$sprites$ + "/" + $class_name_image$jscomp$16$$ + ".png");
  if ($class_name_image$jscomp$16$$.height) {
    var $drawHeight$jscomp$4$$ = $class_name_image$jscomp$16$$.height / (parseInt(this.max_ripeness, 10) + 1);
    $ctx$jscomp$53$$.drawImage($class_name_image$jscomp$16$$, 0, this.ripeness * $drawHeight$jscomp$4$$, $class_name_image$jscomp$16$$.width, $drawHeight$jscomp$4$$, $localX$jscomp$7$$, $localY$jscomp$7$$ - $drawHeight$jscomp$4$$ + 32, $class_name_image$jscomp$16$$.width, $drawHeight$jscomp$4$$);
  }
};
$JSCompiler_prototypeAlias$$.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/harvest.png) 1 3, auto";
};
var $module$build$ts$entity$NPC$default$$ = {};
Object.defineProperty($module$build$ts$entity$NPC$default$$, "__esModule", {value:!0});
$module$build$ts$entity$NPC$default$$.$NPC$ = void 0;
$module$build$ts$entity$NPC$default$$.$NPC$ = function() {
  var $$jscomp$super$this$jscomp$65$$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.call(this) || this;
  $$jscomp$super$this$jscomp$65$$.$minimapStyle$ = $module$build$ts$util$Color$default$$.$Color$.$NPC$;
  $$jscomp$super$this$jscomp$65$$.$H$ = "npc";
  $$jscomp$super$this$jscomp$65$$.$G$ = "#c8c8ff";
  $$jscomp$super$this$jscomp$65$$.hp = 100;
  $$jscomp$super$this$jscomp$65$$.base_hp = 100;
  return $$jscomp$super$this$jscomp$65$$;
};
$$jscomp$inherits$$($module$build$ts$entity$NPC$default$$.$NPC$, $module$build$ts$entity$RPEntity$default$$.$RPEntity$);
$module$build$ts$entity$NPC$default$$.$NPC$.$v$ = $module$build$ts$entity$RPEntity$default$$.$RPEntity$.$v$;
$module$build$ts$entity$NPC$default$$.$NPC$.prototype.set = function($key$jscomp$102$$, $value$jscomp$135$$) {
  $module$build$ts$entity$RPEntity$default$$.$RPEntity$.prototype.set.call(this, $key$jscomp$102$$, $value$jscomp$135$$);
  "name" === $key$jscomp$102$$ && $value$jscomp$135$$.startsWith("Zekiel") && (this.$F$ = -32);
};
$module$build$ts$entity$NPC$default$$.$NPC$.prototype.$drawTop$ = function($ctx$jscomp$54$$) {
  var $localX$jscomp$8$$ = 32 * this._x, $localY$jscomp$8$$ = 32 * this._y;
  "undefined" == typeof this.no_hpbar && $JSCompiler_StaticMethods_drawHealthBar$$(this, $ctx$jscomp$54$$, $localX$jscomp$8$$, $localY$jscomp$8$$ + this.$D$);
  "undefined" == typeof this.unnamed && $JSCompiler_StaticMethods_drawTitle$$(this, $ctx$jscomp$54$$, $localX$jscomp$8$$, $localY$jscomp$8$$ + this.$D$);
};
$module$build$ts$entity$NPC$default$$.$NPC$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/look.png) 1 3, auto";
};
var $module$build$ts$entity$Sign$default$$ = {};
Object.defineProperty($module$build$ts$entity$Sign$default$$, "__esModule", {value:!0});
$module$build$ts$entity$Sign$default$$.$Sign$ = void 0;
$module$build$ts$entity$Sign$default$$.$Sign$ = function() {
  var $$jscomp$super$this$jscomp$66$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$66$$.zIndex = 5000;
  $$jscomp$super$this$jscomp$66$$["class"] = "default";
  return $$jscomp$super$this$jscomp$66$$;
};
$$jscomp$inherits$$($module$build$ts$entity$Sign$default$$.$Sign$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$Sign$default$$.$Sign$.prototype.$draw$ = function($ctx$jscomp$55$$) {
  this.$C$ || (this.$C$ = $stendhal$$.$paths$.$sprites$ + "/signs/" + this["class"] + ".png");
  var $image$jscomp$17$$ = $stendhal$$.data.$sprites$.get(this.$C$);
  $image$jscomp$17$$.height && $ctx$jscomp$55$$.drawImage($image$jscomp$17$$, 32 * this.x, 32 * this.y);
};
$module$build$ts$entity$Sign$default$$.$Sign$.prototype.$isVisibleToAction$ = function() {
  return !0;
};
$module$build$ts$entity$Sign$default$$.$Sign$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/look.png) 1 3, auto";
};
var $module$build$ts$entity$TrainingDummy$default$$ = {};
Object.defineProperty($module$build$ts$entity$TrainingDummy$default$$, "__esModule", {value:!0});
$module$build$ts$entity$TrainingDummy$default$$.$TrainingDummy$ = void 0;
$module$build$ts$entity$TrainingDummy$default$$.$TrainingDummy$ = function() {
  return $module$build$ts$entity$NPC$default$$.$NPC$.apply(this, arguments) || this;
};
$$jscomp$inherits$$($module$build$ts$entity$TrainingDummy$default$$.$TrainingDummy$, $module$build$ts$entity$NPC$default$$.$NPC$);
$module$build$ts$entity$TrainingDummy$default$$.$TrainingDummy$.$v$ = $module$build$ts$entity$NPC$default$$.$NPC$.$v$;
$module$build$ts$entity$TrainingDummy$default$$.$TrainingDummy$.prototype.onclick = function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"attack", target:"#" + this.id});
};
$module$build$ts$entity$TrainingDummy$default$$.$TrainingDummy$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/attack.png) 1 3, auto";
};
var $module$build$ts$entity$UnknownEntity$default$$ = {};
Object.defineProperty($module$build$ts$entity$UnknownEntity$default$$, "__esModule", {value:!0});
$module$build$ts$entity$UnknownEntity$default$$.$UnknownEntity$ = void 0;
$module$build$ts$entity$UnknownEntity$default$$.$UnknownEntity$ = function() {
  var $$jscomp$super$this$jscomp$67$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$67$$.zIndex = 1;
  setTimeout(function() {
    $$jscomp$super$this$jscomp$67$$._rpclass && console.log("Unknown entity", $$jscomp$super$this$jscomp$67$$._rpclass, "at", $marauroa$$.$currentZoneName$, $$jscomp$super$this$jscomp$67$$.x, $$jscomp$super$this$jscomp$67$$.y, "is", $$jscomp$super$this$jscomp$67$$);
  }, 1);
  return $$jscomp$super$this$jscomp$67$$;
};
$$jscomp$inherits$$($module$build$ts$entity$UnknownEntity$default$$.$UnknownEntity$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$UnknownEntity$default$$.$UnknownEntity$.prototype.$isVisibleToAction$ = function() {
  return $marauroa$$.$me$.adminlevel && 600 <= $marauroa$$.$me$.adminlevel;
};
var $module$build$ts$entity$UseableEntity$default$$ = {};
Object.defineProperty($module$build$ts$entity$UseableEntity$default$$, "__esModule", {value:!0});
$module$build$ts$entity$UseableEntity$default$$.$UseableEntity$ = void 0;
$module$build$ts$entity$UseableEntity$default$$.$UseableEntity$ = function() {
  var $$jscomp$super$this$jscomp$68$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$68$$.zIndex = 3000;
  $$jscomp$super$this$jscomp$68$$.action = "use";
  $$jscomp$super$this$jscomp$68$$.$sprite$ = {height:32, width:32};
  return $$jscomp$super$this$jscomp$68$$;
};
$$jscomp$inherits$$($module$build$ts$entity$UseableEntity$default$$.$UseableEntity$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$UseableEntity$default$$.$UseableEntity$.prototype.set = function($key$jscomp$103$$, $value$jscomp$136$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$103$$, $value$jscomp$136$$);
  if ("class" === $key$jscomp$103$$ || "name" === $key$jscomp$103$$) {
    this.$sprite$.filename = $stendhal$$.$paths$.$sprites$ + "/" + this["class"] + "/" + this._name + ".png";
  }
  "state" === $key$jscomp$103$$ && (this.$sprite$.offsetY = 32 * this.state);
};
$module$build$ts$entity$UseableEntity$default$$.$UseableEntity$.prototype.$isVisibleToAction$ = function() {
  return !0;
};
var $module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$.$OutfitPartSelector$ = void 0;
$module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$.$OutfitPartSelector$ = function($canvas$jscomp$11_part$jscomp$2$$, $initialIndex$$, $maxindex$$, $onPartChanged$$) {
  this.$G$ = $canvas$jscomp$11_part$jscomp$2$$;
  this.$D$ = $onPartChanged$$;
  this.$A$ = parseInt($initialIndex$$, 10);
  this.$C$ = $maxindex$$;
  $canvas$jscomp$11_part$jscomp$2$$ = document.getElementById("setoutfit" + $canvas$jscomp$11_part$jscomp$2$$ + "canvas");
  $canvas$jscomp$11_part$jscomp$2$$.style.margin = "5px";
  this.$B$ = $canvas$jscomp$11_part$jscomp$2$$.getContext("2d");
  this.$I$ = $canvas$jscomp$11_part$jscomp$2$$.width;
  this.$H$ = $canvas$jscomp$11_part$jscomp$2$$.height;
  this.$v$ = this.$F$ = void 0;
};
function $JSCompiler_StaticMethods_indexString$$($index$jscomp$87$$) {
  return -1 < $index$jscomp$87$$ && 100 > $index$jscomp$87$$ ? 10 > $index$jscomp$87$$ ? "00" + $index$jscomp$87$$ : "0" + $index$jscomp$87$$ : "" + $index$jscomp$87$$;
}
$module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$.$OutfitPartSelector$.prototype.$draw$ = function() {
  var $$jscomp$this$jscomp$42$$ = this, $image$jscomp$18$$ = $JSCompiler_StaticMethods__getPartSprite$$(this, this.$G$, this.$A$, this.$F$);
  this.$v$ = $image$jscomp$18$$;
  this.$B$.fillStyle = "white";
  this.$B$.fillRect(0, 0, this.$I$, this.$H$);
  $image$jscomp$18$$.then(function($img$jscomp$17$$) {
    $$jscomp$this$jscomp$42$$.$B$.drawImage($img$jscomp$17$$, -48, -128);
    $$jscomp$this$jscomp$42$$.$D$();
  });
};
$module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$.$OutfitPartSelector$.prototype.$previous$ = function() {
  var $numOutfits$$ = this.$C$ + 1;
  this.$A$ += this.$C$;
  this.$A$ %= $numOutfits$$;
  this.$draw$();
  this.$D$();
};
$module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$.$OutfitPartSelector$.prototype.next = function() {
  var $numOutfits$jscomp$1$$ = this.$C$ + 1;
  this.$A$++;
  this.$A$ %= $numOutfits$jscomp$1$$;
  this.$draw$();
  this.$D$();
};
function $JSCompiler_StaticMethods__getPartSprite$$($JSCompiler_StaticMethods__getPartSprite$self_fname$$, $part$jscomp$3$$, $index$jscomp$88$$, $color$jscomp$9$$) {
  $color$jscomp$9$$ = void 0 === $color$jscomp$9$$ ? null : $color$jscomp$9$$;
  var $suffix$jscomp$1$$ = ".png";
  "body" === $JSCompiler_StaticMethods__getPartSprite$self_fname$$.$G$ && $JSCompiler_StaticMethods_getBoolean$$($stendhal$$.$config$, "gamescreen.nonude") && ($suffix$jscomp$1$$ = "-nonude.png");
  $JSCompiler_StaticMethods__getPartSprite$self_fname$$ = $stendhal$$.$paths$.$sprites$ + "/outfit/" + $part$jscomp$3$$ + "/" + $JSCompiler_StaticMethods_indexString$$($index$jscomp$88$$) + $suffix$jscomp$1$$;
  return null != $color$jscomp$9$$ ? $JSCompiler_StaticMethods_getFilteredWithPromise$$($JSCompiler_StaticMethods__getPartSprite$self_fname$$, $color$jscomp$9$$) : $JSCompiler_StaticMethods_getWithPromise$$($JSCompiler_StaticMethods__getPartSprite$self_fname$$);
}
$$jscomp$global$$.Object.defineProperties($module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$.$OutfitPartSelector$.prototype, {image:{configurable:!0, enumerable:!0, get:function() {
  return this.$v$;
}}, index:{configurable:!0, enumerable:!0, get:function() {
  return $JSCompiler_StaticMethods_indexString$$(this.$A$);
}, set:function($newIndex$$) {
  this.$A$ = parseInt($newIndex$$, 10);
  this.$draw$();
}}, color:{configurable:!0, enumerable:!0, set:function($newColor$$) {
  this.$F$ = $newColor$$;
  this.$draw$();
}}});
var $module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$ = void 0;
$module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$ = function($canvas$jscomp$12$$, $gradientCanvas$$, $onColorChanged$$) {
  var $$jscomp$this$jscomp$43$$ = this;
  this.$ctx$ = $canvas$jscomp$12$$.getContext("2d");
  this.$G$ = $gradientCanvas$$.getContext("2d");
  this.$v$ = this.$_createBaseImage$($canvas$jscomp$12$$.width, $canvas$jscomp$12$$.height);
  this.$I$ = $onColorChanged$$;
  this.$J$ = !1;
  this.$A$ = this.$v$.width / 2;
  this.$B$ = this.$v$.height / 2;
  this.$C$ = this.$A$;
  $canvas$jscomp$12$$.addEventListener("mousedown", function($e$jscomp$96$$) {
    return $$jscomp$this$jscomp$43$$.$_onMouseDown$($e$jscomp$96$$);
  });
  $canvas$jscomp$12$$.addEventListener("mousemove", function($e$jscomp$97$$) {
    $e$jscomp$97$$.buttons && $$jscomp$this$jscomp$43$$.$_onMouseDown$($e$jscomp$97$$);
  });
  $gradientCanvas$$.addEventListener("mousedown", function($e$jscomp$98$$) {
    return $JSCompiler_StaticMethods__onMouseDownGrad$$($$jscomp$this$jscomp$43$$, $e$jscomp$98$$);
  });
  $gradientCanvas$$.addEventListener("mousemove", function($e$jscomp$99$$) {
    $e$jscomp$99$$.buttons && $JSCompiler_StaticMethods__onMouseDownGrad$$($$jscomp$this$jscomp$43$$, $e$jscomp$99$$);
  });
  $gradientCanvas$$.style.margin = "5px 0px 0px 0px";
};
$JSCompiler_prototypeAlias$$ = $module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$.prototype;
$JSCompiler_prototypeAlias$$.$_createBaseImage$ = function($width$jscomp$34$$, $height$jscomp$33$$) {
  var $img$jscomp$18$$ = document.createElement("canvas");
  $img$jscomp$18$$.width = $width$jscomp$34$$;
  $img$jscomp$18$$.height = $height$jscomp$33$$;
  for (var $ctx$jscomp$56$$ = $img$jscomp$18$$.getContext("2d"), $x$jscomp$160$$ = 0; $x$jscomp$160$$ < $width$jscomp$34$$; $x$jscomp$160$$++) {
    for (var $y$jscomp$140$$ = 0; $y$jscomp$140$$ < $height$jscomp$33$$; $y$jscomp$140$$++) {
      $ctx$jscomp$56$$.fillStyle = $JSCompiler_StaticMethods__rgbToCssString$$($stendhal$$.data.$sprites$.filter.$hsl2rgb$([$x$jscomp$160$$ / $width$jscomp$34$$, 1 - $y$jscomp$140$$ / $height$jscomp$33$$, 0.5])), $ctx$jscomp$56$$.fillRect($x$jscomp$160$$, $y$jscomp$140$$, 1, 1);
    }
  }
  return $img$jscomp$18$$;
};
function $JSCompiler_StaticMethods__rgbToCssString$$($rgb$jscomp$4$$) {
  return "rgb(".concat($rgb$jscomp$4$$[0], ",", $rgb$jscomp$4$$[1], ",", $rgb$jscomp$4$$[2], ")");
}
$JSCompiler_prototypeAlias$$.$draw$ = function() {
  this.enabled ? (this.$ctx$.drawImage(this.$v$, 0, 0), this.$_drawSelection$()) : (this.$ctx$.fillStyle = "gray", this.$ctx$.fillRect(0, 0, this.$v$.width, this.$v$.height));
  if (this.enabled) {
    var $gradient$jscomp$inline_947$$ = this.$G$.createLinearGradient(0, 0, this.$v$.width, 0), $ctx$jscomp$inline_949_stops$jscomp$inline_948$$ = this.$_calculateGradientStops$();
    $gradient$jscomp$inline_947$$.addColorStop(0, $JSCompiler_StaticMethods__rgbToCssString$$($ctx$jscomp$inline_949_stops$jscomp$inline_948$$[0]));
    $gradient$jscomp$inline_947$$.addColorStop(0.5, $JSCompiler_StaticMethods__rgbToCssString$$($ctx$jscomp$inline_949_stops$jscomp$inline_948$$[1]));
    $gradient$jscomp$inline_947$$.addColorStop(1, $JSCompiler_StaticMethods__rgbToCssString$$($ctx$jscomp$inline_949_stops$jscomp$inline_948$$[2]));
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$ = this.$G$;
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$.fillStyle = $gradient$jscomp$inline_947$$;
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$.fillRect(0, 0, this.$v$.width, 10);
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$.fillStyle = "black";
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$.beginPath();
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$.moveTo(this.$C$, 0);
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$.lineTo(this.$C$, 10);
    $ctx$jscomp$inline_949_stops$jscomp$inline_948$$.stroke();
  } else {
    this.$G$.fillStyle = "gray", this.$G$.fillRect(0, 0, this.$v$.width, 10);
  }
};
$JSCompiler_prototypeAlias$$.$_drawSelection$ = function() {
  this.$ctx$.strokeStyle = "black";
  this.$ctx$.beginPath();
  this.$ctx$.moveTo(this.$A$, 0);
  this.$ctx$.lineTo(this.$A$, this.$v$.height);
  this.$ctx$.moveTo(0, this.$B$);
  this.$ctx$.lineTo(this.$v$.width, this.$B$);
  this.$ctx$.stroke();
};
$JSCompiler_prototypeAlias$$.$_calculateGradientStops$ = function() {
  var $width$jscomp$35$$ = this.$v$.width, $height$jscomp$34$$ = this.$v$.height;
  return [$stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.$A$ / $width$jscomp$35$$, 1 - this.$B$ / $height$jscomp$34$$, 0.08]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.$A$ / $width$jscomp$35$$, 1 - this.$B$ / $height$jscomp$34$$, 0.5]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.$A$ / $width$jscomp$35$$, 1 - this.$B$ / $height$jscomp$34$$, 0.92])];
};
$JSCompiler_prototypeAlias$$.$_onMouseDown$ = function($event$jscomp$69$$) {
  this.enabled && (this.$A$ = $event$jscomp$69$$.offsetX, this.$B$ = $event$jscomp$69$$.offsetY, this.$draw$(), this.$I$(this.color));
};
function $JSCompiler_StaticMethods__onMouseDownGrad$$($JSCompiler_StaticMethods__onMouseDownGrad$self$$, $event$jscomp$71$$) {
  $JSCompiler_StaticMethods__onMouseDownGrad$self$$.enabled && ($JSCompiler_StaticMethods__onMouseDownGrad$self$$.$C$ = $event$jscomp$71$$.offsetX, $JSCompiler_StaticMethods__onMouseDownGrad$self$$.$draw$(), $JSCompiler_StaticMethods__onMouseDownGrad$self$$.$I$($JSCompiler_StaticMethods__onMouseDownGrad$self$$.color));
}
$$jscomp$global$$.Object.defineProperties($module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$.prototype, {enabled:{configurable:!0, enumerable:!0, set:function($value$jscomp$137$$) {
  this.$J$ = $value$jscomp$137$$ ? !0 : !1;
  this.$draw$();
  this.$I$(this.color);
}, get:function() {
  return this.$J$;
}}, color:{configurable:!0, enumerable:!0, get:function() {
  return this.enabled ? $stendhal$$.data.$sprites$.filter.$mergergb$($stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.$A$ / this.$v$.width, 1 - this.$B$ / this.$v$.height, this.$C$ / this.$v$.width])) : null;
}, set:function($hsl$jscomp$4_rgb$jscomp$5$$) {
  null != $hsl$jscomp$4_rgb$jscomp$5$$ ? ($hsl$jscomp$4_rgb$jscomp$5$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$($stendhal$$.data.$sprites$.filter.$splitrgb$($hsl$jscomp$4_rgb$jscomp$5$$)), this.$A$ = $hsl$jscomp$4_rgb$jscomp$5$$[0] * this.$v$.width, this.$B$ = (1 - $hsl$jscomp$4_rgb$jscomp$5$$[1]) * this.$v$.height, this.$C$ = $hsl$jscomp$4_rgb$jscomp$5$$[2] * this.$v$.width, this.enabled = !0) : this.enabled = !1;
}}});
var $module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$ = void 0;
$module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$ = function($$jscomp$super$this$jscomp$69_canvas$jscomp$13$$, $gradientCanvas$jscomp$1$$, $onColorChanged$jscomp$1$$) {
  $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$ = $module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$.call(this, $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$, $gradientCanvas$jscomp$1$$, $onColorChanged$jscomp$1$$) || this;
  $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$F$ = $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$v$.width / 4;
  $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$D$ = $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$v$.height / 4;
  $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$A$ = 0;
  $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$B$ = 0;
  $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$C$ = $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$.$v$.width / 2;
  return $$jscomp$super$this$jscomp$69_canvas$jscomp$13$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$, $module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$);
$module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$.prototype.$_createBaseImage$ = function($blockWidth_width$jscomp$36$$, $blockHeight_height$jscomp$35$$) {
  for (var $hues$jscomp$inline_952_img$jscomp$19$$ = [0.05, 0.07, 0.09, 0.11], $ctx$jscomp$58_saturations$jscomp$inline_953$$ = [0.70, 0.55, 0.40, 0.25], $hsMap$jscomp$inline_954_x$jscomp$161$$ = [[], [], [], []], $colors$jscomp$inline_955_y$jscomp$141$$ = [[], [], [], []], $i$jscomp$inline_956$$ = 0; 4 > $i$jscomp$inline_956$$; $i$jscomp$inline_956$$++) {
    for (var $j$jscomp$inline_957$$ = 0; 4 > $j$jscomp$inline_957$$; $j$jscomp$inline_957$$++) {
      var $hue$jscomp$inline_958$$ = $hues$jscomp$inline_952_img$jscomp$19$$[$j$jscomp$inline_957$$], $sat$jscomp$inline_959$$ = $ctx$jscomp$58_saturations$jscomp$inline_953$$[$i$jscomp$inline_956$$];
      $hsMap$jscomp$inline_954_x$jscomp$161$$[$i$jscomp$inline_956$$].push([$hue$jscomp$inline_958$$, $sat$jscomp$inline_959$$]);
      $colors$jscomp$inline_955_y$jscomp$141$$[$i$jscomp$inline_956$$].push($stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hue$jscomp$inline_958$$, $sat$jscomp$inline_959$$, 0.5]));
    }
  }
  this.$H$ = $hsMap$jscomp$inline_954_x$jscomp$161$$;
  this.$K$ = $colors$jscomp$inline_955_y$jscomp$141$$;
  $hues$jscomp$inline_952_img$jscomp$19$$ = document.createElement("canvas");
  $hues$jscomp$inline_952_img$jscomp$19$$.width = $blockWidth_width$jscomp$36$$;
  $hues$jscomp$inline_952_img$jscomp$19$$.height = $blockHeight_height$jscomp$35$$;
  $ctx$jscomp$58_saturations$jscomp$inline_953$$ = $hues$jscomp$inline_952_img$jscomp$19$$.getContext("2d");
  $blockWidth_width$jscomp$36$$ /= 4;
  $blockHeight_height$jscomp$35$$ /= 4;
  for ($hsMap$jscomp$inline_954_x$jscomp$161$$ = 0; 4 > $hsMap$jscomp$inline_954_x$jscomp$161$$; $hsMap$jscomp$inline_954_x$jscomp$161$$++) {
    for ($colors$jscomp$inline_955_y$jscomp$141$$ = 0; 4 > $colors$jscomp$inline_955_y$jscomp$141$$; $colors$jscomp$inline_955_y$jscomp$141$$++) {
      $ctx$jscomp$58_saturations$jscomp$inline_953$$.fillStyle = $JSCompiler_StaticMethods__rgbToCssString$$(this.$K$[$hsMap$jscomp$inline_954_x$jscomp$161$$][$colors$jscomp$inline_955_y$jscomp$141$$]), $ctx$jscomp$58_saturations$jscomp$inline_953$$.fillRect($hsMap$jscomp$inline_954_x$jscomp$161$$ * $blockWidth_width$jscomp$36$$, $colors$jscomp$inline_955_y$jscomp$141$$ * $blockHeight_height$jscomp$35$$, $blockWidth_width$jscomp$36$$, $blockHeight_height$jscomp$35$$);
    }
  }
  return $hues$jscomp$inline_952_img$jscomp$19$$;
};
$module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$.prototype.$_calculateGradientStops$ = function() {
  var $hs$$ = this.$H$[Math.floor(this.$A$ / this.$F$)][Math.floor(this.$B$ / this.$D$)];
  return [$stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$$[0], $hs$$[1], 0.08]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$$[0], $hs$$[1], 0.5]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$$[0], $hs$$[1], 0.92])];
};
$module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$.prototype.$_drawSelection$ = function() {
  var $x$jscomp$162$$ = Math.floor(this.$A$ / this.$F$), $y$jscomp$142$$ = Math.floor(this.$B$ / this.$D$);
  this.$ctx$.strokeStyle = "white";
  this.$ctx$.strokeRect($x$jscomp$162$$ * this.$F$, $y$jscomp$142$$ * this.$D$, this.$F$, this.$D$);
};
$$jscomp$global$$.Object.defineProperties($module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$.prototype, {color:{configurable:!0, enumerable:!0, get:function() {
  if (this.enabled) {
    var $hs$jscomp$1$$ = this.$H$[Math.floor(this.$A$ / this.$F$)][Math.floor(this.$B$ / this.$D$)];
    return $stendhal$$.data.$sprites$.filter.$mergergb$($stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$jscomp$1$$[0], $hs$jscomp$1$$[1], this.$C$ / this.$v$.width]));
  }
  return null;
}, set:function($hsl$jscomp$6_rgb$jscomp$7$$) {
  if (null != $hsl$jscomp$6_rgb$jscomp$7$$) {
    this.enabled = !0;
    $hsl$jscomp$6_rgb$jscomp$7$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$($stendhal$$.data.$sprites$.filter.$splitrgb$($hsl$jscomp$6_rgb$jscomp$7$$));
    this.$C$ = $hsl$jscomp$6_rgb$jscomp$7$$[2] * this.$v$.width;
    for (var $bestDelta$$ = Number.MAX_VALUE, $i$jscomp$99$$ = 0; 4 > $i$jscomp$99$$; $i$jscomp$99$$++) {
      for (var $j$jscomp$7$$ = 0; 4 > $j$jscomp$7$$; $j$jscomp$7$$++) {
        var $hs$jscomp$2_satDelta$$ = this.$H$[$i$jscomp$99$$][$j$jscomp$7$$], $delta$jscomp$5_hueDelta$$ = $hs$jscomp$2_satDelta$$[0] - $hsl$jscomp$6_rgb$jscomp$7$$[0];
        $hs$jscomp$2_satDelta$$ = $hs$jscomp$2_satDelta$$[1] - $hsl$jscomp$6_rgb$jscomp$7$$[1];
        $delta$jscomp$5_hueDelta$$ = $delta$jscomp$5_hueDelta$$ * $delta$jscomp$5_hueDelta$$ + $hs$jscomp$2_satDelta$$ * $hs$jscomp$2_satDelta$$;
        $delta$jscomp$5_hueDelta$$ < $bestDelta$$ && ($bestDelta$$ = $delta$jscomp$5_hueDelta$$, this.$A$ = $i$jscomp$99$$ * this.$F$, this.$B$ = $j$jscomp$7$$ * this.$D$);
      }
    }
  } else {
    this.enabled = !1;
  }
}}});
var $module$build$ts$ui$dialog$outfit$OutfitDialog$default$$ = {};
Object.defineProperty($module$build$ts$ui$dialog$outfit$OutfitDialog$default$$, "__esModule", {value:!0});
$module$build$ts$ui$dialog$outfit$OutfitDialog$default$$.$OutfitDialog$ = void 0;
$module$build$ts$ui$dialog$outfit$OutfitDialog$default$$.$OutfitDialog$ = function() {
  var $$jscomp$super$this$jscomp$70$$ = $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$.call(this, "outfitdialog-template") || this;
  $JSCompiler_StaticMethods_registerComponent$$($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$OutfitDialog$, $$jscomp$super$this$jscomp$70$$);
  queueMicrotask(function() {
    $JSCompiler_StaticMethods_createDialog$$($$jscomp$super$this$jscomp$70$$);
  });
  return $$jscomp$super$this$jscomp$70$$;
};
$$jscomp$inherits$$($module$build$ts$ui$dialog$outfit$OutfitDialog$default$$.$OutfitDialog$, $module$build$ts$ui$toolkit$DialogContentComponent$default$$.$DialogContentComponent$);
function $JSCompiler_StaticMethods_createDialog$$($JSCompiler_StaticMethods_createDialog$self$$) {
  var $entries_outfit$jscomp$1$$ = $marauroa$$.$me$.outfit_ext_orig;
  void 0 === $entries_outfit$jscomp$1$$ && ($entries_outfit$jscomp$1$$ = $marauroa$$.$me$.outfit_ext);
  $entries_outfit$jscomp$1$$ = $entries_outfit$jscomp$1$$.split(",");
  for (var $currentOutfit$$ = {}, $i$jscomp$100$$ = 0; $i$jscomp$100$$ < $entries_outfit$jscomp$1$$.length; $i$jscomp$100$$++) {
    var $entry$jscomp$13$$ = $entries_outfit$jscomp$1$$[$i$jscomp$100$$].split("=");
    $currentOutfit$$[$entry$jscomp$13$$[0]] = $entry$jscomp$13$$[1];
  }
  $JSCompiler_StaticMethods_createDialog$self$$.$G$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "hat", $currentOutfit$$.hat);
  $JSCompiler_StaticMethods_createDialog$self$$.$D$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "hair", $currentOutfit$$.hair);
  $JSCompiler_StaticMethods_createDialog$self$$.$I$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "mask", $currentOutfit$$.mask);
  $JSCompiler_StaticMethods_createDialog$self$$.$C$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "eyes", $currentOutfit$$.eyes);
  $JSCompiler_StaticMethods_createDialog$self$$.$J$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "mouth", $currentOutfit$$.mouth);
  $JSCompiler_StaticMethods_createDialog$self$$.$H$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "head", $currentOutfit$$.head);
  $JSCompiler_StaticMethods_createDialog$self$$.$A$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "body", $currentOutfit$$.body);
  $JSCompiler_StaticMethods_createDialog$self$$.$B$ = $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_createDialog$self$$, "dress", $currentOutfit$$.dress);
  $JSCompiler_StaticMethods_createDialog$self$$.$M$ = $JSCompiler_StaticMethods_createDialog$self$$.$v$($module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$, "hair", $JSCompiler_StaticMethods_createDialog$self$$.$D$);
  $JSCompiler_StaticMethods_createDialog$self$$.$L$ = $JSCompiler_StaticMethods_createDialog$self$$.$v$($module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$, "eyes", $JSCompiler_StaticMethods_createDialog$self$$.$C$);
  $JSCompiler_StaticMethods_createDialog$self$$.$K$ = $JSCompiler_StaticMethods_createDialog$self$$.$v$($module$build$ts$ui$dialog$outfit$OutfitColorSelector$default$$.$OutfitColorSelector$, "dress", $JSCompiler_StaticMethods_createDialog$self$$.$B$);
  $JSCompiler_StaticMethods_createDialog$self$$.$N$ = $JSCompiler_StaticMethods_createDialog$self$$.$v$($module$build$ts$ui$dialog$outfit$OutfitPaletteColorSelector$default$$.$OutfitPaletteColorSelector$, "skin", $JSCompiler_StaticMethods_createDialog$self$$.$H$, $JSCompiler_StaticMethods_createDialog$self$$.$A$);
  $JSCompiler_StaticMethods_drawComposite$$($JSCompiler_StaticMethods_createDialog$self$$);
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_createDialog$self$$, "#setoutfitcancel").addEventListener("click", function($event$jscomp$73$$) {
    $JSCompiler_StaticMethods_createDialog$self$$.$componentElement$.dispatchEvent(new Event("close"));
    $event$jscomp$73$$.preventDefault();
  });
  $JSCompiler_StaticMethods_child$$($JSCompiler_StaticMethods_createDialog$self$$, "#setoutfitapply").addEventListener("click", function($event$jscomp$74$$) {
    var $action$jscomp$inline_966$$ = {type:"outfit_ext", zone:$marauroa$$.$currentZoneName$, value:"body=" + $JSCompiler_StaticMethods_createDialog$self$$.$A$.index.toString() + ",dress=" + $JSCompiler_StaticMethods_createDialog$self$$.$B$.index.toString() + ",head=" + $JSCompiler_StaticMethods_createDialog$self$$.$H$.index.toString() + ",mouth=" + $JSCompiler_StaticMethods_createDialog$self$$.$J$.index.toString() + ",eyes=" + $JSCompiler_StaticMethods_createDialog$self$$.$C$.index.toString() + 
    ",mask=" + $JSCompiler_StaticMethods_createDialog$self$$.$I$.index.toString() + ",hair=" + $JSCompiler_StaticMethods_createDialog$self$$.$D$.index.toString() + ",hat=" + $JSCompiler_StaticMethods_createDialog$self$$.$G$.index.toString()}, $color$jscomp$inline_967$$ = $JSCompiler_StaticMethods_createDialog$self$$.$M$.color;
    null != $color$jscomp$inline_967$$ && ($action$jscomp$inline_966$$.hair = $color$jscomp$inline_967$$.toString());
    $color$jscomp$inline_967$$ = $JSCompiler_StaticMethods_createDialog$self$$.$L$.color;
    null != $color$jscomp$inline_967$$ && ($action$jscomp$inline_966$$.eyes = $color$jscomp$inline_967$$.toString());
    $color$jscomp$inline_967$$ = $JSCompiler_StaticMethods_createDialog$self$$.$K$.color;
    null != $color$jscomp$inline_967$$ && ($action$jscomp$inline_966$$.dress = $color$jscomp$inline_967$$.toString());
    $color$jscomp$inline_967$$ = $JSCompiler_StaticMethods_createDialog$self$$.$N$.color;
    null != $color$jscomp$inline_967$$ && ($action$jscomp$inline_966$$.skin = $color$jscomp$inline_967$$.toString());
    $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$inline_966$$);
    $event$jscomp$74$$.preventDefault();
    $JSCompiler_StaticMethods_createDialog$self$$.$componentElement$.dispatchEvent(new Event("close"));
  });
}
function $JSCompiler_StaticMethods_makeSelector$$($JSCompiler_StaticMethods_makeSelector$self$$, $part$jscomp$4$$, $index$jscomp$89$$) {
  if (0 > $index$jscomp$89$$ || void 0 === $index$jscomp$89$$) {
    $index$jscomp$89$$ = 0;
  }
  var $selector$jscomp$2$$ = new $module$build$ts$ui$dialog$outfit$OutfitPartSelector$default$$.$OutfitPartSelector$($part$jscomp$4$$, $index$jscomp$89$$, $stendhal$$.data.$outfit$.count[$part$jscomp$4$$] - 1, function() {
    $JSCompiler_StaticMethods_drawComposite$$($JSCompiler_StaticMethods_makeSelector$self$$);
  });
  document.getElementById("setoutfitprev" + $part$jscomp$4$$).addEventListener("click", function() {
    $selector$jscomp$2$$.$previous$();
  });
  document.getElementById("setoutfitnext" + $part$jscomp$4$$).addEventListener("click", function() {
    $selector$jscomp$2$$.next();
  });
  $selector$jscomp$2$$.$draw$();
  return $selector$jscomp$2$$;
}
function $JSCompiler_StaticMethods_drawComposite$$($JSCompiler_StaticMethods_drawComposite$self$$) {
  function $draw$$($ctx$jscomp$60$$, $image$jscomp$19_selector$jscomp$3$$) {
    $image$jscomp$19_selector$jscomp$3$$ = $image$jscomp$19_selector$jscomp$3$$.image;
    null === $image$jscomp$19_selector$jscomp$3$$ || void 0 === $image$jscomp$19_selector$jscomp$3$$ ? void 0 : $image$jscomp$19_selector$jscomp$3$$.then(function($img$jscomp$20$$) {
      return $ctx$jscomp$60$$.drawImage($img$jscomp$20$$, -48, -128);
    });
  }
  var $canvas$jscomp$14$$ = document.getElementById("setoutfitcompositecanvas"), $ctx$jscomp$59$$ = $canvas$jscomp$14$$.getContext("2d");
  $ctx$jscomp$59$$.fillStyle = "white";
  $ctx$jscomp$59$$.fillRect(0, 0, $canvas$jscomp$14$$.width, $canvas$jscomp$14$$.height);
  $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$A$);
  $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$B$);
  $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$H$);
  $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$J$);
  $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$C$);
  $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$I$);
  0 > $stendhal$$.data.$outfit$.$hats_no_hair$.indexOf(parseInt($JSCompiler_StaticMethods_drawComposite$self$$.$G$.index, 10)) && $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$D$);
  $draw$$($ctx$jscomp$59$$, $JSCompiler_StaticMethods_drawComposite$self$$.$G$);
}
function $JSCompiler_StaticMethods_initialColorValue$$($layer_color_part$jscomp$5$$) {
  var $colors$jscomp$2$$ = $marauroa$$.$me$.outfit_colors;
  if (null != $colors$jscomp$2$$) {
    var $colorName$$ = $layer_color_part$jscomp$5$$;
    if ("body" === $layer_color_part$jscomp$5$$ || "head" === $layer_color_part$jscomp$5$$) {
      $colorName$$ = "skin";
    }
    $layer_color_part$jscomp$5$$ = $colors$jscomp$2$$[$colorName$$ + "_orig"];
    void 0 === $layer_color_part$jscomp$5$$ && ($layer_color_part$jscomp$5$$ = $colors$jscomp$2$$[$colorName$$]);
    return $layer_color_part$jscomp$5$$;
  }
  return null;
}
$module$build$ts$ui$dialog$outfit$OutfitDialog$default$$.$OutfitDialog$.prototype.$v$ = function($classObject$$, $part$jscomp$6$$) {
  var $partSelectors$$ = $$jscomp$getRestArguments$$.apply(2, arguments), $toggle$$ = document.getElementById("setoutfit" + $part$jscomp$6$$ + "colortoggle"), $selector$jscomp$4$$ = new $classObject$$(document.getElementById("setoutfit" + $part$jscomp$6$$ + "colorcanvas"), document.getElementById("setoutfit" + $part$jscomp$6$$ + "colorgradient"), function($color$jscomp$12$$) {
    for (var $$jscomp$iter$87$$ = $$jscomp$makeIterator$$($partSelectors$$), $$jscomp$key$partSelector$$ = $$jscomp$iter$87$$.next(); !$$jscomp$key$partSelector$$.done; $$jscomp$key$partSelector$$ = $$jscomp$iter$87$$.next()) {
      $$jscomp$key$partSelector$$.value.color = $color$jscomp$12$$;
    }
  }), $initialColor$$ = $JSCompiler_StaticMethods_initialColorValue$$($part$jscomp$6$$);
  null != $initialColor$$ && ($toggle$$.checked = !0, $selector$jscomp$4$$.color = $initialColor$$);
  $toggle$$.addEventListener("change", function() {
    $selector$jscomp$4$$.enabled = $toggle$$.checked;
  });
  $selector$jscomp$4$$.$draw$();
  return $selector$jscomp$4$$;
};
$module$build$ts$ui$dialog$outfit$OutfitDialog$default$$.$OutfitDialog$.prototype.$onParentClose$ = function() {
  $JSCompiler_StaticMethods_unregisterComponent$$(this);
};
var $module$build$ts$entity$User$default$$ = {};
Object.defineProperty($module$build$ts$entity$User$default$$, "__esModule", {value:!0});
$module$build$ts$entity$User$default$$.$User$ = void 0;
$module$build$ts$entity$User$default$$.$User$ = function() {
  var $$jscomp$super$this$jscomp$71$$ = $module$build$ts$entity$Player$default$$.$Player$.call(this) || this;
  $$jscomp$super$this$jscomp$71$$.$soundMan$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$v$();
  $$jscomp$super$this$jscomp$71$$.$C$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$M$();
  $$jscomp$super$this$jscomp$71$$.$minimapStyle$ = $module$build$ts$util$Color$default$$.$Color$.$L$;
  queueMicrotask(function() {
    for (var $JSCompiler_StaticMethods_onZoneReady$self$jscomp$inline_1140$$ = $$jscomp$super$this$jscomp$71$$.$C$, $$jscomp$inline_1141$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getZoneEntities$$()), $$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$ = $$jscomp$inline_1141$$.next(); !$$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$.done; $$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$ = 
    $$jscomp$inline_1141$$.next()) {
      var $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ = $$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$.value;
      if (!$JSCompiler_StaticMethods_isLoaded$$($JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$)) {
        $$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$ = $JSCompiler_StaticMethods_onZoneReady$self$jscomp$inline_1140$$;
        var $id$jscomp$inline_1169$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.id;
        if ($marauroa$$.$me$) {
          if ($$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$.$JSC$2020_sources$[$id$jscomp$inline_1169$$]) {
            console.warn("tried to add looped sound source with existing ID '" + $id$jscomp$inline_1169$$ + "'");
          } else {
            var $layer$jscomp$inline_1171$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.layer, $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.sound;
            if ($JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$.startsWith("loop/") || $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$.startsWith("weather/") || $sfxLoops$$module$build$ts$ui$LoopedSoundSourceManager$$[$JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$]) {
              $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.volume, $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ = $JSCompiler_StaticMethods_playLocalizedEffect$$($$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$.$v$, 
              $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.x, $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.y, $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.radius, $layer$jscomp$inline_1171$$, $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.sound, 
              void 0 === $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$ ? 1.0 : $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$, !0);
            } else {
              $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$ = $$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$.$v$;
              var $x$jscomp$inline_1175$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.x, $y$jscomp$inline_1176$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.y, $radius$jscomp$inline_1177$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.radius, $musicname$jscomp$inline_1178$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.sound;
              $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ = $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$.volume;
              $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ = void 0 === $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ ? 1.0 : $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$;
              $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$.cache[$musicname$jscomp$inline_1178$$] || $JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$.load($musicname$jscomp$inline_1178$$, $stendhal$$.$paths$.$music$ + "/" + $musicname$jscomp$inline_1178$$ + ".ogg");
              $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ = $JSCompiler_StaticMethods_playLocalizedEffect$$($JSCompiler_StaticMethods_playLocalizedMusic$self$jscomp$inline_1174_filename$jscomp$inline_1173_volume$jscomp$inline_1187$$, $x$jscomp$inline_1175$$, $y$jscomp$inline_1176$$, $radius$jscomp$inline_1177$$, $layer$jscomp$inline_1171$$, $musicname$jscomp$inline_1178$$, void 0 === $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ ? 
              1.0 : $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$, !0);
            }
            $JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$ ? $$jscomp$key$ent$jscomp$inline_1142_JSCompiler_StaticMethods_addSource$self$jscomp$inline_1167$$.$JSC$2020_sources$[$id$jscomp$inline_1169$$] = {$layer$:$layer$jscomp$inline_1171$$, $sound$:$JSCompiler_temp$jscomp$inline_1172_ent$jscomp$inline_1143_snd$jscomp$inline_1170_volume$jscomp$inline_1179$$} : console.error("failed to add looped sound source with ID '" + $id$jscomp$inline_1169$$ + 
            "'");
          }
        } else {
          console.warn("tried to add looped sound source with ID '" + $id$jscomp$inline_1169$$ + "' before player was ready");
        }
      }
    }
  });
  return $$jscomp$super$this$jscomp$71$$;
};
$$jscomp$inherits$$($module$build$ts$entity$User$default$$.$User$, $module$build$ts$entity$Player$default$$.$Player$);
$module$build$ts$entity$User$default$$.$User$.$v$ = $module$build$ts$entity$Player$default$$.$Player$.$v$;
$JSCompiler_prototypeAlias$$ = $module$build$ts$entity$User$default$$.$User$.prototype;
$JSCompiler_prototypeAlias$$.$destroy$ = function($parent$jscomp$5$$) {
  this.$onExitZone$();
  $module$build$ts$entity$Player$default$$.$Player$.prototype.$destroy$.call(this, $parent$jscomp$5$$);
};
$JSCompiler_prototypeAlias$$.set = function($key$jscomp$104$$, $value$jscomp$138$$) {
  var $$jscomp$this$jscomp$48$$ = this, $oldX$jscomp$1$$ = this.x, $oldY$jscomp$1$$ = this.y;
  $module$build$ts$entity$Player$default$$.$Player$.prototype.set.call(this, $key$jscomp$104$$, $value$jscomp$138$$);
  "x" !== $key$jscomp$104$$ && "y" !== $key$jscomp$104$$ || !this.x || !this.y || this.x === $oldX$jscomp$1$$ && this.y === $oldY$jscomp$1$$ || $JSCompiler_StaticMethods_onDistanceChanged$$(this.$C$, this.x, this.y);
  queueMicrotask(function() {
    $module$build$ts$ui$UI$default$$.$ui$.get(110).update($key$jscomp$104$$);
    $module$build$ts$ui$UI$default$$.$ui$.get(101).update();
    $module$build$ts$ui$UI$default$$.$ui$.get(107).update();
    $module$build$ts$ui$UI$default$$.$ui$.get(112).update($$jscomp$this$jscomp$48$$);
  });
};
$JSCompiler_prototypeAlias$$.$unset$ = function($key$jscomp$105$$) {
  var $$jscomp$this$jscomp$49$$ = this;
  $module$build$ts$entity$Player$default$$.$Player$.prototype.$unset$.call(this, $key$jscomp$105$$);
  queueMicrotask(function() {
    $module$build$ts$ui$UI$default$$.$ui$.get(112).update($$jscomp$this$jscomp$49$$);
  });
};
$JSCompiler_prototypeAlias$$.$buildActions$ = function($list$jscomp$10$$) {
  $module$build$ts$entity$Player$default$$.$Player$.prototype.$buildActions$.call(this, $list$jscomp$10$$);
  var $charname$jscomp$7$$ = this._name;
  $list$jscomp$10$$.push({title:0.0 == this.$dx$ && 0.0 == this.$dy$ ? "Walk" : "Stop", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"walk"});
  }});
  $list$jscomp$10$$.push({title:"Set outfit", action:function() {
    var $outfitDialog$$ = $module$build$ts$ui$UI$default$$.$ui$.get($module$build$ts$ui$UIComponentEnum$default$$.$UIComponentEnum$.$OutfitDialog$);
    if (!$outfitDialog$$) {
      var $dstate$jscomp$4$$ = $JSCompiler_StaticMethods_getWindowState$$("outfit");
      $outfitDialog$$ = new $module$build$ts$ui$dialog$outfit$OutfitDialog$default$$.$OutfitDialog$();
      (new $module$build$ts$ui$toolkit$FloatingWindow$default$$.$FloatingWindow$("Choose outfit", $outfitDialog$$, $dstate$jscomp$4$$.x, $dstate$jscomp$4$$.y)).$windowId$ = "outfit";
    }
  }});
  $list$jscomp$10$$.push({title:"Where", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"where", target:$charname$jscomp$7$$,});
  }});
};
function $JSCompiler_StaticMethods_isInHearingRange$$($entity$jscomp$18$$, $rangeSquared$jscomp$3$$) {
  var $JSCompiler_StaticMethods_isInHearingRange$self$$ = $marauroa$$.$me$;
  if ($entity$jscomp$18$$ === $marauroa$$.$me$ || $JSCompiler_StaticMethods_isAdmin$$($JSCompiler_StaticMethods_isInHearingRange$self$$)) {
    return !0;
  }
  var $hearingRange$$ = 15;
  "undefined" !== typeof $rangeSquared$jscomp$3$$ && ($hearingRange$$ = 0 > $rangeSquared$jscomp$3$$ ? -1 : Math.sqrt($rangeSquared$jscomp$3$$));
  return 0 > $hearingRange$$ || Math.abs($JSCompiler_StaticMethods_isInHearingRange$self$$.x - $entity$jscomp$18$$.x) < $hearingRange$$ && Math.abs($JSCompiler_StaticMethods_isInHearingRange$self$$.y - $entity$jscomp$18$$.y) < $hearingRange$$;
}
$JSCompiler_prototypeAlias$$.$onExitZone$ = function() {
  $stendhal$$.$ui$.$gamewindow$.$onExitZone$();
  var $$jscomp$iter$89_msgs$$ = [], $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ = this.$C$, $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = !0;
  for ($$jscomp$inline_981_id$jscomp$inline_976_loopSources$$ in $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$.$JSC$2020_sources$) {
    if ($$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$) {
      $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$;
      var $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$ = $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.$JSC$2020_sources$[$$jscomp$inline_981_id$jscomp$inline_976_loopSources$$];
      if ("undefined" === typeof $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$) {
        console.warn("tried to remove unknown looped sound source with ID '" + $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$ + "'"), $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = !0;
      } else {
        delete $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.$JSC$2020_sources$[$$jscomp$inline_981_id$jscomp$inline_976_loopSources$$];
        var $errmsg$jscomp$inline_1148$$ = [];
        $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.$v$.stop($$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$.$layer$, $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$.$sound$) || $errmsg$jscomp$inline_1148$$.push("failed to stop looped sound source with ID '" + $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$ + 
        "' (" + $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$.$sound$.src + ")");
        $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.$JSC$2020_sources$[$$jscomp$inline_981_id$jscomp$inline_976_loopSources$$] && $errmsg$jscomp$inline_1148$$.push("failed to remove looped sound source with ID '" + $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$ + "' (" + $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$.$sound$.src + 
        ")");
        if (0 < $errmsg$jscomp$inline_1148$$.length) {
          $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = $$jscomp$makeIterator$$($errmsg$jscomp$inline_1148$$);
          for ($$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$ = $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.next(); !$$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$.done; $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$ = $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.next()) {
            console.error($$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$.value);
          }
          $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = !1;
        } else {
          $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = !0;
        }
      }
    }
  }
  $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ || ($$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$.$JSC$2020_sources$ = {});
  if (0 != Object.keys($$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$.$JSC$2020_sources$).length) {
    $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ = "LoopedSoundSourceManager reported not all sources stopped on zone change:";
    var $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$ = this.$C$.$JSC$2020_sources$;
    for (var $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$ in $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$) {
      $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ += "\n- ID: " + $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$ + " (" + $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$[$$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$].$sound$.src + ")";
    }
    $$jscomp$iter$89_msgs$$.push($$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$);
  }
  $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$ = this.$soundMan$;
  var $$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$ = void 0 === $$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$ ? !1 : $$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$;
  $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ = !0;
  $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$ = $$jscomp$makeIterator$$($$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$.$layers$);
  for ($$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$.next(); !$$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.done; $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = 
  $$jscomp$inline_981_id$jscomp$inline_976_loopSources$$.next()) {
    if ($$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ = $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$.value, "gui" !== $$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$ || 
    $$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$) {
      for ($$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$ = $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$.active[$$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$]; 0 < $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$.length;) {
        $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$.stop($$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$.$layers$.indexOf($$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$), $$jscomp$inline_1150_curLayer$jscomp$inline_984_source$jscomp$inline_1147$$[0]);
      }
      $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ = $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ && 0 == $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$.active[$$jscomp$inline_1149_$jscomp$key$layerName$jscomp$inline_982_JSCompiler_StaticMethods_removeSource$self$jscomp$inline_1145_JSCompiler_temp$jscomp$995_layerName$jscomp$inline_983_removed$jscomp$inline_975$$].length;
    }
  }
  if (!$$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$) {
    $$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$ = "SoundManager reported not all sounds stopped on zone change:";
    $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$ = $$jscomp$makeIterator$$($JSCompiler_StaticMethods_getActive$$(this.$soundMan$));
    for ($$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ = $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$.next(); !$$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$.done; $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ = 
    $$jscomp$iter$88_JSCompiler_StaticMethods_stopAll$self$jscomp$inline_978_id$jscomp$39$$.next()) {
      $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$ = $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$.value, $$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$ += "\n- " + $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$.src, $$jscomp$key$snd$jscomp$5_JSCompiler_StaticMethods_removeAll$self$jscomp$inline_974_snd$111_stopped$jscomp$inline_980_tmp$jscomp$11$$.loop && 
      ($$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$ += " (loop)");
    }
    $$jscomp$iter$89_msgs$$.push($$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$);
  }
  $$jscomp$iter$89_msgs$$ = $$jscomp$makeIterator$$($$jscomp$iter$89_msgs$$);
  for ($$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$ = $$jscomp$iter$89_msgs$$.next(); !$$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$.done; $$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$ = $$jscomp$iter$89_msgs$$.next()) {
    console.warn($$jscomp$key$msg$jscomp$1_includeGui$jscomp$inline_979_tmp$110$$.value);
  }
};
function $JSCompiler_StaticMethods_canInviteToGroup$$() {
  var $JSCompiler_StaticMethods_canInviteToGroup$self$$ = $marauroa$$.$me$, $gman$$ = $module$build$ts$SingletonRepo$default$$.$singletons$.$D$();
  return 0 == $gman$$.count || $gman$$.$leader$ === $JSCompiler_StaticMethods_canInviteToGroup$self$$.name;
}
;var $module$build$ts$entity$VisibleEntity$default$$ = {};
Object.defineProperty($module$build$ts$entity$VisibleEntity$default$$, "__esModule", {value:!0});
$module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$ = void 0;
$module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$ = function() {
  var $$jscomp$super$this$jscomp$72$$ = $module$build$ts$entity$Entity$default$$.$Entity$.call(this) || this;
  $$jscomp$super$this$jscomp$72$$.zIndex = 1;
  $$jscomp$super$this$jscomp$72$$.$sprite$ = {height:32, width:32};
  return $$jscomp$super$this$jscomp$72$$;
};
$$jscomp$inherits$$($module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$.prototype.set = function($key$jscomp$106$$, $value$jscomp$139$$) {
  $module$build$ts$entity$Entity$default$$.$Entity$.prototype.set.call(this, $key$jscomp$106$$, $value$jscomp$139$$);
  "class" === $key$jscomp$106$$ || "subclass" === $key$jscomp$106$$ || "_name" === $key$jscomp$106$$ ? this.$sprite$.filename = $stendhal$$.$paths$.$sprites$ + "/" + (this["class"] || "") + "/" + (this.subclass || "") + "/" + (this._name || "") + ".png" : "state" === $key$jscomp$106$$ && (this.$sprite$.offsetY = 32 * $value$jscomp$139$$);
};
$module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$.prototype.$isVisibleToAction$ = function() {
  return !0;
};
$module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/look.png) 1 3, auto";
};
var $module$build$ts$entity$WalkBlocker$default$$ = {};
Object.defineProperty($module$build$ts$entity$WalkBlocker$default$$, "__esModule", {value:!0});
$module$build$ts$entity$WalkBlocker$default$$.$WalkBlocker$ = void 0;
$module$build$ts$entity$WalkBlocker$default$$.$WalkBlocker$ = function() {
  var $$jscomp$super$this$jscomp$73$$ = $module$build$ts$entity$Entity$default$$.$Entity$.apply(this, arguments) || this;
  $$jscomp$super$this$jscomp$73$$.zIndex = 3000;
  return $$jscomp$super$this$jscomp$73$$;
};
$$jscomp$inherits$$($module$build$ts$entity$WalkBlocker$default$$.$WalkBlocker$, $module$build$ts$entity$Entity$default$$.$Entity$);
$module$build$ts$entity$WalkBlocker$default$$.$WalkBlocker$.prototype.$isVisibleToAction$ = function() {
  return !0;
};
$module$build$ts$entity$WalkBlocker$default$$.$WalkBlocker$.prototype.$getCursor$ = function() {
  return "url(" + $stendhal$$.$paths$.$sprites$ + "/cursor/stop.png) 1 3, auto";
};
Object.defineProperty({}, "__esModule", {value:!0});
$marauroa$$.$rpobjectFactory$.area = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$;
$marauroa$$.$rpobjectFactory$.baby_dragon = $module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$;
$marauroa$$.$rpobjectFactory$.blackboard = $module$build$ts$entity$Sign$default$$.$Sign$;
$marauroa$$.$rpobjectFactory$.blocktarget = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$;
$marauroa$$.$rpobjectFactory$.block = $module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$;
$marauroa$$.$rpobjectFactory$.blood = $module$build$ts$entity$Blood$default$$.$Blood$;
$marauroa$$.$rpobjectFactory$.cat = $module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$;
$marauroa$$.$rpobjectFactory$.chest = $module$build$ts$entity$Chest$default$$.$Chest$;
$marauroa$$.$rpobjectFactory$.corpse = $module$build$ts$entity$Corpse$default$$.$Corpse$;
$marauroa$$.$rpobjectFactory$.creature = $module$build$ts$entity$Creature$default$$.$Creature$;
$marauroa$$.$rpobjectFactory$._default = $module$build$ts$entity$UnknownEntity$default$$.$UnknownEntity$;
$marauroa$$.$rpobjectFactory$.domesticanimal = $module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$;
$marauroa$$.$rpobjectFactory$.door = $module$build$ts$entity$Door$default$$.$Door$;
$marauroa$$.$rpobjectFactory$.flyover = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$;
$marauroa$$.$rpobjectFactory$.food = $module$build$ts$entity$Food$default$$.$Food$;
$marauroa$$.$rpobjectFactory$.game_board = $module$build$ts$entity$GameBoard$default$$.$GameBoard$;
$marauroa$$.$rpobjectFactory$.gate = $module$build$ts$entity$Gate$default$$.$Gate$;
$marauroa$$.$rpobjectFactory$.growing_entity_spawner = $module$build$ts$entity$GrowingEntitySpawner$default$$.$GrowingEntitySpawner$;
$marauroa$$.$rpobjectFactory$.house_portal = $module$build$ts$entity$Portal$default$$.$Portal$;
$marauroa$$.$rpobjectFactory$.invisible_entity = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$;
$marauroa$$.$rpobjectFactory$.item = $module$build$ts$entity$Item$default$$.$Item$;
$marauroa$$.$rpobjectFactory$.looped_sound_source = $module$build$ts$entity$LoopedSoundSource$default$$.$LoopedSoundSource$;
$marauroa$$.$rpobjectFactory$.npc = $module$build$ts$entity$NPC$default$$.$NPC$;
$marauroa$$.$rpobjectFactory$.plant_grower = $module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$;
$marauroa$$.$rpobjectFactory$.player = $module$build$ts$entity$Player$default$$.$Player$;
$marauroa$$.$rpobjectFactory$.portal = $module$build$ts$entity$Portal$default$$.$Portal$;
$marauroa$$.$rpobjectFactory$.rented_sign = $module$build$ts$entity$Sign$default$$.$Sign$;
$marauroa$$.$rpobjectFactory$.sheep = $module$build$ts$entity$DomesticAnimal$default$$.$DomesticAnimal$;
$marauroa$$.$rpobjectFactory$.sign = $module$build$ts$entity$Sign$default$$.$Sign$;
$marauroa$$.$rpobjectFactory$.tiled_entity = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$;
$marauroa$$.$rpobjectFactory$.training_dummy = $module$build$ts$entity$TrainingDummy$default$$.$TrainingDummy$;
$marauroa$$.$rpobjectFactory$.unknown = $module$build$ts$entity$UnknownEntity$default$$.$UnknownEntity$;
$marauroa$$.$rpobjectFactory$.useable_entity = $module$build$ts$entity$UseableEntity$default$$.$UseableEntity$;
$marauroa$$.$rpobjectFactory$.user = $module$build$ts$entity$User$default$$.$User$;
$marauroa$$.$rpobjectFactory$.visible_entity = $module$build$ts$entity$VisibleEntity$default$$.$VisibleEntity$;
$marauroa$$.$rpobjectFactory$.walkblocker = $module$build$ts$entity$WalkBlocker$default$$.$WalkBlocker$;
$marauroa$$.$rpobjectFactory$.wall = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$;
$marauroa$$.$rpobjectFactory$.weather_entity = $module$build$ts$entity$InvisibleEntity$default$$.$InvisibleEntity$;
var $module$build$ts$sprite$EmptySprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$EmptySprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$EmptySprite$default$$.$EmptySprite$ = void 0;
$module$build$ts$sprite$EmptySprite$default$$.$EmptySprite$ = function($width$jscomp$37$$, $height$jscomp$36$$) {
  this.width = $width$jscomp$37$$;
  this.height = $height$jscomp$36$$;
};
$module$build$ts$sprite$EmptySprite$default$$.$EmptySprite$.prototype.$draw$ = function() {
};
$module$build$ts$sprite$EmptySprite$default$$.$EmptySprite$.prototype.$getHeight$ = function() {
  return this.height;
};
$module$build$ts$sprite$EmptySprite$default$$.$EmptySprite$.prototype.$getWidth$ = function() {
  return this.width;
};
var $module$build$ts$sprite$Sprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$Sprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$Sprite$default$$.$Sprite$ = void 0;
$module$build$ts$sprite$Sprite$default$$.$Sprite$ = function() {
};
var $module$build$ts$sprite$ImageSprite$default$$ = {};
Object.defineProperty($module$build$ts$sprite$ImageSprite$default$$, "__esModule", {value:!0});
$module$build$ts$sprite$ImageSprite$default$$.$ImageSprite$ = void 0;
$module$build$ts$sprite$ImageSprite$default$$.$ImageSprite$ = function($image$jscomp$20$$) {
  this.image = $image$jscomp$20$$ instanceof $module$build$ts$sprite$Sprite$default$$.$Sprite$ ? $module$build$ts$sprite$ImageSprite$default$$.$ImageSprite$.$v$($image$jscomp$20$$) : $image$jscomp$20$$;
};
$module$build$ts$sprite$ImageSprite$default$$.$ImageSprite$.$v$ = function($sprite$jscomp$8$$) {
  var $canvas$jscomp$16$$ = document.createElement("canvas"), $g$jscomp$22$$ = $canvas$jscomp$16$$.getContext("2d");
  $canvas$jscomp$16$$.width = $sprite$jscomp$8$$.$getWidth$();
  $canvas$jscomp$16$$.height = $sprite$jscomp$8$$.$getHeight$();
  $sprite$jscomp$8$$.$draw$($g$jscomp$22$$, 0, 0);
  return $canvas$jscomp$16$$;
};
$module$build$ts$sprite$ImageSprite$default$$.$ImageSprite$.prototype.$draw$ = function($g$jscomp$24$$, $destx$jscomp$1$$, $desty$jscomp$1$$, $x$jscomp$166$$, $y$jscomp$146$$, $w$jscomp$16$$, $h$jscomp$28$$) {
  7 === arguments.length ? $g$jscomp$24$$.drawImage(this.image, $destx$jscomp$1$$, $desty$jscomp$1$$, $destx$jscomp$1$$ + $w$jscomp$16$$, $desty$jscomp$1$$ + $h$jscomp$28$$, $x$jscomp$166$$, $y$jscomp$146$$, $x$jscomp$166$$ + $w$jscomp$16$$, $y$jscomp$146$$ + $h$jscomp$28$$) : $g$jscomp$24$$.drawImage(this.image, $destx$jscomp$1$$, $desty$jscomp$1$$);
};
$module$build$ts$sprite$ImageSprite$default$$.$ImageSprite$.prototype.$getHeight$ = function() {
  return this.image.height;
};
$module$build$ts$sprite$ImageSprite$default$$.$ImageSprite$.prototype.$getWidth$ = function() {
  return this.image.width;
};
Object.defineProperty({}, "__esModule", {value:!0});


//# sourceMappingURL=stendhal-compiled.js.map
    