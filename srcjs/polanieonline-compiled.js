/*

**************************************************************************
                   (C) Copyright 2003-2021 - Stendhal                    *
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
function $$jscomp$polyfill$$($property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$, $impl$jscomp$inline_31_polyfill$jscomp$1$$) {
  if ($impl$jscomp$inline_31_polyfill$jscomp$1$$) {
    a: {
      var $obj$jscomp$inline_25$$ = $$jscomp$global$$;
      $property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$ = $property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$.split(".");
      for (var $i$jscomp$inline_27_orig$jscomp$inline_30$$ = 0; $i$jscomp$inline_27_orig$jscomp$inline_30$$ < $property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$.length - 1; $i$jscomp$inline_27_orig$jscomp$inline_30$$++) {
        var $key$jscomp$inline_28$$ = $property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$[$i$jscomp$inline_27_orig$jscomp$inline_30$$];
        if (!($key$jscomp$inline_28$$ in $obj$jscomp$inline_25$$)) {
          break a;
        }
        $obj$jscomp$inline_25$$ = $obj$jscomp$inline_25$$[$key$jscomp$inline_28$$];
      }
      $property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$ = $property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$[$property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$.length - 1];
      $i$jscomp$inline_27_orig$jscomp$inline_30$$ = $obj$jscomp$inline_25$$[$property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$];
      $impl$jscomp$inline_31_polyfill$jscomp$1$$ = $impl$jscomp$inline_31_polyfill$jscomp$1$$($i$jscomp$inline_27_orig$jscomp$inline_30$$);
      $impl$jscomp$inline_31_polyfill$jscomp$1$$ != $i$jscomp$inline_27_orig$jscomp$inline_30$$ && null != $impl$jscomp$inline_31_polyfill$jscomp$1$$ && $$jscomp$defineProperty$$($obj$jscomp$inline_25$$, $property$jscomp$inline_29_split$jscomp$inline_26_target$jscomp$94$$, {configurable:!0, writable:!0, value:$impl$jscomp$inline_31_polyfill$jscomp$1$$});
    }
  }
}
var $JSCompiler_temp$jscomp$16$$;
if ("function" == typeof Object.setPrototypeOf) {
  $JSCompiler_temp$jscomp$16$$ = Object.setPrototypeOf;
} else {
  var $JSCompiler_inline_result$jscomp$17$$;
  a: {
    var $x$jscomp$inline_33$$ = {a:!0}, $y$jscomp$inline_34$$ = {};
    try {
      $y$jscomp$inline_34$$.__proto__ = $x$jscomp$inline_33$$;
      $JSCompiler_inline_result$jscomp$17$$ = $y$jscomp$inline_34$$.a;
      break a;
    } catch ($e$6$jscomp$inline_35$$) {
    }
    $JSCompiler_inline_result$jscomp$17$$ = !1;
  }
  $JSCompiler_temp$jscomp$16$$ = $JSCompiler_inline_result$jscomp$17$$ ? function($target$jscomp$99$$, $proto$jscomp$4$$) {
    $target$jscomp$99$$.__proto__ = $proto$jscomp$4$$;
    if ($target$jscomp$99$$.__proto__ !== $proto$jscomp$4$$) {
      throw new TypeError($target$jscomp$99$$ + " is not extensible");
    }
    return $target$jscomp$99$$;
  } : null;
}
var $$jscomp$setPrototypeOf$$ = $JSCompiler_temp$jscomp$16$$;
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
$$jscomp$polyfill$$("Promise", function($NativePromise$$) {
  function $PolyfillPromise$$($executor$$) {
    this.$A$ = 0;
    this.$B$ = void 0;
    this.$v$ = [];
    this.$G$ = !1;
    var $resolveAndReject$$ = this.$C$();
    try {
      $executor$$($resolveAndReject$$.resolve, $resolveAndReject$$.reject);
    } catch ($e$8$$) {
      $resolveAndReject$$.reject($e$8$$);
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
      for (var $i$jscomp$8$$ = 0; $i$jscomp$8$$ < $executingBatch$$.length; ++$i$jscomp$8$$) {
        var $f$jscomp$3$$ = $executingBatch$$[$i$jscomp$8$$];
        $executingBatch$$[$i$jscomp$8$$] = null;
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
      return function($x$jscomp$89$$) {
        $alreadyCalled$$ || ($alreadyCalled$$ = !0, $method$jscomp$1$$.call($thisPromise$$, $x$jscomp$89$$));
      };
    }
    var $thisPromise$$ = this, $alreadyCalled$$ = !1;
    return {resolve:$firstCallWins$$(this.$L$), reject:$firstCallWins$$(this.$D$)};
  };
  $PolyfillPromise$$.prototype.$L$ = function($value$jscomp$91$$) {
    if ($value$jscomp$91$$ === this) {
      this.$D$(new TypeError("A Promise cannot resolve to itself"));
    } else {
      if ($value$jscomp$91$$ instanceof $PolyfillPromise$$) {
        this.$N$($value$jscomp$91$$);
      } else {
        a: {
          switch(typeof $value$jscomp$91$$) {
            case "object":
              var $JSCompiler_inline_result$jscomp$20$$ = null != $value$jscomp$91$$;
              break a;
            case "function":
              $JSCompiler_inline_result$jscomp$20$$ = !0;
              break a;
            default:
              $JSCompiler_inline_result$jscomp$20$$ = !1;
          }
        }
        $JSCompiler_inline_result$jscomp$20$$ ? this.$K$($value$jscomp$91$$) : this.$F$($value$jscomp$91$$);
      }
    }
  };
  $PolyfillPromise$$.prototype.$K$ = function($obj$jscomp$28$$) {
    var $thenMethod$$ = void 0;
    try {
      $thenMethod$$ = $obj$jscomp$28$$.then;
    } catch ($error$jscomp$3$$) {
      this.$D$($error$jscomp$3$$);
      return;
    }
    "function" == typeof $thenMethod$$ ? this.$O$($thenMethod$$, $obj$jscomp$28$$) : this.$F$($obj$jscomp$28$$);
  };
  $PolyfillPromise$$.prototype.$D$ = function($reason$jscomp$6$$) {
    this.$H$(2, $reason$jscomp$6$$);
  };
  $PolyfillPromise$$.prototype.$F$ = function($value$jscomp$93$$) {
    this.$H$(1, $value$jscomp$93$$);
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
      for (var $i$jscomp$9$$ = 0; $i$jscomp$9$$ < this.$v$.length; ++$i$jscomp$9$$) {
        $asyncExecutor$$.$A$(this.$v$[$i$jscomp$9$$]);
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
      return "function" == typeof $paramF$$ ? function($x$jscomp$90$$) {
        try {
          $resolveChild$$($paramF$$($x$jscomp$90$$));
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
    function $callback$jscomp$46$$() {
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
    null == this.$v$ ? $asyncExecutor$$.$A$($callback$jscomp$46$$) : this.$v$.push($callback$jscomp$46$$);
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
      for (var $iterator$jscomp$7$$ = $$jscomp$makeIterator$$($thenablesOrValues$$), $iterRec$$ = $iterator$jscomp$7$$.next(); !$iterRec$$.done; $iterRec$$ = $iterator$jscomp$7$$.next()) {
        $resolvingPromise$$($iterRec$$.value).$callWhenSettled_$($resolve$jscomp$3$$, $reject$jscomp$3$$);
      }
    });
  };
  $PolyfillPromise$$.all = function($thenablesOrValues$jscomp$1$$) {
    var $iterator$jscomp$8$$ = $$jscomp$makeIterator$$($thenablesOrValues$jscomp$1$$), $iterRec$jscomp$1$$ = $iterator$jscomp$8$$.next();
    return $iterRec$jscomp$1$$.done ? $resolvingPromise$$([]) : new $PolyfillPromise$$(function($resolveAll$$, $rejectAll$$) {
      function $onFulfilled$jscomp$2$$($i$jscomp$10$$) {
        return function($ithResult$$) {
          $resultsArray$$[$i$jscomp$10$$] = $ithResult$$;
          $unresolvedCount$$--;
          0 == $unresolvedCount$$ && $resolveAll$$($resultsArray$$);
        };
      }
      var $resultsArray$$ = [], $unresolvedCount$$ = 0;
      do {
        $resultsArray$$.push(void 0), $unresolvedCount$$++, $resolvingPromise$$($iterRec$jscomp$1$$.value).$callWhenSettled_$($onFulfilled$jscomp$2$$($resultsArray$$.length - 1), $rejectAll$$), $iterRec$jscomp$1$$ = $iterator$jscomp$8$$.next();
      } while (!$iterRec$jscomp$1$$.done);
    });
  };
  return $PolyfillPromise$$;
});
$$jscomp$polyfill$$("Array.prototype.fill", function($orig$jscomp$4$$) {
  return $orig$jscomp$4$$ ? $orig$jscomp$4$$ : function($value$jscomp$94$$, $i$jscomp$11_opt_start$$, $opt_end$jscomp$9$$) {
    var $length$jscomp$16$$ = this.length || 0;
    0 > $i$jscomp$11_opt_start$$ && ($i$jscomp$11_opt_start$$ = Math.max(0, $length$jscomp$16$$ + $i$jscomp$11_opt_start$$));
    if (null == $opt_end$jscomp$9$$ || $opt_end$jscomp$9$$ > $length$jscomp$16$$) {
      $opt_end$jscomp$9$$ = $length$jscomp$16$$;
    }
    $opt_end$jscomp$9$$ = Number($opt_end$jscomp$9$$);
    0 > $opt_end$jscomp$9$$ && ($opt_end$jscomp$9$$ = Math.max(0, $length$jscomp$16$$ + $opt_end$jscomp$9$$));
    for ($i$jscomp$11_opt_start$$ = Number($i$jscomp$11_opt_start$$ || 0); $i$jscomp$11_opt_start$$ < $opt_end$jscomp$9$$; $i$jscomp$11_opt_start$$++) {
      this[$i$jscomp$11_opt_start$$] = $value$jscomp$94$$;
    }
    return this;
  };
});
function $$jscomp$typedArrayFill$$($orig$jscomp$5$$) {
  return $orig$jscomp$5$$ ? $orig$jscomp$5$$ : Array.prototype.fill;
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
$$jscomp$polyfill$$("Object.is", function($orig$jscomp$6$$) {
  return $orig$jscomp$6$$ ? $orig$jscomp$6$$ : function($left$jscomp$2$$, $right$jscomp$2$$) {
    return $left$jscomp$2$$ === $right$jscomp$2$$ ? 0 !== $left$jscomp$2$$ || 1 / $left$jscomp$2$$ === 1 / $right$jscomp$2$$ : $left$jscomp$2$$ !== $left$jscomp$2$$ && $right$jscomp$2$$ !== $right$jscomp$2$$;
  };
});
$$jscomp$polyfill$$("Array.prototype.includes", function($orig$jscomp$7$$) {
  return $orig$jscomp$7$$ ? $orig$jscomp$7$$ : function($searchElement$jscomp$4$$, $i$jscomp$12_opt_fromIndex$jscomp$8$$) {
    var $array$jscomp$8$$ = this;
    $array$jscomp$8$$ instanceof String && ($array$jscomp$8$$ = String($array$jscomp$8$$));
    var $len$$ = $array$jscomp$8$$.length;
    $i$jscomp$12_opt_fromIndex$jscomp$8$$ = $i$jscomp$12_opt_fromIndex$jscomp$8$$ || 0;
    for (0 > $i$jscomp$12_opt_fromIndex$jscomp$8$$ && ($i$jscomp$12_opt_fromIndex$jscomp$8$$ = Math.max($i$jscomp$12_opt_fromIndex$jscomp$8$$ + $len$$, 0)); $i$jscomp$12_opt_fromIndex$jscomp$8$$ < $len$$; $i$jscomp$12_opt_fromIndex$jscomp$8$$++) {
      var $element$jscomp$8$$ = $array$jscomp$8$$[$i$jscomp$12_opt_fromIndex$jscomp$8$$];
      if ($element$jscomp$8$$ === $searchElement$jscomp$4$$ || Object.is($element$jscomp$8$$, $searchElement$jscomp$4$$)) {
        return !0;
      }
    }
    return !1;
  };
});
$$jscomp$polyfill$$("String.prototype.includes", function($orig$jscomp$8$$) {
  return $orig$jscomp$8$$ ? $orig$jscomp$8$$ : function($searchString$jscomp$3$$, $opt_position$jscomp$3$$) {
    if (null == this) {
      throw new TypeError("The 'this' value for String.prototype.includes must not be null or undefined");
    }
    if ($searchString$jscomp$3$$ instanceof RegExp) {
      throw new TypeError("First argument to String.prototype.includes must not be a regular expression");
    }
    return -1 !== this.indexOf($searchString$jscomp$3$$, $opt_position$jscomp$3$$ || 0);
  };
});
$$jscomp$polyfill$$("Symbol", function($orig$jscomp$9$$) {
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
  if ($orig$jscomp$9$$) {
    return $orig$jscomp$9$$;
  }
  $SymbolClass$$.prototype.toString = function() {
    return this.$v$;
  };
  var $SYMBOL_PREFIX$$ = "jscomp_symbol_" + (1e9 * Math.random() >>> 0) + "_", $counter$$ = 0;
  return $symbolPolyfill$$;
});
$$jscomp$polyfill$$("Symbol.iterator", function($orig$jscomp$10_symbolIterator$$) {
  if ($orig$jscomp$10_symbolIterator$$) {
    return $orig$jscomp$10_symbolIterator$$;
  }
  $orig$jscomp$10_symbolIterator$$ = Symbol("Symbol.iterator");
  for (var $arrayLikes$$ = "Array Int8Array Uint8Array Uint8ClampedArray Int16Array Uint16Array Int32Array Uint32Array Float32Array Float64Array".split(" "), $i$jscomp$13$$ = 0; $i$jscomp$13$$ < $arrayLikes$$.length; $i$jscomp$13$$++) {
    var $ArrayLikeCtor$$ = $$jscomp$global$$[$arrayLikes$$[$i$jscomp$13$$]];
    "function" === typeof $ArrayLikeCtor$$ && "function" != typeof $ArrayLikeCtor$$.prototype[$orig$jscomp$10_symbolIterator$$] && $$jscomp$defineProperty$$($ArrayLikeCtor$$.prototype, $orig$jscomp$10_symbolIterator$$, {configurable:!0, writable:!0, value:function() {
      return $$jscomp$iteratorPrototype$$($$jscomp$arrayIteratorImpl$$(this));
    }});
  }
  return $orig$jscomp$10_symbolIterator$$;
});
function $$jscomp$iteratorPrototype$$($iterator$jscomp$9_next$$) {
  $iterator$jscomp$9_next$$ = {next:$iterator$jscomp$9_next$$};
  $iterator$jscomp$9_next$$[Symbol.iterator] = function() {
    return this;
  };
  return $iterator$jscomp$9_next$$;
}
function $$jscomp$iteratorFromArray$$($array$jscomp$9$$, $transform$jscomp$1$$) {
  $array$jscomp$9$$ instanceof String && ($array$jscomp$9$$ += "");
  var $i$jscomp$14$$ = 0, $done$$ = !1, $iter$jscomp$1$$ = {next:function() {
    if (!$done$$ && $i$jscomp$14$$ < $array$jscomp$9$$.length) {
      var $index$jscomp$74$$ = $i$jscomp$14$$++;
      return {value:$transform$jscomp$1$$($index$jscomp$74$$, $array$jscomp$9$$[$index$jscomp$74$$]), done:!1};
    }
    $done$$ = !0;
    return {done:!0, value:void 0};
  }};
  $iter$jscomp$1$$[Symbol.iterator] = function() {
    return $iter$jscomp$1$$;
  };
  return $iter$jscomp$1$$;
}
$$jscomp$polyfill$$("Array.prototype.keys", function($orig$jscomp$12$$) {
  return $orig$jscomp$12$$ ? $orig$jscomp$12$$ : function() {
    return $$jscomp$iteratorFromArray$$(this, function($i$jscomp$15$$) {
      return $i$jscomp$15$$;
    });
  };
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
$marauroa$$.$util$ = {$isEmpty$:function($obj$jscomp$29$$) {
  for (var $i$jscomp$16$$ in $obj$jscomp$29$$) {
    if ($obj$jscomp$29$$.hasOwnProperty($i$jscomp$16$$)) {
      return !1;
    }
  }
  return !0;
}, $isEmptyExceptId$:function($obj$jscomp$30$$) {
  for (var $i$jscomp$17$$ in $obj$jscomp$30$$) {
    if ("id" !== $i$jscomp$17$$ && $obj$jscomp$30$$.hasOwnProperty($i$jscomp$17$$)) {
      return !1;
    }
  }
  return !0;
}, first:function($obj$jscomp$31$$) {
  for (var $i$jscomp$18$$ in $obj$jscomp$31$$) {
    if ($obj$jscomp$31$$.hasOwnProperty($i$jscomp$18$$)) {
      return $obj$jscomp$31$$[$i$jscomp$18$$];
    }
  }
}, $fromProto$:function($proto$jscomp$6$$, $def$$) {
  function $F$$() {
    this.$proto$ = $proto$jscomp$6$$;
  }
  $F$$.prototype = $proto$jscomp$6$$;
  var $obj$jscomp$32$$ = new $F$$();
  return $def$$ ? $marauroa$$.$util$.$merge$($obj$jscomp$32$$, $def$$) : $obj$jscomp$32$$;
}, $merge$:function($a$jscomp$1$$, $b$jscomp$1$$) {
  for (var $key$jscomp$39$$ in $b$jscomp$1$$) {
    $a$jscomp$1$$[$key$jscomp$39$$] = $b$jscomp$1$$[$key$jscomp$39$$];
  }
  return $a$jscomp$1$$;
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$clientFramework$ = {$clientid$:"-1", connect:function($host_socket$$, $port$$) {
  var $protocol$jscomp$1$$ = "ws";
  "https:" === window.location.protocol && ($protocol$jscomp$1$$ = "wss");
  null === $host_socket$$ && ($host_socket$$ = window.location.hostname);
  null === $port$$ && ($port$$ = window.location.port);
  "" != $port$$ && ($port$$ = ":" + $port$$);
  $host_socket$$ = new WebSocket($protocol$jscomp$1$$ + "://" + $host_socket$$ + $port$$ + "/ws/");
  $host_socket$$.onmessage = $marauroa$$.$clientFramework$.$onMessage$;
  $host_socket$$.onopen = function() {
    setInterval(function() {
      $marauroa$$.$clientFramework$.$sendMessage$({t:"8",});
    }, 10000);
    $marauroa$$.$clientFramework$.$onConnect$();
  };
  $host_socket$$.onclose = $marauroa$$.$clientFramework$.$onDisonnect$;
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
}, $onMessage$:function($e$jscomp$8_msg$jscomp$2$$) {
  $e$jscomp$8_msg$jscomp$2$$ = JSON.parse($e$jscomp$8_msg$jscomp$2$$.data);
  if ("9" === $e$jscomp$8_msg$jscomp$2$$.t || "15" === $e$jscomp$8_msg$jscomp$2$$.t) {
    $marauroa$$.$clientFramework$.$clientid$ = $e$jscomp$8_msg$jscomp$2$$.c;
  }
  "string" === typeof $e$jscomp$8_msg$jscomp$2$$ ? console.error("JSON error on message: " + $e$jscomp$8_msg$jscomp$2$$) : ($marauroa$$.$messageFactory$.$addDispatchMethod$($e$jscomp$8_msg$jscomp$2$$), $e$jscomp$8_msg$jscomp$2$$.$dispatch$());
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
    for (var $key$jscomp$40$$ in $characters$$) {
      $characters$$.hasOwnProperty($key$jscomp$40$$) && $marauroa$$.$clientFramework$.$chooseCharacter$($key$jscomp$40$$);
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
    var $contents$jscomp$3$$ = {}, $i$jscomp$19$$;
    for ($i$jscomp$19$$ in this.contents) {
      $contents$jscomp$3$$[this.contents[$i$jscomp$19$$].name] = "undefined" != typeof this.contents[$i$jscomp$19$$].ack && this.contents[$i$jscomp$19$$].ack ? !0 : !1;
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
  for (var $i$jscomp$20$$ in $marauroa$$.$currentZone$) {
    $marauroa$$.$currentZone$.hasOwnProperty($i$jscomp$20$$) && "function" !== typeof $marauroa$$.$currentZone$[$i$jscomp$20$$] && ($marauroa$$.$currentZone$[$i$jscomp$20$$].$destroy$($marauroa$$.$currentZone$), delete $marauroa$$.$currentZone$[$i$jscomp$20$$]);
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
    for (var $i$jscomp$21$$ in $msg$jscomp$13$$.aO) {
      if ($msg$jscomp$13$$.aO.hasOwnProperty($i$jscomp$21$$)) {
        var $o$$ = $marauroa$$.$rpobjectFactory$.create($msg$jscomp$13$$.aO[$i$jscomp$21$$].c);
        $marauroa$$.$perceptionHandler$.$addChanges$($o$$, $msg$jscomp$13$$.aO[$i$jscomp$21$$]);
        $marauroa$$.$currentZone$[$msg$jscomp$13$$.aO[$i$jscomp$21$$].a.id] = $o$$;
      }
    }
  }
}, $applyPerceptionDeletedRPObjects$:function($msg$jscomp$14$$) {
  if ($msg$jscomp$14$$.dO) {
    for (var $i$jscomp$22$$ in $msg$jscomp$14$$.dO) {
      if ($msg$jscomp$14$$.dO.hasOwnProperty($i$jscomp$22$$)) {
        var $tmp$$ = $msg$jscomp$14$$.dO[$i$jscomp$22$$].a.id;
        $marauroa$$.$currentZone$[$tmp$$].$destroy$($marauroa$$.$currentZone$);
        delete $marauroa$$.$currentZone$[$tmp$$];
      }
    }
  }
}, $applyPerceptionModifiedRPObjects$:function($msg$jscomp$15$$) {
  if ($msg$jscomp$15$$.dA) {
    for (var $i$jscomp$23$$ in $msg$jscomp$15$$.dA) {
      if ($msg$jscomp$15$$.dA.hasOwnProperty($i$jscomp$23$$) && "undefined" !== typeof $marauroa$$.$currentZone$[$msg$jscomp$15$$.dA[$i$jscomp$23$$].a.id]) {
        var $o$jscomp$1$$ = $marauroa$$.$currentZone$[$msg$jscomp$15$$.dA[$i$jscomp$23$$].a.id];
        $marauroa$$.$perceptionHandler$.$deleteChanges$($o$jscomp$1$$, $msg$jscomp$15$$.dA[$i$jscomp$23$$]);
      }
    }
  }
  if ($msg$jscomp$15$$.aA) {
    for ($i$jscomp$23$$ in $msg$jscomp$15$$.aA) {
      $msg$jscomp$15$$.aA.hasOwnProperty($i$jscomp$23$$) && "undefined" !== typeof $marauroa$$.$currentZone$[$msg$jscomp$15$$.aA[$i$jscomp$23$$].a.id] && ($o$jscomp$1$$ = $marauroa$$.$currentZone$[$msg$jscomp$15$$.aA[$i$jscomp$23$$].a.id], $marauroa$$.$perceptionHandler$.$addChanges$($o$jscomp$1$$, $msg$jscomp$15$$.aA[$i$jscomp$23$$]));
    }
  }
}, $applyPerceptionMyRPObject$:function($msg$jscomp$16$$) {
  var $id$jscomp$6_o$jscomp$2$$;
  "undefined" !== typeof $msg$jscomp$16$$.aM && ($id$jscomp$6_o$jscomp$2$$ = $msg$jscomp$16$$.aM.a.id);
  "undefined" !== typeof $msg$jscomp$16$$.dM && ($id$jscomp$6_o$jscomp$2$$ = $msg$jscomp$16$$.dM.a.id);
  "undefined" !== typeof $id$jscomp$6_o$jscomp$2$$ && ($marauroa$$.$perceptionHandler$.$addMyRPObjectToWorldIfPrivate$($id$jscomp$6_o$jscomp$2$$, $msg$jscomp$16$$.aM), $id$jscomp$6_o$jscomp$2$$ = $marauroa$$.$currentZone$[$id$jscomp$6_o$jscomp$2$$], $marauroa$$.$me$ = $id$jscomp$6_o$jscomp$2$$, $marauroa$$.$perceptionHandler$.$deleteChanges$($id$jscomp$6_o$jscomp$2$$, $msg$jscomp$16$$.dM), $marauroa$$.$perceptionHandler$.$addChanges$($id$jscomp$6_o$jscomp$2$$, $msg$jscomp$16$$.aM));
}, $addMyRPObjectToWorldIfPrivate$:function($id$jscomp$7$$, $added$jscomp$1$$) {
  if ("undefined" === typeof $marauroa$$.$currentZone$[$id$jscomp$7$$]) {
    if ("undefined" === typeof $added$jscomp$1$$) {
      $marauroa$$.$currentZone$[$id$jscomp$7$$] = {};
    } else {
      var $o$jscomp$3$$ = $marauroa$$.$rpobjectFactory$.create($added$jscomp$1$$.c);
      $marauroa$$.$currentZone$[$id$jscomp$7$$] = $o$jscomp$3$$;
      $marauroa$$.$perceptionHandler$.$addChanges$($o$jscomp$3$$, $added$jscomp$1$$);
    }
  }
}, $deleteChanges$:function($object$jscomp$4$$, $diff$$) {
  if ("undefined" !== typeof $diff$$) {
    if ("undefined" !== typeof $diff$$.a) {
      for (var $i$jscomp$24$$ in $diff$$.a) {
        $diff$$.a.hasOwnProperty($i$jscomp$24$$) && "id" !== $i$jscomp$24$$ && "zoneid" !== $i$jscomp$24$$ && $object$jscomp$4$$.$unset$($i$jscomp$24$$);
      }
    }
    if ("undefined" !== typeof $diff$$.s) {
      for ($i$jscomp$24$$ in $diff$$.s) {
        if ($diff$$.s.hasOwnProperty($i$jscomp$24$$)) {
          if ($marauroa$$.$util$.$isEmpty$($diff$$.s[$i$jscomp$24$$])) {
            $object$jscomp$4$$.$del$($diff$$.s[$i$jscomp$24$$]);
          } else {
            for (var $j$$ in $diff$$.s[$i$jscomp$24$$]) {
              $diff$$.s[$i$jscomp$24$$].hasOwnProperty($j$$) && $object$jscomp$4$$[$i$jscomp$24$$].$del$($diff$$.s[$i$jscomp$24$$][$j$$].a.id);
            }
          }
        }
      }
    }
    if ("undefined" !== typeof $diff$$.m) {
      for ($i$jscomp$24$$ in $diff$$.m) {
        if ($diff$$.m.hasOwnProperty($i$jscomp$24$$)) {
          if ($marauroa$$.$util$.$isEmpty$($diff$$.m[$i$jscomp$24$$].a)) {
            $object$jscomp$4$$.$unset$($diff$$.m[$i$jscomp$24$$]);
          } else {
            for ($j$$ in $diff$$.m[$i$jscomp$24$$].a) {
              $diff$$.m[$i$jscomp$24$$].a.hasOwnProperty($j$$) && $object$jscomp$4$$.$unsetMapEntry$($i$jscomp$24$$, $j$$);
            }
          }
        }
      }
    }
  }
}, $addChanges$:function($object$jscomp$5$$, $diff$jscomp$1$$) {
  if ("undefined" !== typeof $diff$jscomp$1$$) {
    $object$jscomp$5$$._rpclass = $diff$jscomp$1$$.c;
    for (var $i$jscomp$25$$ in $diff$jscomp$1$$.a) {
      $diff$jscomp$1$$.a.hasOwnProperty($i$jscomp$25$$) && ("undefined" === typeof $object$jscomp$5$$.set ? (console.warn("Object missing set(key, value)-function", $object$jscomp$5$$, $diff$jscomp$1$$.a), $object$jscomp$5$$[$i$jscomp$25$$] = $diff$jscomp$1$$.a[$i$jscomp$25$$]) : $object$jscomp$5$$.set($i$jscomp$25$$, $diff$jscomp$1$$.a[$i$jscomp$25$$]));
    }
    if ("undefined" !== typeof $diff$jscomp$1$$.m) {
      for ($i$jscomp$25$$ in $diff$jscomp$1$$.m) {
        if ($diff$jscomp$1$$.m.hasOwnProperty($i$jscomp$25$$)) {
          "undefined" === typeof $object$jscomp$5$$[$i$jscomp$25$$] && ($object$jscomp$5$$[$i$jscomp$25$$] = {});
          for (var $j$jscomp$1$$ in $diff$jscomp$1$$.m[$i$jscomp$25$$].a) {
            "zoneid" !== $j$jscomp$1$$ && "id" !== $j$jscomp$1$$ && $diff$jscomp$1$$.m[$i$jscomp$25$$].a.hasOwnProperty($j$jscomp$1$$) && $object$jscomp$5$$.$setMapEntry$($i$jscomp$25$$, $j$jscomp$1$$, $diff$jscomp$1$$.m[$i$jscomp$25$$].a[$j$jscomp$1$$]);
          }
        }
      }
    }
    if ("undefined" !== typeof $diff$jscomp$1$$.s) {
      for ($i$jscomp$25$$ in $diff$jscomp$1$$.s) {
        if ($diff$jscomp$1$$.s.hasOwnProperty($i$jscomp$25$$)) {
          for ($j$jscomp$1$$ in "undefined" === typeof $object$jscomp$5$$[$i$jscomp$25$$] && ($object$jscomp$5$$[$i$jscomp$25$$] = $object$jscomp$5$$.$createSlot$($i$jscomp$25$$)), $diff$jscomp$1$$.s[$i$jscomp$25$$]) {
            if ($diff$jscomp$1$$.s[$i$jscomp$25$$].hasOwnProperty($j$jscomp$1$$)) {
              var $id$jscomp$8$$ = $diff$jscomp$1$$.s[$i$jscomp$25$$][$j$jscomp$1$$].a.id;
              if ("undefined" === typeof $object$jscomp$5$$[$i$jscomp$25$$].get($id$jscomp$8$$)) {
                var $newObject$$ = $marauroa$$.$rpobjectFactory$.create($diff$jscomp$1$$.s[$i$jscomp$25$$][$j$jscomp$1$$].c);
                $newObject$$.$_parent$ = $object$jscomp$5$$[$i$jscomp$25$$];
                $newObject$$.id = $id$jscomp$8$$;
                $object$jscomp$5$$[$i$jscomp$25$$].add($newObject$$);
              }
              $marauroa$$.$perceptionHandler$.$addChanges$($object$jscomp$5$$[$i$jscomp$25$$].get($id$jscomp$8$$), $diff$jscomp$1$$.s[$i$jscomp$25$$][$j$jscomp$1$$]);
            }
          }
        }
      }
    }
    if ("undefined" !== typeof $diff$jscomp$1$$.e && "undefined" !== typeof $object$jscomp$5$$.$onEvent$) {
      for ($i$jscomp$25$$ in $diff$jscomp$1$$.e) {
        $diff$jscomp$1$$.e.hasOwnProperty($i$jscomp$25$$) && $object$jscomp$5$$.$onEvent$($diff$jscomp$1$$.e[$i$jscomp$25$$]);
      }
    }
  }
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$ = new function() {
  this._default = function() {
  };
  this._default.$onEvent$ = function($e$jscomp$9$$) {
    var $event$jscomp$5$$ = $marauroa$$.$rpeventFactory$.create($e$jscomp$9$$.c), $i$jscomp$26$$;
    for ($i$jscomp$26$$ in $e$jscomp$9$$.a) {
      $e$jscomp$9$$.a.hasOwnProperty($i$jscomp$26$$) && ($event$jscomp$5$$[$i$jscomp$26$$] = $e$jscomp$9$$.a[$i$jscomp$26$$]), $event$jscomp$5$$._rpclass = $e$jscomp$9$$.c;
    }
    for (var $slot$$ in $e$jscomp$9$$.s) {
      $e$jscomp$9$$.s.hasOwnProperty($slot$$) && ($event$jscomp$5$$[$slot$$] = $e$jscomp$9$$.s[$slot$$]);
    }
    $event$jscomp$5$$.$execute$(this);
  };
  this._default.set = function($key$jscomp$41$$, $value$jscomp$95$$) {
    this[$key$jscomp$41$$] = $value$jscomp$95$$;
  };
  this._default.$setMapEntry$ = function($map$$, $key$jscomp$42$$, $value$jscomp$96$$) {
    this[$map$$][$key$jscomp$42$$] = $value$jscomp$96$$;
  };
  this._default.$unset$ = function($key$jscomp$43$$) {
    delete this[$key$jscomp$43$$];
  };
  this._default.$unsetMapEntry$ = function($map$jscomp$1$$, $key$jscomp$44$$) {
    delete this[$map$jscomp$1$$][$key$jscomp$44$$];
  };
  this._default.$destroy$ = function() {
  };
  this._default.$createSlot$ = function($name$jscomp$73_slot$jscomp$1$$) {
    $name$jscomp$73_slot$jscomp$1$$ = $marauroa$$.$rpslotFactory$.create($name$jscomp$73_slot$jscomp$1$$);
    $name$jscomp$73_slot$jscomp$1$$.$_parent$ = this;
    return $name$jscomp$73_slot$jscomp$1$$;
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
  this._default = function() {
  };
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
  this._default = function() {
    this.$_objects$ = [];
  };
  this._default.add = function($value$jscomp$97$$) {
    $value$jscomp$97$$ && $value$jscomp$97$$.id && this.$_objects$.push($value$jscomp$97$$);
  };
  this._default.get = function($idx_key$jscomp$45$$) {
    $idx_key$jscomp$45$$ = this.$getIndex$($idx_key$jscomp$45$$);
    if (-1 < $idx_key$jscomp$45$$) {
      return this.$_objects$[$idx_key$jscomp$45$$];
    }
  };
  this._default.$getByIndex$ = function($idx$jscomp$1$$) {
    return this.$_objects$[$idx$jscomp$1$$];
  };
  this._default.count = function() {
    return this.$_objects$.length;
  };
  this._default.$getIndex$ = function($key$jscomp$46$$) {
    var $i$jscomp$27$$, $c$$ = this.$_objects$.length;
    for ($i$jscomp$27$$ = 0; $i$jscomp$27$$ < $c$$; $i$jscomp$27$$++) {
      if (this.$_objects$[$i$jscomp$27$$].id === $key$jscomp$46$$) {
        return $i$jscomp$27$$;
      }
    }
    return -1;
  };
  this._default.$del$ = function($idx$jscomp$2_key$jscomp$47$$) {
    $idx$jscomp$2_key$jscomp$47$$ = this.$getIndex$($idx$jscomp$2_key$jscomp$47$$);
    -1 < $idx$jscomp$2_key$jscomp$47$$ && this.$_objects$.splice($idx$jscomp$2_key$jscomp$47$$, 1);
  };
  this._default.first = function() {
    if (0 < this.$_objects$.length) {
      return this.$_objects$[0];
    }
  };
  this.create = function($name$jscomp$74$$) {
    var $ctor$jscomp$3_slot$jscomp$2$$ = this._default;
    "undefined" != typeof this[$name$jscomp$74$$] && ($ctor$jscomp$3_slot$jscomp$2$$ = this[$name$jscomp$74$$]);
    $ctor$jscomp$3_slot$jscomp$2$$ = $marauroa$$.$util$.$fromProto$($ctor$jscomp$3_slot$jscomp$2$$);
    $ctor$jscomp$3_slot$jscomp$2$$.$_name$ = $name$jscomp$74$$;
    $ctor$jscomp$3_slot$jscomp$2$$.$_objects$ = [];
    return $ctor$jscomp$3_slot$jscomp$2$$;
  };
}();
/*
 zlib.js 2012 - imaya [ https://github.com/imaya/zlib.js ]
 The MIT License
*/
(function() {
  function $r$jscomp$1$$($a$jscomp$2_c$jscomp$1$$, $d$$) {
    $a$jscomp$2_c$jscomp$1$$ = $a$jscomp$2_c$jscomp$1$$.split(".");
    var $b$jscomp$2$$ = $aa$$;
    $a$jscomp$2_c$jscomp$1$$[0] in $b$jscomp$2$$ || !$b$jscomp$2$$.execScript || $b$jscomp$2$$.execScript("var " + $a$jscomp$2_c$jscomp$1$$[0]);
    for (var $e$jscomp$10$$; $a$jscomp$2_c$jscomp$1$$.length && ($e$jscomp$10$$ = $a$jscomp$2_c$jscomp$1$$.shift());) {
      $a$jscomp$2_c$jscomp$1$$.length || void 0 === $d$$ ? $b$jscomp$2$$ = $b$jscomp$2$$[$e$jscomp$10$$] ? $b$jscomp$2$$[$e$jscomp$10$$] : $b$jscomp$2$$[$e$jscomp$10$$] = {} : $b$jscomp$2$$[$e$jscomp$10$$] = $d$$;
    }
  }
  function $u$$($c$jscomp$2$$) {
    var $d$jscomp$1$$ = $c$jscomp$2$$.length, $a$jscomp$3$$ = 0, $b$jscomp$3$$ = Number.POSITIVE_INFINITY, $l$$, $p$jscomp$1$$, $s$jscomp$5$$;
    for ($p$jscomp$1$$ = 0; $p$jscomp$1$$ < $d$jscomp$1$$; ++$p$jscomp$1$$) {
      $c$jscomp$2$$[$p$jscomp$1$$] > $a$jscomp$3$$ && ($a$jscomp$3$$ = $c$jscomp$2$$[$p$jscomp$1$$]), $c$jscomp$2$$[$p$jscomp$1$$] < $b$jscomp$3$$ && ($b$jscomp$3$$ = $c$jscomp$2$$[$p$jscomp$1$$]);
    }
    var $e$jscomp$11$$ = 1 << $a$jscomp$3$$;
    var $f$jscomp$4$$ = new ($t$$ ? Uint32Array : Array)($e$jscomp$11$$);
    var $g$$ = 1;
    var $h$jscomp$7$$ = 0;
    for ($l$$ = 2; $g$$ <= $a$jscomp$3$$;) {
      for ($p$jscomp$1$$ = 0; $p$jscomp$1$$ < $d$jscomp$1$$; ++$p$jscomp$1$$) {
        if ($c$jscomp$2$$[$p$jscomp$1$$] === $g$$) {
          var $n$jscomp$2$$ = 0;
          var $m_x$jscomp$91$$ = $h$jscomp$7$$;
          for ($s$jscomp$5$$ = 0; $s$jscomp$5$$ < $g$$; ++$s$jscomp$5$$) {
            $n$jscomp$2$$ = $n$jscomp$2$$ << 1 | $m_x$jscomp$91$$ & 1, $m_x$jscomp$91$$ >>= 1;
          }
          $m_x$jscomp$91$$ = $g$$ << 16 | $p$jscomp$1$$;
          for ($s$jscomp$5$$ = $n$jscomp$2$$; $s$jscomp$5$$ < $e$jscomp$11$$; $s$jscomp$5$$ += $l$$) {
            $f$jscomp$4$$[$s$jscomp$5$$] = $m_x$jscomp$91$$;
          }
          ++$h$jscomp$7$$;
        }
      }
      ++$g$$;
      $h$jscomp$7$$ <<= 1;
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
    this.$i$ = $y$jscomp$74$$;
    this.p = !1;
    if ($d$jscomp$2$$ || !($d$jscomp$2$$ = {})) {
      $d$jscomp$2$$.index && (this.d = $d$jscomp$2$$.index), $d$jscomp$2$$.bufferSize && (this.$h$ = $d$jscomp$2$$.bufferSize), $d$jscomp$2$$.$bufferType$ && (this.$i$ = $d$jscomp$2$$.$bufferType$), $d$jscomp$2$$.resize && (this.p = $d$jscomp$2$$.resize);
    }
    switch(this.$i$) {
      case $A$$:
        this.a = 32768;
        this.b = new ($t$$ ? Uint8Array : Array)(32768 + this.$h$ + 258);
        break;
      case $y$jscomp$74$$:
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
    for (var $a$jscomp$5$$ = $c$jscomp$5$$.f, $b$jscomp$5$$ = $c$jscomp$5$$.c, $e$jscomp$13$$ = $c$jscomp$5$$.input, $f$jscomp$6$$ = $c$jscomp$5$$.d, $g$jscomp$2$$ = $e$jscomp$13$$.length; $b$jscomp$5$$ < $d$jscomp$4$$;) {
      if ($f$jscomp$6$$ >= $g$jscomp$2$$) {
        throw Error("input buffer is broken");
      }
      $a$jscomp$5$$ |= $e$jscomp$13$$[$f$jscomp$6$$++] << $b$jscomp$5$$;
      $b$jscomp$5$$ += 8;
    }
    $c$jscomp$5$$.f = $a$jscomp$5$$ >>> $d$jscomp$4$$;
    $c$jscomp$5$$.c = $b$jscomp$5$$ - $d$jscomp$4$$;
    $c$jscomp$5$$.d = $f$jscomp$6$$;
    return $a$jscomp$5$$ & (1 << $d$jscomp$4$$) - 1;
  }
  function $D$$($c$jscomp$6$$, $d$jscomp$5_l$jscomp$2$$) {
    var $a$jscomp$6$$ = $c$jscomp$6$$.f, $b$jscomp$6$$ = $c$jscomp$6$$.c, $e$jscomp$14_n$jscomp$4$$ = $c$jscomp$6$$.input, $f$jscomp$7$$ = $c$jscomp$6$$.d, $g$jscomp$3_m$jscomp$2$$ = $e$jscomp$14_n$jscomp$4$$.length, $h$jscomp$10$$ = $d$jscomp$5_l$jscomp$2$$[0];
    for ($d$jscomp$5_l$jscomp$2$$ = $d$jscomp$5_l$jscomp$2$$[1]; $b$jscomp$6$$ < $d$jscomp$5_l$jscomp$2$$ && !($f$jscomp$7$$ >= $g$jscomp$3_m$jscomp$2$$);) {
      $a$jscomp$6$$ |= $e$jscomp$14_n$jscomp$4$$[$f$jscomp$7$$++] << $b$jscomp$6$$, $b$jscomp$6$$ += 8;
    }
    $e$jscomp$14_n$jscomp$4$$ = $h$jscomp$10$$[$a$jscomp$6$$ & (1 << $d$jscomp$5_l$jscomp$2$$) - 1];
    $g$jscomp$3_m$jscomp$2$$ = $e$jscomp$14_n$jscomp$4$$ >>> 16;
    if ($g$jscomp$3_m$jscomp$2$$ > $b$jscomp$6$$) {
      throw Error("invalid code length: " + $g$jscomp$3_m$jscomp$2$$);
    }
    $c$jscomp$6$$.f = $a$jscomp$6$$ >> $g$jscomp$3_m$jscomp$2$$;
    $c$jscomp$6$$.c = $b$jscomp$6$$ - $g$jscomp$3_m$jscomp$2$$;
    $c$jscomp$6$$.d = $f$jscomp$7$$;
    return $e$jscomp$14_n$jscomp$4$$ & 65535;
  }
  var $aa$$ = this, $t$$ = "undefined" !== typeof Uint8Array && "undefined" !== typeof Uint16Array && "undefined" !== typeof Uint32Array && "undefined" !== typeof DataView, $A$$ = 0, $y$jscomp$74$$ = 1;
  $w$jscomp$11$$.prototype.t = function() {
    for (; !this.$l$;) {
      var $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = $B$$(this, 3);
      $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ & 1 && (this.$l$ = !0);
      $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ >>>= 1;
      switch($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$) {
        case 0:
          $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = this.input;
          var $M_R_a$jscomp$4$$ = this.d, $F$jscomp$1_b$jscomp$4$$ = this.b, $e$jscomp$12_q$$ = this.a, $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.length, $l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = $F$jscomp$1_b$jscomp$4$$.length;
          this.c = this.f = 0;
          if ($M_R_a$jscomp$4$$ + 1 >= $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$) {
            throw Error("invalid uncompressed block header: LEN");
          }
          var $g$jscomp$1_s$jscomp$6_v$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] | $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] << 8;
          if ($M_R_a$jscomp$4$$ + 1 >= $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$) {
            throw Error("invalid uncompressed block header: NLEN");
          }
          $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] | $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++] << 8;
          if ($g$jscomp$1_s$jscomp$6_v$$ === ~$Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$) {
            throw Error("invalid uncompressed block header: length verify");
          }
          if ($M_R_a$jscomp$4$$ + $g$jscomp$1_s$jscomp$6_v$$ > $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.length) {
            throw Error("input buffer is broken");
          }
          switch(this.$i$) {
            case $A$$:
              for (; $e$jscomp$12_q$$ + $g$jscomp$1_s$jscomp$6_v$$ > $F$jscomp$1_b$jscomp$4$$.length;) {
                $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$ = $l$jscomp$1_p$jscomp$2_z$jscomp$17$$ - $e$jscomp$12_q$$;
                $g$jscomp$1_s$jscomp$6_v$$ -= $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$;
                if ($t$$) {
                  $F$jscomp$1_b$jscomp$4$$.set($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.subarray($M_R_a$jscomp$4$$, $M_R_a$jscomp$4$$ + $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$), $e$jscomp$12_q$$), $e$jscomp$12_q$$ += $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$, $M_R_a$jscomp$4$$ += $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$;
                } else {
                  for (; $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$--;) {
                    $F$jscomp$1_b$jscomp$4$$[$e$jscomp$12_q$$++] = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++];
                  }
                }
                this.a = $e$jscomp$12_q$$;
                $F$jscomp$1_b$jscomp$4$$ = this.e();
                $e$jscomp$12_q$$ = this.a;
              }
              break;
            case $y$jscomp$74$$:
              for (; $e$jscomp$12_q$$ + $g$jscomp$1_s$jscomp$6_v$$ > $F$jscomp$1_b$jscomp$4$$.length;) {
                $F$jscomp$1_b$jscomp$4$$ = this.e({$o$:2});
              }
              break;
            default:
              throw Error("invalid inflate mode");
          }if ($t$$) {
            $F$jscomp$1_b$jscomp$4$$.set($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$.subarray($M_R_a$jscomp$4$$, $M_R_a$jscomp$4$$ + $g$jscomp$1_s$jscomp$6_v$$), $e$jscomp$12_q$$), $e$jscomp$12_q$$ += $g$jscomp$1_s$jscomp$6_v$$, $M_R_a$jscomp$4$$ += $g$jscomp$1_s$jscomp$6_v$$;
          } else {
            for (; $g$jscomp$1_s$jscomp$6_v$$--;) {
              $F$jscomp$1_b$jscomp$4$$[$e$jscomp$12_q$$++] = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$[$M_R_a$jscomp$4$$++];
            }
          }
          this.d = $M_R_a$jscomp$4$$;
          this.a = $e$jscomp$12_q$$;
          this.b = $F$jscomp$1_b$jscomp$4$$;
          break;
        case 1:
          this.$j$($ba$$, $ca$$);
          break;
        case 2:
          $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = $B$$(this, 5) + 257;
          $l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = $B$$(this, 5) + 1;
          $g$jscomp$1_s$jscomp$6_v$$ = $B$$(this, 4) + 4;
          $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$ = new ($t$$ ? Uint8Array : Array)($C$$.length);
          $F$jscomp$1_b$jscomp$4$$ = $M_R_a$jscomp$4$$ = void 0;
          var $T$$;
          for ($e$jscomp$12_q$$ = 0; $e$jscomp$12_q$$ < $g$jscomp$1_s$jscomp$6_v$$; ++$e$jscomp$12_q$$) {
            $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$[$C$$[$e$jscomp$12_q$$]] = $B$$(this, 3);
          }
          if (!$t$$) {
            for ($e$jscomp$12_q$$ = $g$jscomp$1_s$jscomp$6_v$$, $g$jscomp$1_s$jscomp$6_v$$ = $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$.length; $e$jscomp$12_q$$ < $g$jscomp$1_s$jscomp$6_v$$; ++$e$jscomp$12_q$$) {
              $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$[$C$$[$e$jscomp$12_q$$]] = 0;
            }
          }
          $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$ = $u$$($Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$);
          $g$jscomp$1_s$jscomp$6_v$$ = new ($t$$ ? Uint8Array : Array)($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ + $l$jscomp$1_p$jscomp$2_z$jscomp$17$$);
          $e$jscomp$12_q$$ = 0;
          for ($T$$ = $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ + $l$jscomp$1_p$jscomp$2_z$jscomp$17$$; $e$jscomp$12_q$$ < $T$$;) {
            switch($M_R_a$jscomp$4$$ = $D$$(this, $Q_f$jscomp$5_h$jscomp$8_n$jscomp$3_x$jscomp$92$$), $M_R_a$jscomp$4$$) {
              case 16:
                for ($l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = 3 + $B$$(this, 2); $l$jscomp$1_p$jscomp$2_z$jscomp$17$$--;) {
                  $g$jscomp$1_s$jscomp$6_v$$[$e$jscomp$12_q$$++] = $F$jscomp$1_b$jscomp$4$$;
                }
                break;
              case 17:
                for ($l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = 3 + $B$$(this, 3); $l$jscomp$1_p$jscomp$2_z$jscomp$17$$--;) {
                  $g$jscomp$1_s$jscomp$6_v$$[$e$jscomp$12_q$$++] = 0;
                }
                $F$jscomp$1_b$jscomp$4$$ = 0;
                break;
              case 18:
                for ($l$jscomp$1_p$jscomp$2_z$jscomp$17$$ = 11 + $B$$(this, 7); $l$jscomp$1_p$jscomp$2_z$jscomp$17$$--;) {
                  $g$jscomp$1_s$jscomp$6_v$$[$e$jscomp$12_q$$++] = 0;
                }
                $F$jscomp$1_b$jscomp$4$$ = 0;
                break;
              default:
                $F$jscomp$1_b$jscomp$4$$ = $g$jscomp$1_s$jscomp$6_v$$[$e$jscomp$12_q$$++] = $M_R_a$jscomp$4$$;
            }
          }
          $M_R_a$jscomp$4$$ = $t$$ ? $u$$($g$jscomp$1_s$jscomp$6_v$$.subarray(0, $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$)) : $u$$($g$jscomp$1_s$jscomp$6_v$$.slice(0, $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$));
          $S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$ = $t$$ ? $u$$($g$jscomp$1_s$jscomp$6_v$$.subarray($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$)) : $u$$($g$jscomp$1_s$jscomp$6_v$$.slice($S_c$jscomp$4_d$jscomp$3_m$jscomp$1$$));
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
    for (var $e$jscomp$15$$ = $a$jscomp$7$$.length - 258, $f$jscomp$8_g$jscomp$4$$, $h$jscomp$11$$, $l$jscomp$3$$; 256 !== ($f$jscomp$8_g$jscomp$4$$ = $D$$(this, $c$jscomp$7$$));) {
      if (256 > $f$jscomp$8_g$jscomp$4$$) {
        $b$jscomp$7$$ >= $e$jscomp$15$$ && (this.a = $b$jscomp$7$$, $a$jscomp$7$$ = this.e(), $b$jscomp$7$$ = this.a), $a$jscomp$7$$[$b$jscomp$7$$++] = $f$jscomp$8_g$jscomp$4$$;
      } else {
        for ($f$jscomp$8_g$jscomp$4$$ -= 257, $l$jscomp$3$$ = $H$$[$f$jscomp$8_g$jscomp$4$$], 0 < $J$$[$f$jscomp$8_g$jscomp$4$$] && ($l$jscomp$3$$ += $B$$(this, $J$$[$f$jscomp$8_g$jscomp$4$$])), $f$jscomp$8_g$jscomp$4$$ = $D$$(this, $d$jscomp$6$$), $h$jscomp$11$$ = $L$$[$f$jscomp$8_g$jscomp$4$$], 0 < $O$$[$f$jscomp$8_g$jscomp$4$$] && ($h$jscomp$11$$ += $B$$(this, $O$$[$f$jscomp$8_g$jscomp$4$$])), $b$jscomp$7$$ >= $e$jscomp$15$$ && (this.a = $b$jscomp$7$$, $a$jscomp$7$$ = this.e(), $b$jscomp$7$$ = 
        this.a); $l$jscomp$3$$--;) {
          $a$jscomp$7$$[$b$jscomp$7$$] = $a$jscomp$7$$[$b$jscomp$7$$++ - $h$jscomp$11$$];
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
    for (var $e$jscomp$16$$ = $a$jscomp$8$$.length, $f$jscomp$9_g$jscomp$5$$, $h$jscomp$12$$, $l$jscomp$4$$; 256 !== ($f$jscomp$9_g$jscomp$5$$ = $D$$(this, $c$jscomp$8$$));) {
      if (256 > $f$jscomp$9_g$jscomp$5$$) {
        $b$jscomp$8$$ >= $e$jscomp$16$$ && ($a$jscomp$8$$ = this.e(), $e$jscomp$16$$ = $a$jscomp$8$$.length), $a$jscomp$8$$[$b$jscomp$8$$++] = $f$jscomp$9_g$jscomp$5$$;
      } else {
        for ($f$jscomp$9_g$jscomp$5$$ -= 257, $l$jscomp$4$$ = $H$$[$f$jscomp$9_g$jscomp$5$$], 0 < $J$$[$f$jscomp$9_g$jscomp$5$$] && ($l$jscomp$4$$ += $B$$(this, $J$$[$f$jscomp$9_g$jscomp$5$$])), $f$jscomp$9_g$jscomp$5$$ = $D$$(this, $d$jscomp$7$$), $h$jscomp$12$$ = $L$$[$f$jscomp$9_g$jscomp$5$$], 0 < $O$$[$f$jscomp$9_g$jscomp$5$$] && ($h$jscomp$12$$ += $B$$(this, $O$$[$f$jscomp$9_g$jscomp$5$$])), $b$jscomp$8$$ + $l$jscomp$4$$ > $e$jscomp$16$$ && ($a$jscomp$8$$ = this.e(), $e$jscomp$16$$ = $a$jscomp$8$$.length); $l$jscomp$4$$--;) {
          $a$jscomp$8$$[$b$jscomp$8$$] = $a$jscomp$8$$[$b$jscomp$8$$++ - $h$jscomp$12$$];
        }
      }
    }
    for (; 8 <= this.c;) {
      this.c -= 8, this.d--;
    }
    this.a = $b$jscomp$8$$;
  };
  $w$jscomp$11$$.prototype.e = function() {
    var $c$jscomp$9$$ = new ($t$$ ? Uint8Array : Array)(this.a - 32768), $d$jscomp$8$$ = this.a - 32768, $b$jscomp$9$$, $e$jscomp$17$$ = this.b;
    if ($t$$) {
      $c$jscomp$9$$.set($e$jscomp$17$$.subarray(32768, $c$jscomp$9$$.length));
    } else {
      var $a$jscomp$9$$ = 0;
      for ($b$jscomp$9$$ = $c$jscomp$9$$.length; $a$jscomp$9$$ < $b$jscomp$9$$; ++$a$jscomp$9$$) {
        $c$jscomp$9$$[$a$jscomp$9$$] = $e$jscomp$17$$[$a$jscomp$9$$ + 32768];
      }
    }
    this.$g$.push($c$jscomp$9$$);
    this.k += $c$jscomp$9$$.length;
    if ($t$$) {
      $e$jscomp$17$$.set($e$jscomp$17$$.subarray($d$jscomp$8$$, $d$jscomp$8$$ + 32768));
    } else {
      for ($a$jscomp$9$$ = 0; 32768 > $a$jscomp$9$$; ++$a$jscomp$9$$) {
        $e$jscomp$17$$[$a$jscomp$9$$] = $e$jscomp$17$$[$d$jscomp$8$$ + $a$jscomp$9$$];
      }
    }
    this.a = 32768;
    return $e$jscomp$17$$;
  };
  $w$jscomp$11$$.prototype.$u$ = function($c$jscomp$10$$) {
    var $d$jscomp$9$$, $a$jscomp$10$$ = this.input.length / this.d + 1 | 0, $b$jscomp$10$$, $e$jscomp$18$$, $f$jscomp$10$$, $g$jscomp$6$$ = this.input, $h$jscomp$13$$ = this.b;
    $c$jscomp$10$$ && ("number" === typeof $c$jscomp$10$$.$o$ && ($a$jscomp$10$$ = $c$jscomp$10$$.$o$), "number" === typeof $c$jscomp$10$$.q && ($a$jscomp$10$$ += $c$jscomp$10$$.q));
    2 > $a$jscomp$10$$ ? ($b$jscomp$10$$ = ($g$jscomp$6$$.length - this.d) / this.n[2], $f$jscomp$10$$ = $b$jscomp$10$$ / 2 * 258 | 0, $e$jscomp$18$$ = $f$jscomp$10$$ < $h$jscomp$13$$.length ? $h$jscomp$13$$.length + $f$jscomp$10$$ : $h$jscomp$13$$.length << 1) : $e$jscomp$18$$ = $h$jscomp$13$$.length * $a$jscomp$10$$;
    $t$$ ? ($d$jscomp$9$$ = new Uint8Array($e$jscomp$18$$), $d$jscomp$9$$.set($h$jscomp$13$$)) : $d$jscomp$9$$ = $h$jscomp$13$$;
    return this.b = $d$jscomp$9$$;
  };
  $w$jscomp$11$$.prototype.$m$ = function() {
    var $c$jscomp$11$$ = 0, $d$jscomp$10$$ = this.b, $a$jscomp$11$$ = this.$g$, $e$jscomp$19$$ = new ($t$$ ? Uint8Array : Array)(this.k + (this.a - 32768)), $g$jscomp$7$$, $l$jscomp$5$$;
    if (0 === $a$jscomp$11$$.length) {
      return $t$$ ? this.b.subarray(32768, this.a) : this.b.slice(32768, this.a);
    }
    var $f$jscomp$11$$ = 0;
    for ($g$jscomp$7$$ = $a$jscomp$11$$.length; $f$jscomp$11$$ < $g$jscomp$7$$; ++$f$jscomp$11$$) {
      var $b$jscomp$11$$ = $a$jscomp$11$$[$f$jscomp$11$$];
      var $h$jscomp$14$$ = 0;
      for ($l$jscomp$5$$ = $b$jscomp$11$$.length; $h$jscomp$14$$ < $l$jscomp$5$$; ++$h$jscomp$14$$) {
        $e$jscomp$19$$[$c$jscomp$11$$++] = $b$jscomp$11$$[$h$jscomp$14$$];
      }
    }
    $f$jscomp$11$$ = 32768;
    for ($g$jscomp$7$$ = this.a; $f$jscomp$11$$ < $g$jscomp$7$$; ++$f$jscomp$11$$) {
      $e$jscomp$19$$[$c$jscomp$11$$++] = $d$jscomp$10$$[$f$jscomp$11$$];
    }
    this.$g$ = [];
    return this.buffer = $e$jscomp$19$$;
  };
  $w$jscomp$11$$.prototype.r = function() {
    var $c$jscomp$12$$, $d$jscomp$11$$ = this.a;
    $t$$ ? this.p ? ($c$jscomp$12$$ = new Uint8Array($d$jscomp$11$$), $c$jscomp$12$$.set(this.b.subarray(0, $d$jscomp$11$$))) : $c$jscomp$12$$ = this.b.subarray(0, $d$jscomp$11$$) : (this.b.length > $d$jscomp$11$$ && (this.b.length = $d$jscomp$11$$), $c$jscomp$12$$ = this.b);
    return this.buffer = $c$jscomp$12$$;
  };
  $r$jscomp$1$$("Zlib.RawInflate", $w$jscomp$11$$);
  $r$jscomp$1$$("Zlib.RawInflate.prototype.decompress", $w$jscomp$11$$.prototype.t);
  $E_G_I_K_N_P_V_X$$ = {$ADAPTIVE$:$y$jscomp$74$$, $BLOCK$:$A$$};
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
    $r$jscomp$1$$("Zlib.RawInflate.BufferType." + $Z$$, $E_G_I_K_N_P_V_X$$[$Z$$]);
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
  this.$readAttributes$ = function($obj$jscomp$33$$) {
    this.$readString$();
    for (var $size$jscomp$25$$ = this.$readInt$(), $i$jscomp$29$$ = 0; $i$jscomp$29$$ < $size$jscomp$25$$; $i$jscomp$29$$++) {
      if (-1 !== this.$readShort$()) {
        console.error("RPClass not supported, yet.");
        break;
      }
      var $key$jscomp$48$$ = this.$readString$(), $value$jscomp$98$$ = this.$readString$();
      $obj$jscomp$33$$[$key$jscomp$48$$] = $value$jscomp$98$$;
    }
  };
};
$marauroa$$.$Deserializer$.$binaryStringToUint$ = function($binary$$) {
  for (var $len$jscomp$1$$ = $binary$$.length, $bytes$jscomp$3$$ = new Uint8Array($len$jscomp$1$$), $i$jscomp$30$$ = 0; $i$jscomp$30$$ < $len$jscomp$1$$; $i$jscomp$30$$++) {
    $bytes$jscomp$3$$[$i$jscomp$30$$] = $binary$$.charCodeAt($i$jscomp$30$$);
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
  for (var $len$jscomp$2$$ = $binary$jscomp$1$$.length, $bytes$jscomp$4$$ = new Uint8Array($len$jscomp$2$$), $i$jscomp$31$$ = 0; $i$jscomp$31$$ < $len$jscomp$2$$; $i$jscomp$31$$++) {
    $bytes$jscomp$4$$[$i$jscomp$31$$] = $binary$jscomp$1$$.charCodeAt($i$jscomp$31$$);
  }
  return new $marauroa$$.$Deserializer$($bytes$jscomp$4$$.buffer);
};
var $stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$config$ = {$sound$:{play:!1}, $gamescreen$:{$blood$:!0}};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.data = $stendhal$$.data || {};
$stendhal$$.data.$build$ = {version:"1.37.5", $build$:"2021-12-28 20:51:22", $dist$:"webclient"};
JXG = {$Util$:{}};
JXG.$Util$.$Unzip$ = function() {
  Array(17)[0] = 0;
};
JXG.$Util$.$Base64$ = {$_keyStr$:"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=", encode:function($input$jscomp$9$$) {
  var $output$jscomp$3$$ = [], $i$jscomp$39$$ = 0;
  for ($input$jscomp$9$$ = JXG.$Util$.$Base64$.$_utf8_encode$($input$jscomp$9$$); $i$jscomp$39$$ < $input$jscomp$9$$.length;) {
    var $chr1_enc2$$ = $input$jscomp$9$$.charCodeAt($i$jscomp$39$$++);
    var $chr2$$ = $input$jscomp$9$$.charCodeAt($i$jscomp$39$$++);
    var $chr3$$ = $input$jscomp$9$$.charCodeAt($i$jscomp$39$$++);
    var $enc1$$ = $chr1_enc2$$ >> 2;
    $chr1_enc2$$ = ($chr1_enc2$$ & 3) << 4 | $chr2$$ >> 4;
    var $enc3$$ = ($chr2$$ & 15) << 2 | $chr3$$ >> 6;
    var $enc4$$ = $chr3$$ & 63;
    isNaN($chr2$$) ? $enc3$$ = $enc4$$ = 64 : isNaN($chr3$$) && ($enc4$$ = 64);
    $output$jscomp$3$$.push([this.$_keyStr$.charAt($enc1$$), this.$_keyStr$.charAt($chr1_enc2$$), this.$_keyStr$.charAt($enc3$$), this.$_keyStr$.charAt($enc4$$)].join(""));
  }
  return $output$jscomp$3$$.join("");
}, decode:function($input$jscomp$10$$, $utf8$$) {
  var $output$jscomp$4$$ = [], $i$jscomp$40$$ = 0;
  for ($input$jscomp$10$$ = $input$jscomp$10$$.replace(/[^A-Za-z0-9\+\/=]/g, ""); $i$jscomp$40$$ < $input$jscomp$10$$.length;) {
    var $chr1$jscomp$1_enc1$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$40$$++));
    var $chr2$jscomp$1_enc2$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$40$$++));
    var $enc3$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$40$$++));
    var $enc4$jscomp$1$$ = this.$_keyStr$.indexOf($input$jscomp$10$$.charAt($i$jscomp$40$$++));
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
}, $_utf8_encode$:function($string$jscomp$4$$) {
  $string$jscomp$4$$ = $string$jscomp$4$$.replace(/\r\n/g, "\n");
  for (var $utftext$$ = "", $n$jscomp$6$$ = 0; $n$jscomp$6$$ < $string$jscomp$4$$.length; $n$jscomp$6$$++) {
    var $c$jscomp$16$$ = $string$jscomp$4$$.charCodeAt($n$jscomp$6$$);
    128 > $c$jscomp$16$$ ? $utftext$$ += String.fromCharCode($c$jscomp$16$$) : (127 < $c$jscomp$16$$ && 2048 > $c$jscomp$16$$ ? $utftext$$ += String.fromCharCode($c$jscomp$16$$ >> 6 | 192) : ($utftext$$ += String.fromCharCode($c$jscomp$16$$ >> 12 | 224), $utftext$$ += String.fromCharCode($c$jscomp$16$$ >> 6 & 63 | 128)), $utftext$$ += String.fromCharCode($c$jscomp$16$$ & 63 | 128));
  }
  return $utftext$$;
}, $_utf8_decode$:function($utftext$jscomp$1$$) {
  for (var $string$jscomp$5$$ = [], $i$jscomp$41$$ = 0, $c$jscomp$17$$, $c2$$, $c3$jscomp$1$$; $i$jscomp$41$$ < $utftext$jscomp$1$$.length;) {
    $c$jscomp$17$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$41$$), 128 > $c$jscomp$17$$ ? ($string$jscomp$5$$.push(String.fromCharCode($c$jscomp$17$$)), $i$jscomp$41$$++) : 191 < $c$jscomp$17$$ && 224 > $c$jscomp$17$$ ? ($c2$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$41$$ + 1), $string$jscomp$5$$.push(String.fromCharCode(($c$jscomp$17$$ & 31) << 6 | $c2$$ & 63)), $i$jscomp$41$$ += 2) : ($c2$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$41$$ + 1), $c3$jscomp$1$$ = $utftext$jscomp$1$$.charCodeAt($i$jscomp$41$$ + 
    2), $string$jscomp$5$$.push(String.fromCharCode(($c$jscomp$17$$ & 15) << 12 | ($c2$$ & 63) << 6 | $c3$jscomp$1$$ & 63)), $i$jscomp$41$$ += 3);
  }
  return $string$jscomp$5$$.join("");
}, $_destrip$:function($stripped$$, $wrap$$) {
  var $lines$$ = [], $i$jscomp$42$$, $destripped$$ = [];
  null == $wrap$$ && ($wrap$$ = 76);
  $stripped$$.replace(/ /g, "");
  var $lineno$$ = $stripped$$.length / $wrap$$;
  for ($i$jscomp$42$$ = 0; $i$jscomp$42$$ < $lineno$$; $i$jscomp$42$$++) {
    $lines$$[$i$jscomp$42$$] = $stripped$$.substr($i$jscomp$42$$ * $wrap$$, $wrap$$);
  }
  $lineno$$ != $stripped$$.length / $wrap$$ && ($lines$$[$lines$$.length] = $stripped$$.substr($lineno$$ * $wrap$$, $stripped$$.length - $lineno$$ * $wrap$$));
  for ($i$jscomp$42$$ = 0; $i$jscomp$42$$ < $lines$$.length; $i$jscomp$42$$++) {
    $destripped$$.push($lines$$[$i$jscomp$42$$]);
  }
  return $destripped$$.join("\n");
}, $decodeAsArray$:function($dec_input$jscomp$11$$) {
  $dec_input$jscomp$11$$ = this.decode($dec_input$jscomp$11$$);
  var $ar$$ = [], $i$jscomp$43$$;
  for ($i$jscomp$43$$ = 0; $i$jscomp$43$$ < $dec_input$jscomp$11$$.length; $i$jscomp$43$$++) {
    $ar$$[$i$jscomp$43$$] = $dec_input$jscomp$11$$.charCodeAt($i$jscomp$43$$);
  }
  return $ar$$;
}, $decodeGEONExT$:function($input$jscomp$12$$) {
  return decodeAsArray(destrip($input$jscomp$12$$), !1);
}};
JXG.$Util$.$asciiCharCodeAt$ = function($c$jscomp$18_str$jscomp$6$$, $i$jscomp$44$$) {
  $c$jscomp$18_str$jscomp$6$$ = $c$jscomp$18_str$jscomp$6$$.charCodeAt($i$jscomp$44$$);
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
  for (var $string$jscomp$6$$ = [], $i$jscomp$45$$ = 0, $c$jscomp$19$$, $c2$jscomp$1$$; $i$jscomp$45$$ < $utftext$jscomp$2$$.length;) {
    $c$jscomp$19$$ = $utftext$jscomp$2$$.charCodeAt($i$jscomp$45$$), 128 > $c$jscomp$19$$ ? ($string$jscomp$6$$.push(String.fromCharCode($c$jscomp$19$$)), $i$jscomp$45$$++) : 191 < $c$jscomp$19$$ && 224 > $c$jscomp$19$$ ? ($c2$jscomp$1$$ = $utftext$jscomp$2$$.charCodeAt($i$jscomp$45$$ + 1), $string$jscomp$6$$.push(String.fromCharCode(($c$jscomp$19$$ & 31) << 6 | $c2$jscomp$1$$ & 63)), $i$jscomp$45$$ += 2) : ($c2$jscomp$1$$ = $utftext$jscomp$2$$.charCodeAt($i$jscomp$45$$ + 1), c3 = $utftext$jscomp$2$$.charCodeAt($i$jscomp$45$$ + 
    2), $string$jscomp$6$$.push(String.fromCharCode(($c$jscomp$19$$ & 15) << 12 | ($c2$jscomp$1$$ & 63) << 6 | c3 & 63)), $i$jscomp$45$$ += 3);
  }
  return $string$jscomp$6$$.join("");
};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.data = $stendhal$$.data || {};
$stendhal$$.data.cache = {$init$:function() {
  function $requestAllItems$$($store_storeName$$, $callback$jscomp$47$$) {
    var $cursorRequest_tx$jscomp$2$$ = $stendhal$$.data.cache.db.transaction($store_storeName$$, IDBTransaction.READ_ONLY);
    $store_storeName$$ = $cursorRequest_tx$jscomp$2$$.objectStore($store_storeName$$);
    var $items$jscomp$3$$ = [];
    $cursorRequest_tx$jscomp$2$$.oncomplete = function() {
      $callback$jscomp$47$$($items$jscomp$3$$);
    };
    $cursorRequest_tx$jscomp$2$$ = $store_storeName$$.openCursor();
    $cursorRequest_tx$jscomp$2$$.onerror = function($error$jscomp$8$$) {
      console.log($error$jscomp$8$$);
    };
    $cursorRequest_tx$jscomp$2$$.onsuccess = function($cursor_evt$jscomp$33$$) {
      if ($cursor_evt$jscomp$33$$ = $cursor_evt$jscomp$33$$.target.result) {
        $items$jscomp$3$$.push($cursor_evt$jscomp$33$$.value), $cursor_evt$jscomp$33$$.continue();
      }
    };
  }
  $stendhal$$.data.cache.sync = {cid:"noIndexedDB"};
  if (window.indexedDB) {
    var $open$jscomp$2$$ = indexedDB.open("stendhal", 1);
    $open$jscomp$2$$.onupgradeneeded = function() {
      $open$jscomp$2$$.result.createObjectStore("cache", {keyPath:"key"});
    };
    $open$jscomp$2$$.onsuccess = function() {
      $stendhal$$.data.cache.db = $open$jscomp$2$$.result;
      $requestAllItems$$("cache", function($items$jscomp$4$$) {
        for (var $len$jscomp$6$$ = $items$jscomp$4$$.length, $i$jscomp$46$$ = 0; $i$jscomp$46$$ < $len$jscomp$6$$; $i$jscomp$46$$ += 1) {
          $stendhal$$.data.cache.sync[$items$jscomp$4$$[$i$jscomp$46$$].key] = $items$jscomp$4$$[$i$jscomp$46$$].data;
        }
        "noIndexedDB" === $stendhal$$.data.cache.sync.cid && $stendhal$$.data.cache.put("cid", (1e48 * Math.random()).toString(36));
      });
    };
    $open$jscomp$2$$.onerror = function() {
      $marauroa$$ = {};
      $stendhal$$ = {};
      alert("Could not initialize cache.");
    };
  }
}, get:function($key$jscomp$49$$) {
  return $stendhal$$.data.cache.sync[$key$jscomp$49$$];
}, put:function($key$jscomp$50$$, $value$jscomp$99$$) {
  $stendhal$$.data.cache.sync[$key$jscomp$50$$] !== $value$jscomp$99$$ && ($stendhal$$.data.cache.sync[$key$jscomp$50$$] = $value$jscomp$99$$, $stendhal$$.data.cache.db.transaction("cache", "readwrite").objectStore("cache").put({key:$key$jscomp$50$$, data:$value$jscomp$99$$}));
}};
$stendhal$$.data.cache.$init$();
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.data = $stendhal$$.data || {};
$stendhal$$.data.$cstatus$ = {send:function() {
  if ($marauroa$$.$me$ && "noIndexedDB" !== $stendhal$$.data.cache.get("cid")) {
    var $action$jscomp$1$$ = {type:"cstatus", version:$stendhal$$.data.$build$.version, build:$stendhal$$.data.$build$.$build$, dist:$stendhal$$.data.$build$.$dist$, cid:$stendhal$$.data.cache.get("cid")};
    $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$1$$);
  } else {
    window.setTimeout($stendhal$$.data.$cstatus$.send, 1000);
  }
}};
window.setTimeout($stendhal$$.data.$cstatus$.send, 1000);
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.data = $stendhal$$.data || {};
$stendhal$$.data.group = {$members$:[], $lootmode$:"", $leader$:"", $updateGroupStatus$:function($memberArray_members$$, $leader$$, $lootmode$$) {
  if ($memberArray_members$$) {
    $memberArray_members$$ = $memberArray_members$$.substring(1, $memberArray_members$$.length - 1).split("\t");
    $stendhal$$.data.group.$members$ = {};
    for (var $i$jscomp$47$$ = 0; $i$jscomp$47$$ < $memberArray_members$$.length; $i$jscomp$47$$++) {
      $stendhal$$.data.group.$members$[$memberArray_members$$[$i$jscomp$47$$]] = !0;
    }
    $stendhal$$.data.group.$leader$ = $leader$$;
    $stendhal$$.data.group.$lootmode$ = $lootmode$$;
  } else {
    $stendhal$$.data.group.$members$ = [], $stendhal$$.data.group.$leader$ = "", $stendhal$$.data.group.$lootmode$ = "";
  }
}};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.data = $stendhal$$.data || {};
function $ImagePreloader$$($images$$, $callback$jscomp$48_i$jscomp$48$$) {
  this.$F$ = $callback$jscomp$48_i$jscomp$48$$;
  this.$A$ = this.$nLoaded$ = 0;
  $stendhal$$.data.map.$aImages$ = [];
  this.$G$ = $images$$.length;
  for ($callback$jscomp$48_i$jscomp$48$$ = 0; $callback$jscomp$48_i$jscomp$48$$ < $images$$.length; $callback$jscomp$48_i$jscomp$48$$++) {
    var $image$jscomp$inline_44$$ = $images$$[$callback$jscomp$48_i$jscomp$48$$], $oImage$jscomp$inline_45$$ = new Image();
    $stendhal$$.data.map.$aImages$.push($oImage$jscomp$inline_45$$);
    $oImage$jscomp$inline_45$$.onload = $ImagePreloader$$.prototype.$C$;
    $oImage$jscomp$inline_45$$.onerror = $ImagePreloader$$.prototype.onerror;
    $oImage$jscomp$inline_45$$.onabort = $ImagePreloader$$.prototype.$B$;
    $oImage$jscomp$inline_45$$.$v$ = this;
    $oImage$jscomp$inline_45$$.$D$ = !1;
    $oImage$jscomp$inline_45$$.src = $image$jscomp$inline_44$$;
  }
}
function $JSCompiler_StaticMethods_onComplete$$($JSCompiler_StaticMethods_onComplete$self$$) {
  $JSCompiler_StaticMethods_onComplete$self$$.$A$++;
  $JSCompiler_StaticMethods_onComplete$self$$.$A$ == $JSCompiler_StaticMethods_onComplete$self$$.$G$ && $JSCompiler_StaticMethods_onComplete$self$$.$F$();
}
$ImagePreloader$$.prototype.$C$ = function() {
  this.$D$ = !0;
  this.$v$.$nLoaded$++;
  $JSCompiler_StaticMethods_onComplete$$(this.$v$);
};
$ImagePreloader$$.prototype.onerror = function() {
  $JSCompiler_StaticMethods_onComplete$$(this.$v$);
  console.error("Error loading " + this.src);
};
$ImagePreloader$$.prototype.$B$ = function() {
  $JSCompiler_StaticMethods_onComplete$$(this.$v$);
  console.error("Loading " + this.src + " was aborted");
};
$stendhal$$.data.map = {$currentZoneName$:"", offsetX:0, offsetY:0, $zoneSizeX$:-1, $zoneSizeY$:-1, $sizeX$:20, $sizeY$:15, $tileWidth$:32, $tileHeight$:32, zoom:100, $aImages$:-1, $layerNames$:-1, $layers$:-1, $firstgids$:-1, $drawingError$:!1, $targetTileWidth$:0, $targetTileHeight$:0, $getTilesetForGid$:function($value$jscomp$100$$) {
  return $value$jscomp$100$$ < this.$gidsindex$.length ? this.$gidsindex$[$value$jscomp$100$$] : this.$gidsindex$[this.$gidsindex$.length - 1] + 1;
}, $onTransfer$:function($zoneName$$, $content$$) {
  $stendhal$$.data.map.$currentZoneName$ = $zoneName$$;
  $stendhal$$.data.map.$firstgids$ = [];
  $stendhal$$.data.map.$layers$ = [];
  $stendhal$$.data.map.$layerNames$ = [];
  document.getElementById("body").style.cursor = "wait";
  console.log("load map");
  $stendhal$$.data.map.$decodeTileset$($content$$, "tilesets");
  $stendhal$$.data.map.$decodeMapLayer$($content$$, "0_floor");
  $stendhal$$.data.map.$decodeMapLayer$($content$$, "1_terrain");
  $stendhal$$.data.map.$decodeMapLayer$($content$$, "2_object");
  $stendhal$$.data.map.$decodeMapLayer$($content$$, "3_roof");
  $stendhal$$.data.map.$decodeMapLayer$($content$$, "4_roof_add");
  $stendhal$$.data.map.$protection$ = $stendhal$$.data.map.$decodeMapLayer$($content$$, "protection");
  $stendhal$$.data.map.$collisionData$ = $stendhal$$.data.map.$decodeMapLayer$($content$$, "collision");
}, $decodeTileset$:function($content$jscomp$1_images$jscomp$1$$, $firstgid_name$jscomp$76$$) {
  var $deserializer_pos$$ = $marauroa$$.$Deserializer$.$fromBase64$($content$jscomp$1_images$jscomp$1$$[$firstgid_name$jscomp$76$$]), $amount$$ = $deserializer_pos$$.$readInt$();
  $content$jscomp$1_images$jscomp$1$$ = [];
  for (var $i$jscomp$49_lastStart$$ = 0; $i$jscomp$49_lastStart$$ < $amount$$; $i$jscomp$49_lastStart$$++) {
    $firstgid_name$jscomp$76$$ = $deserializer_pos$$.$readString$();
    var $filename$jscomp$2_source$jscomp$16$$ = $deserializer_pos$$.$readString$();
    $firstgid_name$jscomp$76$$ = $deserializer_pos$$.$readInt$();
    $filename$jscomp$2_source$jscomp$16$$ = "/" + $filename$jscomp$2_source$jscomp$16$$.replace(/\.\.\/\.\.\//g, "");
    $content$jscomp$1_images$jscomp$1$$.push($filename$jscomp$2_source$jscomp$16$$);
    $stendhal$$.data.map.$firstgids$.push($firstgid_name$jscomp$76$$);
  }
  $stendhal$$.data.map.$gidsindex$ = [];
  for ($deserializer_pos$$ = $i$jscomp$49_lastStart$$ = 0; $deserializer_pos$$ < parseInt($stendhal$$.data.map.$firstgids$.length, 10); $deserializer_pos$$++) {
    for (; $i$jscomp$49_lastStart$$ < $stendhal$$.data.map.$firstgids$[$deserializer_pos$$]; $i$jscomp$49_lastStart$$++) {
      $stendhal$$.data.map.$gidsindex$.push($deserializer_pos$$ - 1);
    }
    $i$jscomp$49_lastStart$$ = $stendhal$$.data.map.$firstgids$[$deserializer_pos$$];
  }
  new $ImagePreloader$$($content$jscomp$1_images$jscomp$1$$, function() {
    document.getElementById("body").style.cursor = "auto";
  });
}, $decodeMapLayer$:function($content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$, $name$jscomp$77$$) {
  if ($content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$ = $content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$[$name$jscomp$77$$]) {
    $content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$ = $marauroa$$.$Deserializer$.$fromDeflatedBase64$($content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$);
    $content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readString$();
    $stendhal$$.data.map.$zoneSizeX$ = $content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readInt$();
    $stendhal$$.data.map.$zoneSizeY$ = $content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readInt$();
    $content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$ = $content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.$readByteArray$();
    for (var $layer$jscomp$2$$ = [], $i$jscomp$50$$ = 0; $i$jscomp$50$$ < $stendhal$$.data.map.$zoneSizeX$ * $stendhal$$.data.map.$zoneSizeY$ * 4 - 3; $i$jscomp$50$$ += 4) {
      var $tileId$$ = ($content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.getUint8($i$jscomp$50$$) >>> 0) + ($content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.getUint8($i$jscomp$50$$ + 1) << 8) + ($content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.getUint8($i$jscomp$50$$ + 2) << 16) + ($content$jscomp$2_deserializer$jscomp$1_layerData$jscomp$1_layerRaw$$.getUint8($i$jscomp$50$$ + 3) << 24);
      $layer$jscomp$2$$.push($tileId$$);
    }
    $stendhal$$.data.map.$layerNames$.push($name$jscomp$77$$);
    $stendhal$$.data.map.$layers$.push($layer$jscomp$2$$);
    return $layer$jscomp$2$$;
  }
}, $collision$:function($x$jscomp$93$$, $y$jscomp$75$$) {
  return 0 != this.$collisionData$[$y$jscomp$75$$ * $stendhal$$.data.map.$zoneSizeX$ + $x$jscomp$93$$];
}, $isProtected$:function($x$jscomp$94$$, $y$jscomp$76$$) {
  return 0 != this.$protection$[$y$jscomp$76$$ * $stendhal$$.data.map.$zoneSizeX$ + $x$jscomp$94$$];
}};
(function($L$jscomp$1$$) {
  function $u$jscomp$1$$($d$jscomp$13$$, $b$jscomp$13$$, $h$jscomp$15_w$jscomp$12$$) {
    var $a$jscomp$16$$ = [], $l$jscomp$7$$ = 0;
    $h$jscomp$15_w$jscomp$12$$ = $h$jscomp$15_w$jscomp$12$$ || {};
    var $e$jscomp$20$$ = $h$jscomp$15_w$jscomp$12$$.encoding || "UTF8";
    $h$jscomp$15_w$jscomp$12$$ = $h$jscomp$15_w$jscomp$12$$.$numRounds$ || 1;
    if ($h$jscomp$15_w$jscomp$12$$ !== parseInt($h$jscomp$15_w$jscomp$12$$, 10) || 1 > $h$jscomp$15_w$jscomp$12$$) {
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
    var $k$jscomp$1$$ = $D$jscomp$1$$($b$jscomp$13$$, $e$jscomp$20$$, 1);
    var $m$jscomp$3$$ = $y$jscomp$77$$($d$jscomp$13$$);
    this.update = function($e$jscomp$23$$) {
      var $d$jscomp$15$$, $h$jscomp$17$$ = 0, $q$jscomp$2$$ = $n$jscomp$7$$ >>> 5;
      var $b$jscomp$16$$ = $k$jscomp$1$$($e$jscomp$23$$, $a$jscomp$16$$, $l$jscomp$7$$);
      $e$jscomp$23$$ = $b$jscomp$16$$.$binLen$;
      var $f$jscomp$14$$ = $b$jscomp$16$$.value;
      $b$jscomp$16$$ = $e$jscomp$23$$ >>> 5;
      for ($d$jscomp$15$$ = 0; $d$jscomp$15$$ < $b$jscomp$16$$; $d$jscomp$15$$ += $q$jscomp$2$$) {
        $h$jscomp$17$$ + $n$jscomp$7$$ <= $e$jscomp$23$$ && ($m$jscomp$3$$ = $g$jscomp$9$$($f$jscomp$14$$.slice($d$jscomp$15$$, $d$jscomp$15$$ + $q$jscomp$2$$), $m$jscomp$3$$), $h$jscomp$17$$ += $n$jscomp$7$$);
      }
      $a$jscomp$16$$ = $f$jscomp$14$$.slice($h$jscomp$17$$ >>> 5);
      $l$jscomp$7$$ = $e$jscomp$23$$ % $n$jscomp$7$$;
    };
  }
  function $g$jscomp$8$$($d$jscomp$16$$, $b$jscomp$19$$) {
    this.a = $d$jscomp$16$$;
    this.b = $b$jscomp$19$$;
  }
  function $D$jscomp$1$$($d$jscomp$22$$, $b$jscomp$25$$, $h$jscomp$24$$) {
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
          var $e$jscomp$30$$ = $c$jscomp$27$$.length, $d$jscomp$23$$, $f$jscomp$16$$;
          if (0 !== $e$jscomp$30$$ % 2) {
            throw Error("String of HEX type must be in byte increments");
          }
          $a$jscomp$32$$ = $a$jscomp$32$$ || [0];
          $b$jscomp$26$$ = $b$jscomp$26$$ || 0;
          var $r$jscomp$3$$ = $b$jscomp$26$$ >>> 3;
          var $q$jscomp$3$$ = -1 === $h$jscomp$24$$ ? 3 : 0;
          for ($d$jscomp$23$$ = 0; $d$jscomp$23$$ < $e$jscomp$30$$; $d$jscomp$23$$ += 2) {
            var $k$jscomp$7$$ = parseInt($c$jscomp$27$$.substr($d$jscomp$23$$, 2), 16);
            if (isNaN($k$jscomp$7$$)) {
              throw Error("String of HEX type contains invalid characters");
            }
            var $g$jscomp$12$$ = ($d$jscomp$23$$ >>> 1) + $r$jscomp$3$$;
            for ($f$jscomp$16$$ = $g$jscomp$12$$ >>> 2; $a$jscomp$32$$.length <= $f$jscomp$16$$;) {
              $a$jscomp$32$$.push(0);
            }
            $a$jscomp$32$$[$f$jscomp$16$$] |= $k$jscomp$7$$ << 8 * ($q$jscomp$3$$ + $g$jscomp$12$$ % 4 * $h$jscomp$24$$);
          }
          return {value:$a$jscomp$32$$, $binLen$:4 * $e$jscomp$30$$ + $b$jscomp$26$$};
        };
        break;
      case "TEXT":
        $d$jscomp$22$$ = function($c$jscomp$28$$, $a$jscomp$33$$, $d$jscomp$24$$) {
          var $k$jscomp$8$$ = 0, $f$jscomp$17$$, $g$jscomp$13$$, $q$jscomp$4$$;
          $a$jscomp$33$$ = $a$jscomp$33$$ || [0];
          $d$jscomp$24$$ = $d$jscomp$24$$ || 0;
          var $r$jscomp$4$$ = $d$jscomp$24$$ >>> 3;
          if ("UTF8" === $b$jscomp$25$$) {
            var $p$jscomp$6$$ = -1 === $h$jscomp$24$$ ? 3 : 0;
            for ($f$jscomp$17$$ = 0; $f$jscomp$17$$ < $c$jscomp$28$$.length; $f$jscomp$17$$ += 1) {
              var $e$jscomp$31$$ = $c$jscomp$28$$.charCodeAt($f$jscomp$17$$);
              var $m$jscomp$7$$ = [];
              128 > $e$jscomp$31$$ ? $m$jscomp$7$$.push($e$jscomp$31$$) : 2048 > $e$jscomp$31$$ ? ($m$jscomp$7$$.push(192 | $e$jscomp$31$$ >>> 6), $m$jscomp$7$$.push(128 | $e$jscomp$31$$ & 63)) : 55296 > $e$jscomp$31$$ || 57344 <= $e$jscomp$31$$ ? $m$jscomp$7$$.push(224 | $e$jscomp$31$$ >>> 12, 128 | $e$jscomp$31$$ >>> 6 & 63, 128 | $e$jscomp$31$$ & 63) : ($f$jscomp$17$$ += 1, $e$jscomp$31$$ = 65536 + (($e$jscomp$31$$ & 1023) << 10 | $c$jscomp$28$$.charCodeAt($f$jscomp$17$$) & 1023), $m$jscomp$7$$.push(240 | 
              $e$jscomp$31$$ >>> 18, 128 | $e$jscomp$31$$ >>> 12 & 63, 128 | $e$jscomp$31$$ >>> 6 & 63, 128 | $e$jscomp$31$$ & 63));
              for ($g$jscomp$13$$ = 0; $g$jscomp$13$$ < $m$jscomp$7$$.length; $g$jscomp$13$$ += 1) {
                var $v$jscomp$2$$ = $k$jscomp$8$$ + $r$jscomp$4$$;
                for ($q$jscomp$4$$ = $v$jscomp$2$$ >>> 2; $a$jscomp$33$$.length <= $q$jscomp$4$$;) {
                  $a$jscomp$33$$.push(0);
                }
                $a$jscomp$33$$[$q$jscomp$4$$] |= $m$jscomp$7$$[$g$jscomp$13$$] << 8 * ($p$jscomp$6$$ + $v$jscomp$2$$ % 4 * $h$jscomp$24$$);
                $k$jscomp$8$$ += 1;
              }
            }
          } else {
            if ("UTF16BE" === $b$jscomp$25$$ || "UTF16LE" === $b$jscomp$25$$) {
              for ($p$jscomp$6$$ = -1 === $h$jscomp$24$$ ? 2 : 0, $f$jscomp$17$$ = 0; $f$jscomp$17$$ < $c$jscomp$28$$.length; $f$jscomp$17$$ += 1) {
                $e$jscomp$31$$ = $c$jscomp$28$$.charCodeAt($f$jscomp$17$$);
                "UTF16LE" === $b$jscomp$25$$ && ($g$jscomp$13$$ = $e$jscomp$31$$ & 255, $e$jscomp$31$$ = $g$jscomp$13$$ << 8 | $e$jscomp$31$$ >>> 8);
                $v$jscomp$2$$ = $k$jscomp$8$$ + $r$jscomp$4$$;
                for ($q$jscomp$4$$ = $v$jscomp$2$$ >>> 2; $a$jscomp$33$$.length <= $q$jscomp$4$$;) {
                  $a$jscomp$33$$.push(0);
                }
                $a$jscomp$33$$[$q$jscomp$4$$] |= $e$jscomp$31$$ << 8 * ($p$jscomp$6$$ + $v$jscomp$2$$ % 4 * $h$jscomp$24$$);
                $k$jscomp$8$$ += 2;
              }
            }
          }
          return {value:$a$jscomp$33$$, $binLen$:8 * $k$jscomp$8$$ + $d$jscomp$24$$};
        };
        break;
      case "B64":
        $d$jscomp$22$$ = function($c$jscomp$29$$, $a$jscomp$34$$, $b$jscomp$27$$) {
          var $e$jscomp$32$$ = 0, $f$jscomp$18$$, $n$jscomp$9$$;
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
          var $u$jscomp$3$$ = -1 === $h$jscomp$24$$ ? 3 : 0;
          for ($g$jscomp$14$$ = 0; $g$jscomp$14$$ < $c$jscomp$29$$.length; $g$jscomp$14$$ += 4) {
            var $r$jscomp$5$$ = $c$jscomp$29$$.substr($g$jscomp$14$$, 4);
            for ($f$jscomp$18$$ = $n$jscomp$9$$ = 0; $f$jscomp$18$$ < $r$jscomp$5$$.length; $f$jscomp$18$$ += 1) {
              var $d$jscomp$25$$ = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".indexOf($r$jscomp$5$$[$f$jscomp$18$$]);
              $n$jscomp$9$$ |= $d$jscomp$25$$ << 18 - 6 * $f$jscomp$18$$;
            }
            for ($f$jscomp$18$$ = 0; $f$jscomp$18$$ < $r$jscomp$5$$.length - 1; $f$jscomp$18$$ += 1) {
              var $p$jscomp$7$$ = $e$jscomp$32$$ + $q$jscomp$5$$;
              for ($d$jscomp$25$$ = $p$jscomp$7$$ >>> 2; $a$jscomp$34$$.length <= $d$jscomp$25$$;) {
                $a$jscomp$34$$.push(0);
              }
              $a$jscomp$34$$[$d$jscomp$25$$] |= ($n$jscomp$9$$ >>> 16 - 8 * $f$jscomp$18$$ & 255) << 8 * ($u$jscomp$3$$ + $p$jscomp$7$$ % 4 * $h$jscomp$24$$);
              $e$jscomp$32$$ += 1;
            }
          }
          return {value:$a$jscomp$34$$, $binLen$:8 * $e$jscomp$32$$ + $b$jscomp$27$$};
        };
        break;
      case "BYTES":
        $d$jscomp$22$$ = function($c$jscomp$30$$, $a$jscomp$35$$, $b$jscomp$28$$) {
          var $d$jscomp$26$$;
          $a$jscomp$35$$ = $a$jscomp$35$$ || [0];
          $b$jscomp$28$$ = $b$jscomp$28$$ || 0;
          var $g$jscomp$15$$ = $b$jscomp$28$$ >>> 3;
          var $p$jscomp$8$$ = -1 === $h$jscomp$24$$ ? 3 : 0;
          for ($d$jscomp$26$$ = 0; $d$jscomp$26$$ < $c$jscomp$30$$.length; $d$jscomp$26$$ += 1) {
            var $e$jscomp$33$$ = $c$jscomp$30$$.charCodeAt($d$jscomp$26$$);
            var $n$jscomp$10$$ = $d$jscomp$26$$ + $g$jscomp$15$$;
            var $f$jscomp$19$$ = $n$jscomp$10$$ >>> 2;
            $a$jscomp$35$$.length <= $f$jscomp$19$$ && $a$jscomp$35$$.push(0);
            $a$jscomp$35$$[$f$jscomp$19$$] |= $e$jscomp$33$$ << 8 * ($p$jscomp$8$$ + $n$jscomp$10$$ % 4 * $h$jscomp$24$$);
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
          var $n$jscomp$11$$ = -1 === $h$jscomp$24$$ ? 3 : 0;
          var $p$jscomp$9$$ = new Uint8Array($c$jscomp$32$$);
          for ($d$jscomp$27$$ = 0; $d$jscomp$27$$ < $c$jscomp$32$$.byteLength; $d$jscomp$27$$ += 1) {
            var $f$jscomp$20$$ = $d$jscomp$27$$ + $g$jscomp$16$$;
            var $k$jscomp$9$$ = $f$jscomp$20$$ >>> 2;
            $a$jscomp$36$$.length <= $k$jscomp$9$$ && $a$jscomp$36$$.push(0);
            $a$jscomp$36$$[$k$jscomp$9$$] |= $p$jscomp$9$$[$d$jscomp$27$$] << 8 * ($n$jscomp$11$$ + $f$jscomp$20$$ % 4 * $h$jscomp$24$$);
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
  function $B$jscomp$1$$($d$jscomp$31_h$jscomp$25$$, $b$jscomp$33$$) {
    var $c$jscomp$33$$, $e$jscomp$34$$ = [], $m$jscomp$8$$ = [];
    if (null !== $d$jscomp$31_h$jscomp$25$$) {
      for ($c$jscomp$33$$ = 0; $c$jscomp$33$$ < $d$jscomp$31_h$jscomp$25$$.length; $c$jscomp$33$$ += 2) {
        $b$jscomp$33$$[($c$jscomp$33$$ >>> 1) % 5][($c$jscomp$33$$ >>> 1) / 5 | 0] = $p$jscomp$3$$($b$jscomp$33$$[($c$jscomp$33$$ >>> 1) % 5][($c$jscomp$33$$ >>> 1) / 5 | 0], new $g$jscomp$8$$($d$jscomp$31_h$jscomp$25$$[$c$jscomp$33$$ + 1], $d$jscomp$31_h$jscomp$25$$[$c$jscomp$33$$]));
      }
    }
    for ($d$jscomp$31_h$jscomp$25$$ = 0; 24 > $d$jscomp$31_h$jscomp$25$$; $d$jscomp$31_h$jscomp$25$$ += 1) {
      var $l$jscomp$14$$ = $y$jscomp$77$$("SHA3-");
      for ($c$jscomp$33$$ = 0; 5 > $c$jscomp$33$$; $c$jscomp$33$$ += 1) {
        var $a$jscomp$37$$ = $b$jscomp$33$$[$c$jscomp$33$$][0];
        var $k$jscomp$10$$ = $b$jscomp$33$$[$c$jscomp$33$$][1], $f$jscomp$21$$ = $b$jscomp$33$$[$c$jscomp$33$$][2], $n$jscomp$12$$ = $b$jscomp$33$$[$c$jscomp$33$$][3], $r$jscomp$6$$ = $b$jscomp$33$$[$c$jscomp$33$$][4];
        $e$jscomp$34$$[$c$jscomp$33$$] = new $g$jscomp$8$$($a$jscomp$37$$.a ^ $k$jscomp$10$$.a ^ $f$jscomp$21$$.a ^ $n$jscomp$12$$.a ^ $r$jscomp$6$$.a, $a$jscomp$37$$.b ^ $k$jscomp$10$$.b ^ $f$jscomp$21$$.b ^ $n$jscomp$12$$.b ^ $r$jscomp$6$$.b);
      }
      for ($c$jscomp$33$$ = 0; 5 > $c$jscomp$33$$; $c$jscomp$33$$ += 1) {
        $m$jscomp$8$$[$c$jscomp$33$$] = $p$jscomp$3$$($e$jscomp$34$$[($c$jscomp$33$$ + 4) % 5], $z$jscomp$19$$($e$jscomp$34$$[($c$jscomp$33$$ + 1) % 5], 1));
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
      $b$jscomp$33$$[0][0] = $p$jscomp$3$$($b$jscomp$33$$[0][0], $K$jscomp$1$$[$d$jscomp$31_h$jscomp$25$$]);
    }
    return $b$jscomp$33$$;
  }
  var $K$jscomp$1$$ = [new $g$jscomp$8$$(0, 1), new $g$jscomp$8$$(0, 32898), new $g$jscomp$8$$(2147483648, 32906), new $g$jscomp$8$$(2147483648, 2147516416), new $g$jscomp$8$$(0, 32907), new $g$jscomp$8$$(0, 2147483649), new $g$jscomp$8$$(2147483648, 2147516545), new $g$jscomp$8$$(2147483648, 32777), new $g$jscomp$8$$(0, 138), new $g$jscomp$8$$(0, 136), new $g$jscomp$8$$(0, 2147516425), new $g$jscomp$8$$(0, 2147483658), new $g$jscomp$8$$(0, 2147516555), new $g$jscomp$8$$(2147483648, 139), new $g$jscomp$8$$(2147483648, 
  32905), new $g$jscomp$8$$(2147483648, 32771), new $g$jscomp$8$$(2147483648, 32770), new $g$jscomp$8$$(2147483648, 128), new $g$jscomp$8$$(0, 32778), new $g$jscomp$8$$(2147483648, 2147483658), new $g$jscomp$8$$(2147483648, 2147516545), new $g$jscomp$8$$(2147483648, 32896), new $g$jscomp$8$$(0, 2147483649), new $g$jscomp$8$$(2147483648, 2147516424)];
  var $J$jscomp$1$$ = [[0, 36, 3, 41, 18], [1, 44, 10, 45, 2], [62, 6, 43, 15, 61], [28, 55, 25, 21, 56], [27, 20, 39, 8, 14]];
  "function" === typeof define && define.$amd$ ? define(function() {
    return $u$jscomp$1$$;
  }) : "undefined" !== typeof exports ? ("undefined" !== typeof module && module.exports && (module.exports = $u$jscomp$1$$), exports = $u$jscomp$1$$) : $L$jscomp$1$$.$B$ = $u$jscomp$1$$;
})(this);
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.data = $stendhal$$.data || {};
$stendhal$$.data.$sprites$ = {get:function($filename$jscomp$3$$) {
  if (!$filename$jscomp$3$$) {
    return {};
  }
  if ("undefined" != typeof this[$filename$jscomp$3$$]) {
    return this[$filename$jscomp$3$$].counter++, this[$filename$jscomp$3$$];
  }
  var $temp$jscomp$1$$ = new Image();
  $temp$jscomp$1$$.counter = 0;
  $temp$jscomp$1$$.src = $filename$jscomp$3$$;
  return this[$filename$jscomp$3$$] = $temp$jscomp$1$$;
}, $getWithPromise$:function($filename$jscomp$4$$) {
  var $$jscomp$this$$ = this;
  return new Promise(function($resolve$jscomp$4$$) {
    "undefined" != typeof $$jscomp$this$$[$filename$jscomp$4$$] && ($$jscomp$this$$[$filename$jscomp$4$$].counter++, $resolve$jscomp$4$$($$jscomp$this$$[$filename$jscomp$4$$]));
    var $image$jscomp$4$$ = new Image();
    $image$jscomp$4$$.counter = 0;
    $$jscomp$this$$[$filename$jscomp$4$$] = $image$jscomp$4$$;
    $image$jscomp$4$$.onload = function() {
      return $resolve$jscomp$4$$($image$jscomp$4$$);
    };
    $image$jscomp$4$$.src = $filename$jscomp$4$$;
  });
}, $clean$:function() {
  for (var $i$jscomp$51$$ in this) {
    console.log(typeof $i$jscomp$51$$), "Image" == typeof $i$jscomp$51$$ && (0 < this[$i$jscomp$51$$].counter ? this[$i$jscomp$51$$].counter = 0 : delete this[$i$jscomp$51$$]);
  }
}, $getAreaOf$:function($image$jscomp$5_newImage$$, $width$jscomp$28$$, $height$jscomp$25$$, $offsetX$$, $offsetY$$) {
  $offsetX$$ = $offsetX$$ || 0;
  $offsetY$$ = $offsetY$$ || 0;
  if ($image$jscomp$5_newImage$$.width === $width$jscomp$28$$ && $image$jscomp$5_newImage$$.height === $height$jscomp$25$$ && 0 === $offsetX$$ && 0 === $offsetY$$) {
    return $image$jscomp$5_newImage$$;
  }
  var $canvas$$ = document.createElement("canvas");
  $canvas$$.width = $width$jscomp$28$$;
  $canvas$$.height = $height$jscomp$25$$;
  $canvas$$.getContext("2d").drawImage($image$jscomp$5_newImage$$, $offsetX$$, $offsetY$$, $width$jscomp$28$$, $height$jscomp$25$$, 0, 0, $width$jscomp$28$$, $height$jscomp$25$$);
  $image$jscomp$5_newImage$$ = new Image();
  $image$jscomp$5_newImage$$.src = $canvas$$.toDataURL("image/png");
  return $image$jscomp$5_newImage$$;
}, $getFiltered$:function($fileName_filteredName$$, $canvas$jscomp$1_filter$jscomp$3_filtered$$, $param$jscomp$6$$) {
  var $img$jscomp$2_imgData$$ = this.get($fileName_filteredName$$);
  if (!$img$jscomp$2_imgData$$) {
    return null;
  }
  var $filterFn$$;
  if ("undefined" === typeof $canvas$jscomp$1_filter$jscomp$3_filtered$$ || !($filterFn$$ = $stendhal$$.data.$sprites$.filter[$canvas$jscomp$1_filter$jscomp$3_filtered$$]) || 0 === $img$jscomp$2_imgData$$.width || 0 === $img$jscomp$2_imgData$$.height) {
    return $img$jscomp$2_imgData$$;
  }
  $fileName_filteredName$$ = $fileName_filteredName$$ + " " + $canvas$jscomp$1_filter$jscomp$3_filtered$$ + " " + $param$jscomp$6$$;
  $canvas$jscomp$1_filter$jscomp$3_filtered$$ = this[$fileName_filteredName$$];
  if ("undefined" === typeof $canvas$jscomp$1_filter$jscomp$3_filtered$$) {
    $canvas$jscomp$1_filter$jscomp$3_filtered$$ = document.createElement("canvas");
    $canvas$jscomp$1_filter$jscomp$3_filtered$$.width = $img$jscomp$2_imgData$$.width;
    $canvas$jscomp$1_filter$jscomp$3_filtered$$.height = $img$jscomp$2_imgData$$.height;
    var $ctx$jscomp$1$$ = $canvas$jscomp$1_filter$jscomp$3_filtered$$.getContext("2d");
    $ctx$jscomp$1$$.drawImage($img$jscomp$2_imgData$$, 0, 0);
    $img$jscomp$2_imgData$$ = $ctx$jscomp$1$$.getImageData(0, 0, $img$jscomp$2_imgData$$.width, $img$jscomp$2_imgData$$.height);
    $filterFn$$($img$jscomp$2_imgData$$.data, $param$jscomp$6$$);
    $ctx$jscomp$1$$.putImageData($img$jscomp$2_imgData$$, 0, 0);
    this[$fileName_filteredName$$] = $canvas$jscomp$1_filter$jscomp$3_filtered$$;
  }
  return $canvas$jscomp$1_filter$jscomp$3_filtered$$;
}, $getFilteredWithPromise$:function($fileName$jscomp$1$$, $filter$jscomp$4$$, $param$jscomp$7$$) {
  return this.$getWithPromise$($fileName$jscomp$1$$).then(function($img$jscomp$3_imgData$jscomp$1$$) {
    var $filterFn$jscomp$1$$;
    if ("undefined" === typeof $filter$jscomp$4$$ || !($filterFn$jscomp$1$$ = $stendhal$$.data.$sprites$.filter[$filter$jscomp$4$$]) || 0 === $img$jscomp$3_imgData$jscomp$1$$.width || 0 === $img$jscomp$3_imgData$jscomp$1$$.height) {
      return $img$jscomp$3_imgData$jscomp$1$$;
    }
    var $filteredName$jscomp$1$$ = $fileName$jscomp$1$$ + " " + $filter$jscomp$4$$ + " " + $param$jscomp$7$$, $canvas$jscomp$2_filtered$jscomp$1$$ = this[$filteredName$jscomp$1$$];
    if ("undefined" === typeof $canvas$jscomp$2_filtered$jscomp$1$$) {
      $canvas$jscomp$2_filtered$jscomp$1$$ = document.createElement("canvas");
      $canvas$jscomp$2_filtered$jscomp$1$$.width = $img$jscomp$3_imgData$jscomp$1$$.width;
      $canvas$jscomp$2_filtered$jscomp$1$$.height = $img$jscomp$3_imgData$jscomp$1$$.height;
      var $ctx$jscomp$2$$ = $canvas$jscomp$2_filtered$jscomp$1$$.getContext("2d");
      $ctx$jscomp$2$$.drawImage($img$jscomp$3_imgData$jscomp$1$$, 0, 0);
      $img$jscomp$3_imgData$jscomp$1$$ = $ctx$jscomp$2$$.getImageData(0, 0, $img$jscomp$3_imgData$jscomp$1$$.width, $img$jscomp$3_imgData$jscomp$1$$.height);
      $filterFn$jscomp$1$$($img$jscomp$3_imgData$jscomp$1$$.data, $param$jscomp$7$$);
      $ctx$jscomp$2$$.putImageData($img$jscomp$3_imgData$jscomp$1$$, 0, 0);
      this[$filteredName$jscomp$1$$] = $canvas$jscomp$2_filtered$jscomp$1$$;
    }
    return $canvas$jscomp$2_filtered$jscomp$1$$;
  });
}, filter:{$splitrgb$:function($rgb$$) {
  $rgb$$ &= 16777215;
  var $b$jscomp$34$$ = $rgb$$ & 255;
  $rgb$$ >>>= 8;
  return [$rgb$$ >>> 8, $rgb$$ & 255, $b$jscomp$34$$];
}, $mergergb$:function($rgbArray$$) {
  return 16777215 & ($rgbArray$$[0] << 16 | $rgbArray$$[1] << 8 | $rgbArray$$[2]);
}, $rgb2hsl$:function($l$jscomp$15_rgb$jscomp$1$$) {
  var $h$jscomp$26_r$jscomp$8$$ = $l$jscomp$15_rgb$jscomp$1$$[0] / 255, $g$jscomp$19$$ = $l$jscomp$15_rgb$jscomp$1$$[1] / 255, $b$jscomp$35$$ = $l$jscomp$15_rgb$jscomp$1$$[2] / 255;
  if ($h$jscomp$26_r$jscomp$8$$ > $g$jscomp$19$$) {
    var $max_s$jscomp$7$$ = $h$jscomp$26_r$jscomp$8$$;
    var $min$$ = $g$jscomp$19$$;
    var $maxVar$$ = 0;
  } else {
    $max_s$jscomp$7$$ = $g$jscomp$19$$, $min$$ = $h$jscomp$26_r$jscomp$8$$, $maxVar$$ = 1;
  }
  $b$jscomp$35$$ > $max_s$jscomp$7$$ ? ($max_s$jscomp$7$$ = $b$jscomp$35$$, $maxVar$$ = 2) : $b$jscomp$35$$ < $min$$ && ($min$$ = $b$jscomp$35$$);
  $l$jscomp$15_rgb$jscomp$1$$ = ($max_s$jscomp$7$$ + $min$$) / 2;
  var $diff$jscomp$2$$ = $max_s$jscomp$7$$ - $min$$;
  0.000001 > $diff$jscomp$2$$ ? $h$jscomp$26_r$jscomp$8$$ = $max_s$jscomp$7$$ = 0 : ($max_s$jscomp$7$$ = 0.5 > $l$jscomp$15_rgb$jscomp$1$$ ? $diff$jscomp$2$$ / ($max_s$jscomp$7$$ + $min$$) : $diff$jscomp$2$$ / (2 - $max_s$jscomp$7$$ - $min$$), $h$jscomp$26_r$jscomp$8$$ = (0 === $maxVar$$ ? ($g$jscomp$19$$ - $b$jscomp$35$$) / $diff$jscomp$2$$ : 1 === $maxVar$$ ? 2 + ($b$jscomp$35$$ - $h$jscomp$26_r$jscomp$8$$) / $diff$jscomp$2$$ : 4 + ($h$jscomp$26_r$jscomp$8$$ - $g$jscomp$19$$) / $diff$jscomp$2$$) / 
  6);
  return [$h$jscomp$26_r$jscomp$8$$, $max_s$jscomp$7$$, $l$jscomp$15_rgb$jscomp$1$$];
}, $hsl2rgb$:function($g$jscomp$20_hsl_tmp1$$) {
  var $h$jscomp$27_r$jscomp$9$$ = $g$jscomp$20_hsl_tmp1$$[0];
  var $b$jscomp$36_bf_s$jscomp$8$$ = $g$jscomp$20_hsl_tmp1$$[1];
  var $gf_l$jscomp$16$$ = $g$jscomp$20_hsl_tmp1$$[2];
  if (0.0000001 > $b$jscomp$36_bf_s$jscomp$8$$) {
    $h$jscomp$27_r$jscomp$9$$ = $g$jscomp$20_hsl_tmp1$$ = $b$jscomp$36_bf_s$jscomp$8$$ = Math.floor(255 * $gf_l$jscomp$16$$);
  } else {
    $g$jscomp$20_hsl_tmp1$$ = 0.5 > $gf_l$jscomp$16$$ ? $gf_l$jscomp$16$$ * (1 + $b$jscomp$36_bf_s$jscomp$8$$) : $gf_l$jscomp$16$$ + $b$jscomp$36_bf_s$jscomp$8$$ - $gf_l$jscomp$16$$ * $b$jscomp$36_bf_s$jscomp$8$$;
    var $tmp2$$ = 2 * $gf_l$jscomp$16$$ - $g$jscomp$20_hsl_tmp1$$;
    $gf_l$jscomp$16$$ = this.$hue2rgb$(this.$limitHue$($h$jscomp$27_r$jscomp$9$$), $tmp2$$, $g$jscomp$20_hsl_tmp1$$);
    $b$jscomp$36_bf_s$jscomp$8$$ = this.$hue2rgb$(this.$limitHue$($h$jscomp$27_r$jscomp$9$$ - 1 / 3), $tmp2$$, $g$jscomp$20_hsl_tmp1$$);
    $h$jscomp$27_r$jscomp$9$$ = Math.floor(255 * this.$hue2rgb$(this.$limitHue$($h$jscomp$27_r$jscomp$9$$ + 1 / 3), $tmp2$$, $g$jscomp$20_hsl_tmp1$$)) & 255;
    $g$jscomp$20_hsl_tmp1$$ = Math.floor(255 * $gf_l$jscomp$16$$) & 255;
    $b$jscomp$36_bf_s$jscomp$8$$ = Math.floor(255 * $b$jscomp$36_bf_s$jscomp$8$$) & 255;
  }
  return [$h$jscomp$27_r$jscomp$9$$, $g$jscomp$20_hsl_tmp1$$, $b$jscomp$36_bf_s$jscomp$8$$];
}, $hue2rgb$:function($hue$$, $val1$$, $val2$$) {
  var $res$jscomp$2$$ = $hue$$;
  1 > 6 * $hue$$ ? $res$jscomp$2$$ = $val1$$ + 6 * ($val2$$ - $val1$$) * $hue$$ : 1 > 2 * $hue$$ ? $res$jscomp$2$$ = $val2$$ : 2 > 3 * $hue$$ ? $res$jscomp$2$$ = $val1$$ + ($val2$$ - $val1$$) * (2 / 3 - $hue$$) * 6 : $res$jscomp$2$$ = $val1$$;
  return $res$jscomp$2$$;
}, $limitHue$:function($hue$jscomp$1_res$jscomp$3$$) {
  0 > $hue$jscomp$1_res$jscomp$3$$ ? $hue$jscomp$1_res$jscomp$3$$ += 1 : 1 < $hue$jscomp$1_res$jscomp$3$$ && --$hue$jscomp$1_res$jscomp$3$$;
  return $hue$jscomp$1_res$jscomp$3$$;
}},};
$stendhal$$.data.$sprites$.filter.trueColor = function($data$jscomp$83$$, $color$jscomp$2_hslColor$$) {
  $color$jscomp$2_hslColor$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$($stendhal$$.data.$sprites$.filter.$splitrgb$($color$jscomp$2_hslColor$$));
  for (var $end$jscomp$10$$ = $data$jscomp$83$$.length, $i$jscomp$52$$ = 0; $i$jscomp$52$$ < $end$jscomp$10$$; $i$jscomp$52$$ += 4) {
    var $hsl$jscomp$1_resultRgb$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$([$data$jscomp$83$$[$i$jscomp$52$$], $data$jscomp$83$$[$i$jscomp$52$$ + 1], $data$jscomp$83$$[$i$jscomp$52$$ + 2]]), $tmp$jscomp$4$$ = $hsl$jscomp$1_resultRgb$$[2] - 0.5;
    $hsl$jscomp$1_resultRgb$$ = $stendhal$$.data.$sprites$.filter.$hsl2rgb$([$color$jscomp$2_hslColor$$[0], $color$jscomp$2_hslColor$$[1], $hsl$jscomp$1_resultRgb$$[2] - 2.0 * ($color$jscomp$2_hslColor$$[2] - 0.5) * ($tmp$jscomp$4$$ * $tmp$jscomp$4$$ - 0.25)]);
    $data$jscomp$83$$[$i$jscomp$52$$] = $hsl$jscomp$1_resultRgb$$[0];
    $data$jscomp$83$$[$i$jscomp$52$$ + 1] = $hsl$jscomp$1_resultRgb$$[1];
    $data$jscomp$83$$[$i$jscomp$52$$ + 2] = $hsl$jscomp$1_resultRgb$$[2];
  }
};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.entity = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$._default, {$minimapShow$:!1, $minimapStyle$:"rgb(200,255,200)", zIndex:10000, set:function($key$jscomp$51$$, $value$jscomp$101$$) {
  $marauroa$$.$rpobjectFactory$.entity.$proto$.set.apply(this, arguments);
  "name" === $key$jscomp$51$$ ? ("undefined" === typeof this.title && (this.title = $value$jscomp$101$$), this._name = $value$jscomp$101$$) : this[$key$jscomp$51$$] = -1 < ["x", "y", "height", "width"].indexOf($key$jscomp$51$$) ? parseInt($value$jscomp$101$$, 10) : $value$jscomp$101$$;
}, $isNextTo$:function($other$jscomp$11$$) {
  return $other$jscomp$11$$ && this.x && this.y && $other$jscomp$11$$.x && $other$jscomp$11$$.y && (this.x + this.width >= $other$jscomp$11$$.x && this.x <= $other$jscomp$11$$.x || $other$jscomp$11$$.x + $other$jscomp$11$$.width >= this.x && $other$jscomp$11$$.x <= this.x) ? this.y + this.height >= $other$jscomp$11$$.y && this.y <= $other$jscomp$11$$.y || $other$jscomp$11$$.y + $other$jscomp$11$$.height >= this.y && $other$jscomp$11$$.y <= this.y : !1;
}, $isVisibleToAction$:function() {
  return !1;
}, $actionAliasToAction$:function($actionAlias$$) {
  var $actionAliases$$ = {look_closely:"use", read:"look"}, $actionCommand$$ = "look";
  "string" === typeof $actionAlias$$ && ($actionAliases$$.hasOwnProperty($actionAlias$$) ? $actionCommand$$ = $actionAliases$$[$actionAlias$$] : $actionCommand$$ = $actionAlias$$);
  return $actionCommand$$;
}, $buildActions$:function($list$$) {
  if (this.menu) {
    var $pos$jscomp$1$$ = this.menu.indexOf("|");
    $list$$.push({title:this.menu.substring(0, $pos$jscomp$1$$), type:this.menu.substring($pos$jscomp$1$$ + 1).toLowerCase()});
  }
  this.action ? $list$$.push({title:$stendhal$$.$ui$.$html$.$niceName$(this.action), type:this.$actionAliasToAction$(this.action)}) : $list$$.push({title:"Look", type:"look"});
}, $updatePosition$:function() {
  this._y = this.y;
  this._x = this.x;
}, $draw$:function($ctx$jscomp$3$$) {
  this.$sprite$ && this.$drawSprite$($ctx$jscomp$3$$);
}, $drawSprite$:function($ctx$jscomp$4$$) {
  this.$drawSpriteAt$($ctx$jscomp$4$$, 32 * this.x, 32 * this.y);
}, $drawSpriteAt$:function($ctx$jscomp$5$$, $x$jscomp$96$$, $y$jscomp$78$$) {
  var $image$jscomp$6$$ = $stendhal$$.data.$sprites$.get(this.$sprite$.filename);
  if ($image$jscomp$6$$.height) {
    var $width$jscomp$29$$ = this.$sprite$.width || $image$jscomp$6$$.width, $height$jscomp$26$$ = this.$sprite$.height || $image$jscomp$6$$.height;
    $ctx$jscomp$5$$.drawImage($image$jscomp$6$$, this.$sprite$.offsetX || 0, this.$sprite$.offsetY || 0, $width$jscomp$29$$, $height$jscomp$26$$, $x$jscomp$96$$, $y$jscomp$78$$, $width$jscomp$29$$, $height$jscomp$26$$);
  }
}, $drawOutlineText$:function($ctx$jscomp$6$$, $text$jscomp$12$$, $color$jscomp$3$$, $x$jscomp$97$$, $y$jscomp$79$$) {
  $ctx$jscomp$6$$.lineWidth = 2;
  $ctx$jscomp$6$$.strokeStyle = "black";
  $ctx$jscomp$6$$.fillStyle = $color$jscomp$3$$;
  $ctx$jscomp$6$$.lineJoin = "round";
  $ctx$jscomp$6$$.strokeText($text$jscomp$12$$, $x$jscomp$97$$, $y$jscomp$79$$);
  $ctx$jscomp$6$$.fillText($text$jscomp$12$$, $x$jscomp$97$$, $y$jscomp$79$$);
}, $getIdPath$:function() {
  for (var $object$jscomp$6_slot$jscomp$3$$ = this, $res$jscomp$4$$ = ""; $object$jscomp$6_slot$jscomp$3$$;) {
    $res$jscomp$4$$ = $object$jscomp$6_slot$jscomp$3$$.id + "\t" + $res$jscomp$4$$;
    $object$jscomp$6_slot$jscomp$3$$ = $object$jscomp$6_slot$jscomp$3$$.$_parent$;
    if (!$object$jscomp$6_slot$jscomp$3$$) {
      break;
    }
    $res$jscomp$4$$ = $object$jscomp$6_slot$jscomp$3$$.$_name$ + "\t" + $res$jscomp$4$$;
    $object$jscomp$6_slot$jscomp$3$$ = $object$jscomp$6_slot$jscomp$3$$.$_parent$;
  }
  return "[" + $res$jscomp$4$$.substr(0, $res$jscomp$4$$.length - 1) + "]";
}, $say$:function($text$jscomp$13$$) {
  $marauroa$$.$me$ && $marauroa$$.$me$.$isInHearingRange$(this) && $stendhal$$.$ui$.$chatLog$.$addLine$("normal", $text$jscomp$13$$);
}, $getCursor$:function() {
  var $cursor$jscomp$1$$ = "unknown";
  this.cursor && ($cursor$jscomp$1$$ = this.cursor);
  return "url(/data/sprites/cursor/" + $cursor$jscomp$1$$.toLowerCase().replace("_", "") + ".png) 1 3, auto";
}, $getDefaultAction$:function() {
  return {type:this.$actionAliasToAction$(this.action), target:"#" + this.id, zone:$marauroa$$.$currentZoneName$};
}, $getResistance$:function() {
  return this.resistance;
}, $isObstacle$:function($entity$$) {
  return $entity$$ != this && 95 < $entity$$.$getResistance$() / 100 * this.$getResistance$();
}, onclick:function() {
  $marauroa$$.$clientFramework$.$sendAction$(this.$getDefaultAction$());
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$.unknown = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:1, $init$:function() {
  $marauroa$$.$rpobjectFactory$.unknown.$proto$.$init$.apply(this, arguments);
  var $that$$ = this;
  setTimeout(function() {
    $that$$._rpclass && console.log("Unknown entity", $that$$._rpclass, "at", $marauroa$$.$currentZoneName$, $that$$.x, $that$$.y, "is", $that$$);
  }, 1);
}, $isVisibleToAction$:function() {
  return $marauroa$$.$me$.adminlevel && 600 <= $marauroa$$.$me$.adminlevel;
}});
$marauroa$$.$rpobjectFactory$._default = $marauroa$$.$rpobjectFactory$.unknown;
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$.blood = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {$minimapShow$:!1, zIndex:2000, $init$:function() {
  this.$sprite$ = {height:32, width:32, filename:"/data/sprites/combat/blood_red.png"};
}, set:function($key$jscomp$52$$, $value$jscomp$102$$) {
  $marauroa$$.$rpobjectFactory$.blood.$proto$.set.apply(this, arguments);
  "amount" === $key$jscomp$52$$ ? this.$sprite$.offsetY = 32 * parseInt($value$jscomp$102$$, 10) : "class" === $key$jscomp$52$$ && (this.$sprite$.filename = "/data/sprites/combat/blood_" + $value$jscomp$102$$ + ".png");
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/walk.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.corpse = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {$minimapShow$:!1, zIndex:5500, $autoOpenedAlready$:!1, set:function($key$jscomp$53$$, $value$jscomp$103$$) {
  $marauroa$$.$rpobjectFactory$.corpse.$proto$.set.apply(this, arguments);
  this.$sprite$ = this.$sprite$ || {};
  $stendhal$$.$config$.$gamescreen$.$blood$ && "image" === $key$jscomp$53$$ ? this.$sprite$.filename = "/data/sprites/corpse/" + $value$jscomp$103$$ + ".png" : $stendhal$$.$config$.$gamescreen$.$blood$ || "harmless_image" !== $key$jscomp$53$$ || (this.$sprite$.$fFilename$ = "/data/sprites/corpse/" + $value$jscomp$103$$ + ".png");
}, $createSlot$:function($name$jscomp$78$$) {
  var $slot$jscomp$4$$ = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpslotFactory$._default, {add:function($object$jscomp$7$$) {
    $marauroa$$.$rpslotFactory$._default.add.apply(this, arguments);
    0 < this.$_objects$.length && this.$_parent$.$autoOpenIfDesired$();
  }, $del$:function($key$jscomp$54$$) {
    $marauroa$$.$rpslotFactory$._default.$del$.apply(this, arguments);
    0 == this.$_objects$.length && this.$_parent$.$closeCorpseInventory$();
  }});
  $slot$jscomp$4$$.$_name$ = $name$jscomp$78$$;
  $slot$jscomp$4$$.$_objects$ = [];
  $slot$jscomp$4$$.$_parent$ = this;
  return $slot$jscomp$4$$;
}, $isVisibleToAction$:function() {
  return !0;
}, $closeCorpseInventory$:function() {
  this.$inventory$ && this.$inventory$.close();
}, $openCorpseInventory$:function() {
  if (!this.$inventory$ || !this.$inventory$.$popupdiv$.parentNode) {
    var $content_row$$ = 2, $content_col$$ = 2;
    if (this.content) {
      var $content_count$$ = this.content.$_objects$.length;
      4 < $content_count$$ && ($content_row$$ = Math.ceil($content_count$$ / 2), $content_col$$ = Math.ceil($content_count$$ / $content_row$$));
    }
    this.$inventory$ = $stendhal$$.$ui$.$equip$.$createInventoryWindow$("content", $content_row$$, $content_col$$, this, "Corpse", !0);
  }
}, $autoOpenIfDesired$:function() {
  if (!this.$autoOpenedAlready$ && (this.$autoOpenedAlready$ = !0, $marauroa$$.$me$ && this.corpse_owner == $marauroa$$.$me$._name)) {
    var $that$jscomp$1$$ = this;
    window.setTimeout(function() {
      $that$jscomp$1$$.$openCorpseInventory$();
    }, 1);
  }
}, $destroy$:function() {
  this.$closeCorpseInventory$();
}, onclick:function() {
  this.$openCorpseInventory$();
}, $getCursor$:function() {
  return this.content && 0 !== this.content.$_objects$.length ? !this.corpse_owner || this.corpse_owner == $marauroa$$.$me$._name || "shared" === $stendhal$$.data.group.$lootmode$ && $stendhal$$.data.group.$members$[this.corpse_owner] ? "url(/data/sprites/cursor/bag.png) 1 3, auto" : "url(/data/sprites/cursor/lockedbag.png) 1 3, auto" : "url(/data/sprites/cursor/emptybag.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
(function() {
  var $OPEN_SPRITE$$ = {filename:"/data/sprites/chest.png", height:32, width:32, offsetY:32}, $CLOSED_SPRITE$$ = {filename:"/data/sprites/chest.png", height:32, width:32};
  $marauroa$$.$rpobjectFactory$.chest = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:5000, $sprite$:$CLOSED_SPRITE$$, open:!1, set:function($key$jscomp$55$$, $value$jscomp$104$$) {
    $marauroa$$.$rpobjectFactory$.entity.set.apply(this, arguments);
    "open" === $key$jscomp$55$$ && (this.$sprite$ = $OPEN_SPRITE$$, this.open = !0);
    this.$isNextTo$($marauroa$$.$me$) && this.$openInventoryWindow$();
  }, $unset$:function($key$jscomp$56$$) {
    $marauroa$$.$rpobjectFactory$.entity.$proto$.$unset$.call(this, $key$jscomp$56$$);
    "open" === $key$jscomp$56$$ && (this.$sprite$ = $CLOSED_SPRITE$$, this.open = !1, this.$inventory$ && this.$inventory$.$popupdiv$.parentNode && this.$inventory$.close());
  }, $isVisibleToAction$:function() {
    return !0;
  }, onclick:function() {
    $marauroa$$.$me$.$isNextTo$(this) ? $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + this.id, zone:$marauroa$$.$currentZoneName$}) : this.open && this.$openInventoryWindow$();
  }, $openInventoryWindow$:function() {
    this.$inventory$ && this.$inventory$.$popupdiv$.parentNode || (this.$inventory$ = $stendhal$$.$ui$.$equip$.$createInventoryWindow$("content", 5, 6, this, "Chest", !1));
  }, $getCursor$:function() {
    return "url(/data/sprites/cursor/bag.png) 1 3, auto";
  }});
})();
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.food = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:5000, set:function($key$jscomp$57$$, $value$jscomp$105$$) {
  $marauroa$$.$rpobjectFactory$.entity.set.apply(this, arguments);
  "amount" === $key$jscomp$57$$ && (this.$_amount$ = parseInt($value$jscomp$105$$, 10));
}, $draw$:function($ctx$jscomp$7$$) {
  var $image$jscomp$7$$ = $stendhal$$.data.$sprites$.get("/data/sprites/food.png");
  $image$jscomp$7$$.height && $ctx$jscomp$7$$.drawImage($image$jscomp$7$$, 0, 32 * this.$_amount$, 32, 32, 32 * this.x, 32 * this.y, 32, 32);
}, onclick:function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"look", target:"#" + this.id});
}, $isVisibleToAction$:function() {
  return !0;
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/look.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.gate = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:5000, set:function($key$jscomp$58$$, $value$jscomp$106$$) {
  $marauroa$$.$rpobjectFactory$.entity.set.apply(this, arguments);
  "resistance" === $key$jscomp$58$$ ? this.locked = 0 !== parseInt($value$jscomp$106$$, 10) : ("image" === $key$jscomp$58$$ || "orientation" === $key$jscomp$58$$) && delete this._image;
}, $buildActions$:function($list$jscomp$1$$) {
  var $id$jscomp$9$$ = this.id;
  $list$jscomp$1$$.push({title:this.locked ? "Open" : "Close", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + $id$jscomp$9$$, zone:$marauroa$$.$currentZoneName$,});
  }});
}, $draw$:function($ctx$jscomp$8$$) {
  void 0 == this.$_image$ && (this.$_image$ = $stendhal$$.data.$sprites$.get("/data/sprites/doors/" + this.image + "_" + this.orientation + ".png"));
  if (this.$_image$.height) {
    var $height$jscomp$27$$ = this.$_image$.height / 2;
    $ctx$jscomp$8$$.drawImage(this.$_image$, 0, this.locked ? $height$jscomp$27$$ : 0, this.$_image$.width, $height$jscomp$27$$, 32 * this._x + -32 * Math.floor(this.$_image$.width / 32 / 2), 32 * this._y + -32 * Math.floor($height$jscomp$27$$ / 32 / 2), this.$_image$.width, $height$jscomp$27$$);
  }
}, $isVisibleToAction$:function() {
  return !0;
}, onclick:function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + this.id, zone:$marauroa$$.$currentZoneName$});
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/activity.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.invisible_entity = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {$isVisibleToAction$:function() {
  return !1;
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/walk.png) 1 3, auto";
}});
$marauroa$$.$rpobjectFactory$.area = $marauroa$$.$rpobjectFactory$.invisible_entity;
$marauroa$$.$rpobjectFactory$.looped_sound_source = $marauroa$$.$rpobjectFactory$.invisible_entity;
$marauroa$$.$rpobjectFactory$.tiled_entity = $marauroa$$.$rpobjectFactory$.invisible_entity;
$marauroa$$.$rpobjectFactory$.wall = $marauroa$$.$rpobjectFactory$.invisible_entity;
$marauroa$$.$rpobjectFactory$.blocktarget = $marauroa$$.$rpobjectFactory$.invisible_entity;
$marauroa$$.$rpobjectFactory$.flyover = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.invisible_entity, {});
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$.item = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {$minimapShow$:!1, $minimapStyle$:"rgb(0,255,0)", zIndex:7000, $init$:function() {
  this.$sprite$ = {height:32, width:32};
}, $isVisibleToAction$:function() {
  return !0;
}, $getDefaultAction$:function() {
  return {type:"equip", source_path:this.$getIdPath$(), target_path:"[" + $marauroa$$.$me$.id + "\tbag]", clicked:"", zone:$marauroa$$.$currentZoneName$};
}, set:function($key$jscomp$59$$, $value$jscomp$107$$) {
  $marauroa$$.$rpobjectFactory$.item.$proto$.set.apply(this, arguments);
  if ("class" === $key$jscomp$59$$ || "subclass" === $key$jscomp$59$$) {
    this.$sprite$.filename = "/data/sprites/items/" + this["class"] + "/" + this.subclass + ".png";
  }
}, $draw$:function($ctx$jscomp$9$$) {
  this.$drawAt$($ctx$jscomp$9$$, 32 * this.x, 32 * this.y);
}, $drawAt$:function($ctx$jscomp$10$$, $x$jscomp$110$$, $y$jscomp$92$$) {
  if (this.$sprite$) {
    this.$drawSpriteAt$($ctx$jscomp$10$$, $x$jscomp$110$$, $y$jscomp$92$$);
    var $text$jscomp$14$$ = this.$formatQuantity$();
    this.$drawOutlineText$($ctx$jscomp$10$$, $text$jscomp$14$$, "white", $x$jscomp$110$$ + (32 - $ctx$jscomp$10$$.measureText($text$jscomp$14$$).width) / 2, $y$jscomp$92$$ + 6);
  }
}, $formatQuantity$:function() {
  return this.quantity && "1" !== this.quantity ? 10000000 < this.quantity ? Math.floor(this.quantity / 1000000) + "m" : 10000 < this.quantity ? Math.floor(this.quantity / 1000) + "k" : this.quantity : "";
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/itempickupfromslot.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$.portal = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {$minimapShow$:!0, $minimapStyle$:"rgb(0,0,0)", zIndex:5000, $buildActions$:function($list$jscomp$2$$) {
  $marauroa$$.$rpobjectFactory$.portal.$proto$.$buildActions$.apply(this, arguments);
  "house_portal" == this._rpclass ? ($list$jscomp$2$$.push({title:"Use", type:"use"}), $list$jscomp$2$$.push({title:"Kock", type:"knock"})) : ($list$jscomp$2$$.splice($list$jscomp$2$$.indexOf({title:"Look", type:"look"}), 1), $list$jscomp$2$$.push({title:"Use", type:"use"}));
}, $isVisibleToAction$:function() {
  return !0;
}, $getDefaultAction$:function() {
  return {type:"moveto", x:"" + this.x, y:"" + this.y, zone:$marauroa$$.$currentZoneName$};
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/portal.png) 1 3, auto";
}});
$marauroa$$.$rpobjectFactory$.house_portal = $marauroa$$.$rpobjectFactory$.portal;
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.activeEntity = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {$updatePosition$:function($movement_time$jscomp$1$$) {
  var $serverX$$ = parseFloat(this.x), $serverY$$ = parseFloat(this.y);
  void 0 == this._x && (this._x = $serverX$$);
  void 0 == this._y && (this._y = $serverY$$);
  if (0 < this.speed) {
    var $oldX$$ = this._x, $oldY$$ = this._y;
    $movement_time$jscomp$1$$ = this.speed * $movement_time$jscomp$1$$ / 300;
    switch(this.dir) {
      case "1":
        this._y -= $movement_time$jscomp$1$$;
        this._x = $serverX$$;
        break;
      case "2":
        this._x += $movement_time$jscomp$1$$;
        this._y = $serverY$$;
        break;
      case "3":
        this._y += $movement_time$jscomp$1$$;
        this._x = $serverX$$;
        break;
      case "4":
        this._x -= $movement_time$jscomp$1$$, this._y = $serverY$$;
    }
    1.75 < Math.abs(this._x - $serverX$$) && (this._x = $serverX$$);
    1.75 < Math.abs(this._y - $serverY$$) && (this._y = $serverY$$);
    if (this.$collidesMap$() || this.$collidesEntities$()) {
      this._x = $oldX$$, this._y = $oldY$$;
    }
  } else {
    this._x = $serverX$$, this._y = $serverY$$;
  }
}, $collidesMap$:function() {
  for (var $startX$$ = Math.floor(this._x), $endX$$ = Math.ceil(this._x + this.width), $endY$$ = Math.ceil(this._y + this.height), $y$jscomp$95$$ = Math.floor(this._y); $y$jscomp$95$$ < $endY$$; $y$jscomp$95$$++) {
    for (var $x$jscomp$113$$ = $startX$$; $x$jscomp$113$$ < $endX$$; $x$jscomp$113$$++) {
      if ($stendhal$$.data.map.$collision$($x$jscomp$113$$, $y$jscomp$95$$)) {
        return !0;
      }
    }
  }
  return !1;
}, $collidesEntities$:function() {
  var $thisStartX$$ = Math.floor(this._x), $thisStartY$$ = Math.floor(this._y), $thisEndX$$ = Math.ceil(this._x + this.width), $thisEndY$$ = Math.ceil(this._y + this.height), $i$jscomp$53$$;
  for ($i$jscomp$53$$ in $stendhal$$.$zone$.$entities$) {
    var $other$jscomp$12$$ = $stendhal$$.$zone$.$entities$[$i$jscomp$53$$];
    if (this.$isObstacle$($other$jscomp$12$$)) {
      var $otherStartX$$ = Math.floor($other$jscomp$12$$._x), $otherStartY$$ = Math.floor($other$jscomp$12$$._y), $otherEndY$$ = Math.ceil($other$jscomp$12$$._y + $other$jscomp$12$$.height);
      if ($thisStartX$$ < Math.ceil($other$jscomp$12$$._x + $other$jscomp$12$$.width) && $thisEndX$$ > $otherStartX$$ && $thisStartY$$ < $otherEndY$$ && $thisEndY$$ > $otherStartY$$) {
        return !0;
      }
    }
  }
  return !1;
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.game_board = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:100, $init$:function() {
  this.$sprite$ = {height:96, width:96};
}, set:function($key$jscomp$inline_47$$, $value$jscomp$inline_48$$) {
  $marauroa$$.$rpobjectFactory$.entity.set.apply(this, arguments);
  "class" === $key$jscomp$inline_47$$ && (this.$sprite$.filename = "data/sprites/gameboard/" + this["class"] + ".png");
}, $isVisibleToAction$:function() {
  return !1;
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/walk.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$HATS_NO_HAIR$ = [3, 4, 13, 992, 993, 994];
$marauroa$$.$rpobjectFactory$.rpentity = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.activeEntity, {zIndex:8000, $drawY$:0, $spritePath$:"", $titleStyle$:"#FFFFFF", $_target$:null, $attackSprite$:null, $attackResult$:null, dir:3, set:function($key$jscomp$inline_50$$, $value$jscomp$inline_51$$) {
  var $oldValue$jscomp$inline_52$$ = this[$key$jscomp$inline_50$$];
  $marauroa$$.$rpobjectFactory$.rpentity.$proto$.set.apply(this, arguments);
  "text" == $key$jscomp$inline_50$$ ? this.$say$($value$jscomp$inline_51$$) : -1 !== ["hp", "base_hp"].indexOf($key$jscomp$inline_50$$) ? (this[$key$jscomp$inline_50$$] = parseInt($value$jscomp$inline_51$$, 10), "hp" === $key$jscomp$inline_50$$ && void 0 != $oldValue$jscomp$inline_52$$ && this.$onHPChanged$(this[$key$jscomp$inline_50$$] - $oldValue$jscomp$inline_52$$)) : "target" === $key$jscomp$inline_50$$ ? (this.$_target$ && this.$_target$.$onAttackStopped$(this), (this.$_target$ = $marauroa$$.$currentZone$[$value$jscomp$inline_51$$]) && 
  this.$_target$.$onTargeted$(this)) : "away" === $key$jscomp$inline_50$$ ? this.$addFloater$("Away", "#ffff00") : "grumpy" === $key$jscomp$inline_50$$ ? this.$addFloater$("Grumpy", "#ffff00") : "xp" === $key$jscomp$inline_50$$ && void 0 != $oldValue$jscomp$inline_52$$ && this.$onXPChanged$(this[$key$jscomp$inline_50$$] - $oldValue$jscomp$inline_52$$);
}, $unset$:function($key$jscomp$inline_53$$) {
  "target" === $key$jscomp$inline_53$$ && this.$_target$ ? (this.$_target$.$onAttackStopped$(this), this.$_target$ = null) : "away" === $key$jscomp$inline_53$$ ? this.$addFloater$("Back", "#ffff00") : "grumpy" === $key$jscomp$inline_53$$ && this.$addFloater$("Receptive", "#ffff00");
  delete this[$key$jscomp$inline_53$$];
}, $isVisibleToAction$:function() {
  return "undefined" == typeof this.ghostmode || $marauroa$$.$me$ && $marauroa$$.$me$.$isAdmin$();
}, $buildActions$:function($list$jscomp$inline_54$$) {
  $marauroa$$.$rpobjectFactory$.rpentity.$proto$.$buildActions$.apply(this, arguments);
  this.menu || ($marauroa$$.$me$.$_target$ === this ? $list$jscomp$inline_54$$.push({title:"Stop attack", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"stop", zone:$marauroa$$.$currentZoneName$, attack:""});
  }}) : this !== $marauroa$$.$me$ && $list$jscomp$inline_54$$.push({title:"Attack", type:"attack"}));
  this != $marauroa$$.$me$ && $list$jscomp$inline_54$$.push({title:"Push", type:"push"});
}, $getTitle$:function() {
  return this.title;
}, $say$:function($text$jscomp$inline_55$$) {
  $marauroa$$.$me$ && $marauroa$$.$me$.$isInHearingRange$(this) && ("!me" == $text$jscomp$inline_55$$.match("^!me") ? $stendhal$$.$ui$.$chatLog$.$addLine$("emote", $text$jscomp$inline_55$$.replace(/^!me/, this.$getTitle$())) : (this.$addSpeechBubble$($text$jscomp$inline_55$$), $stendhal$$.$ui$.$chatLog$.$addLine$("normal", this.$getTitle$() + ": " + $text$jscomp$inline_55$$)));
}, $addSpeechBubble$:function($text$jscomp$inline_56$$) {
  var $x$jscomp$inline_57$$ = 32 * this._x + 32, $y$jscomp$inline_58$$ = 32 * this._y - 16;
  $stendhal$$.$ui$.$gamewindow$.$addTextSprite$({$realText$:30 < $text$jscomp$inline_56$$.length ? $text$jscomp$inline_56$$.substring(0, 30) + "..." : $text$jscomp$inline_56$$, timeStamp:Date.now(), $draw$:function($ctx$jscomp$inline_59$$) {
    $ctx$jscomp$inline_59$$.lineWidth = 2;
    $ctx$jscomp$inline_59$$.font = "14px Arial";
    $ctx$jscomp$inline_59$$.fillStyle = "#ffffff";
    var $width$jscomp$inline_60$$ = $ctx$jscomp$inline_59$$.measureText(this.$realText$).width + 8;
    $ctx$jscomp$inline_59$$.strokeStyle = "#000000";
    $ctx$jscomp$inline_59$$.strokeRect($x$jscomp$inline_57$$, $y$jscomp$inline_58$$ - 15, $width$jscomp$inline_60$$, 20);
    $ctx$jscomp$inline_59$$.fillRect($x$jscomp$inline_57$$, $y$jscomp$inline_58$$ - 15, $width$jscomp$inline_60$$, 20);
    $ctx$jscomp$inline_59$$.beginPath();
    $ctx$jscomp$inline_59$$.moveTo($x$jscomp$inline_57$$, $y$jscomp$inline_58$$);
    $ctx$jscomp$inline_59$$.lineTo($x$jscomp$inline_57$$ - 5, $y$jscomp$inline_58$$ + 8);
    $ctx$jscomp$inline_59$$.lineTo($x$jscomp$inline_57$$ + 1, $y$jscomp$inline_58$$ + 5);
    $ctx$jscomp$inline_59$$.stroke();
    $ctx$jscomp$inline_59$$.closePath();
    $ctx$jscomp$inline_59$$.fill();
    $ctx$jscomp$inline_59$$.fillStyle = "#000000";
    $ctx$jscomp$inline_59$$.fillText(this.$realText$, $x$jscomp$inline_57$$ + 4, $y$jscomp$inline_58$$);
    return Date.now() > this.timeStamp + 2000 + 20 * this.$realText$.length;
  }});
}, $drawMultipartOutfit$:function($ctx$jscomp$inline_61$$) {
  var $outfit$jscomp$inline_63$$ = {};
  if ("outfit_ext" in this) {
    var $$jscomp$inline_68_layers$jscomp$inline_62$$ = "body dress head mouth eyes mask hair hat detail".split(" ");
    for (var $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$ = $$jscomp$makeIterator$$(this.outfit_ext.split(",")), $$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$ = $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$.next(); !$$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$.done; $$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$ = $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$.next()) {
      $$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$ = $$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$.value, $$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$.includes("=") && ($$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$ = $$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$.split("="), $outfit$jscomp$inline_63$$[$$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$[0]] = $$jscomp$inline_65_part$jscomp$inline_66_tmp$jscomp$inline_67$$[1]);
    }
  } else {
    $$jscomp$inline_68_layers$jscomp$inline_62$$ = ["body", "dress", "head", "hair", "detail"], $outfit$jscomp$inline_63$$.body = this.outfit % 100, $outfit$jscomp$inline_63$$.dress = Math.floor(this.outfit / 100) % 100, $outfit$jscomp$inline_63$$.head = Math.floor(this.outfit / 10000) % 100, $outfit$jscomp$inline_63$$.hair = Math.floor(this.outfit / 1000000) % 100, $outfit$jscomp$inline_63$$.detail = Math.floor(this.outfit / 100000000) % 100;
  }
  $$jscomp$inline_68_layers$jscomp$inline_62$$ = $$jscomp$makeIterator$$($$jscomp$inline_68_layers$jscomp$inline_62$$);
  for ($$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$ = $$jscomp$inline_68_layers$jscomp$inline_62$$.next(); !$$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$.done; $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$ = $$jscomp$inline_68_layers$jscomp$inline_62$$.next()) {
    $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$ = $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$.value, ("hair" != $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$ || !$stendhal$$.$HATS_NO_HAIR$.includes(parseInt($outfit$jscomp$inline_63$$.hat))) && $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$ in $outfit$jscomp$inline_63$$ && ($$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$ = 
    this.$getOutfitPart$($$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$, $outfit$jscomp$inline_63$$[$$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$])) && this.$drawSprite$($ctx$jscomp$inline_61$$, $$jscomp$inline_64_$jscomp$inline_69_img$jscomp$inline_71_layer$jscomp$inline_70$$);
  }
}, $getOutfitPart$:function($colorname$jscomp$inline_77_part$jscomp$inline_72$$, $filename$jscomp$inline_75_index$jscomp$inline_73$$) {
  var $colors$jscomp$inline_76_n$jscomp$inline_74$$ = $filename$jscomp$inline_75_index$jscomp$inline_73$$;
  10 > $filename$jscomp$inline_75_index$jscomp$inline_73$$ ? $colors$jscomp$inline_76_n$jscomp$inline_74$$ = "00" + $filename$jscomp$inline_75_index$jscomp$inline_73$$ : 100 > $filename$jscomp$inline_75_index$jscomp$inline_73$$ && ($colors$jscomp$inline_76_n$jscomp$inline_74$$ = "0" + $filename$jscomp$inline_75_index$jscomp$inline_73$$);
  $filename$jscomp$inline_75_index$jscomp$inline_73$$ = "/data/sprites/outfit/" + $colorname$jscomp$inline_77_part$jscomp$inline_72$$ + "/" + $colorname$jscomp$inline_77_part$jscomp$inline_72$$ + "_" + $colors$jscomp$inline_76_n$jscomp$inline_74$$ + ".png";
  $colors$jscomp$inline_76_n$jscomp$inline_74$$ = this.outfit_colors;
  $colorname$jscomp$inline_77_part$jscomp$inline_72$$ = "body" === $colorname$jscomp$inline_77_part$jscomp$inline_72$$ || "head" === $colorname$jscomp$inline_77_part$jscomp$inline_72$$ ? "skin" : $colorname$jscomp$inline_77_part$jscomp$inline_72$$;
  return "undefined" !== typeof $colors$jscomp$inline_76_n$jscomp$inline_74$$ && "undefined" !== typeof $colors$jscomp$inline_76_n$jscomp$inline_74$$[$colorname$jscomp$inline_77_part$jscomp$inline_72$$] ? $stendhal$$.data.$sprites$.$getFiltered$($filename$jscomp$inline_75_index$jscomp$inline_73$$, "trueColor", $colors$jscomp$inline_76_n$jscomp$inline_74$$[$colorname$jscomp$inline_77_part$jscomp$inline_72$$]) : $stendhal$$.data.$sprites$.get($filename$jscomp$inline_75_index$jscomp$inline_73$$);
}, $draw$:function($ctx$jscomp$inline_78$$) {
  if ("undefined" == typeof this.ghostmode || !$marauroa$$.$me$ || $marauroa$$.$me$.$isAdmin$()) {
    this.$drawCombat$($ctx$jscomp$inline_78$$);
    if ("undefined" != typeof this.outfit || "undefined" != typeof this.outfit_ext) {
      this.$drawMultipartOutfit$($ctx$jscomp$inline_78$$);
    } else {
      var $filename$jscomp$inline_79_image$jscomp$inline_80$$ = "/data/sprites/" + this.$spritePath$ + "/" + this["class"];
      "undefined" != typeof this.subclass && ($filename$jscomp$inline_79_image$jscomp$inline_80$$ = $filename$jscomp$inline_79_image$jscomp$inline_80$$ + "/" + this.subclass);
      $filename$jscomp$inline_79_image$jscomp$inline_80$$ = $stendhal$$.data.$sprites$.get($filename$jscomp$inline_79_image$jscomp$inline_80$$ + ".png");
      this.$drawSprite$($ctx$jscomp$inline_78$$, $filename$jscomp$inline_79_image$jscomp$inline_80$$);
    }
    this.$drawAttack$($ctx$jscomp$inline_78$$);
    this.$drawFloaters$($ctx$jscomp$inline_78$$);
    this.$drawStatusIcons$($ctx$jscomp$inline_78$$);
  }
}, $drawStatusIcons$:function($ctx$jscomp$inline_81$$) {
  function $drawAnimatedIcon$jscomp$inline_83$$($icon$jscomp$inline_98_iconPath$jscomp$inline_94$$, $delay$jscomp$inline_95$$, $x$jscomp$inline_96$$, $y$jscomp$inline_97$$) {
    $icon$jscomp$inline_98_iconPath$jscomp$inline_94$$ = $stendhal$$.data.$sprites$.get($icon$jscomp$inline_98_iconPath$jscomp$inline_94$$);
    var $dim$jscomp$inline_99$$ = $icon$jscomp$inline_98_iconPath$jscomp$inline_94$$.height;
    $ctx$jscomp$inline_81$$.drawImage($icon$jscomp$inline_98_iconPath$jscomp$inline_94$$, Math.floor(Date.now() / $delay$jscomp$inline_95$$) % ($icon$jscomp$inline_98_iconPath$jscomp$inline_94$$.width / $dim$jscomp$inline_99$$) * $dim$jscomp$inline_99$$, 0, $dim$jscomp$inline_99$$, $dim$jscomp$inline_99$$, $x$jscomp$inline_96$$, $y$jscomp$inline_97$$, $dim$jscomp$inline_99$$, $dim$jscomp$inline_99$$);
  }
  function $drawAnimatedIconWithFrames$jscomp$inline_84$$($icon$jscomp$inline_105_iconPath$jscomp$inline_100$$, $nFrames$jscomp$inline_101$$, $delay$jscomp$inline_102$$, $x$jscomp$inline_103$$, $y$jscomp$inline_104$$) {
    $icon$jscomp$inline_105_iconPath$jscomp$inline_100$$ = $stendhal$$.data.$sprites$.get($icon$jscomp$inline_105_iconPath$jscomp$inline_100$$);
    var $xdim$jscomp$inline_209$$ = $icon$jscomp$inline_105_iconPath$jscomp$inline_100$$.width / $nFrames$jscomp$inline_101$$, $ydim$jscomp$inline_210$$ = $icon$jscomp$inline_105_iconPath$jscomp$inline_100$$.height;
    $ctx$jscomp$inline_81$$.drawImage($icon$jscomp$inline_105_iconPath$jscomp$inline_100$$, Math.floor(Date.now() / $delay$jscomp$inline_102$$) % $nFrames$jscomp$inline_101$$ * $xdim$jscomp$inline_209$$, 0, $xdim$jscomp$inline_209$$, $ydim$jscomp$inline_210$$, $x$jscomp$inline_103$$, $y$jscomp$inline_104$$, $xdim$jscomp$inline_209$$, $ydim$jscomp$inline_210$$);
  }
  var $x$jscomp$inline_85$$ = 32 * this._x - 10, $y$jscomp$inline_86$$ = 32 * (this._y + 1);
  this.hasOwnProperty("choking") ? $ctx$jscomp$inline_81$$.drawImage($stendhal$$.data.$sprites$.get("/data/sprites/ideas/choking.png"), $x$jscomp$inline_85$$, $y$jscomp$inline_86$$ - 10) : this.hasOwnProperty("eating") && $ctx$jscomp$inline_81$$.drawImage($stendhal$$.data.$sprites$.get("/data/sprites/ideas/eat.png"), $x$jscomp$inline_85$$, $y$jscomp$inline_86$$ - 10);
  this.hasOwnProperty("idea") && $ctx$jscomp$inline_81$$.drawImage($stendhal$$.data.$sprites$.get("/data/sprites/ideas/" + this.idea + ".png"), $x$jscomp$inline_85$$ + 32 * this.width, $y$jscomp$inline_86$$ - this.drawHeight);
  this.hasOwnProperty("away") && $drawAnimatedIcon$jscomp$inline_83$$("/data/sprites/ideas/away.png", 1500, $x$jscomp$inline_85$$ + 32 * this.width, $y$jscomp$inline_86$$ - this.drawHeight);
  this.hasOwnProperty("grumpy") && $drawAnimatedIcon$jscomp$inline_83$$("/data/sprites/ideas/grumpy.png", 1000, $x$jscomp$inline_85$$ + 5, $y$jscomp$inline_86$$ - this.drawHeight);
  this.hasOwnProperty("last_player_kill_time") && $drawAnimatedIconWithFrames$jscomp$inline_84$$("/data/sprites/ideas/pk.png", 12, 300, $x$jscomp$inline_85$$, $y$jscomp$inline_86$$ - this.drawHeight);
  this.hasOwnProperty("poisoned") && $drawAnimatedIcon$jscomp$inline_83$$("/data/sprites/status/poison.png", 100, $x$jscomp$inline_85$$ + 32 * this.width - 10, $y$jscomp$inline_86$$ - this.drawHeight);
  this.hasOwnProperty("job_healer") && $ctx$jscomp$inline_81$$.drawImage($stendhal$$.data.$sprites$.get("/data/sprites/status/healer.png"), $x$jscomp$inline_85$$, $y$jscomp$inline_86$$ - 10);
  this.hasOwnProperty("job_merchant") && $ctx$jscomp$inline_81$$.drawImage($stendhal$$.data.$sprites$.get("/data/sprites/status/merchant.png"), $x$jscomp$inline_85$$ + 12, $y$jscomp$inline_86$$ - 10);
}, $drawCombat$:function($ctx$jscomp$inline_106$$) {
  if (this.$attackers$ && 0 < this.$attackers$.size) {
    if ($ctx$jscomp$inline_106$$.lineWidth = 1, $ctx$jscomp$inline_106$$.ellipse instanceof Function) {
      var $xRad$jscomp$inline_107$$ = 16 * this.width, $yRad$jscomp$inline_108$$ = 16 * this.height / Math.SQRT2, $centerX$jscomp$inline_109$$ = 32 * this._x + $xRad$jscomp$inline_107$$, $centerY$jscomp$inline_110$$ = 32 * (this._y + this.height) - $yRad$jscomp$inline_108$$;
      $ctx$jscomp$inline_106$$.strokeStyle = "#4a0000";
      $ctx$jscomp$inline_106$$.beginPath();
      $ctx$jscomp$inline_106$$.ellipse($centerX$jscomp$inline_109$$, $centerY$jscomp$inline_110$$, $xRad$jscomp$inline_107$$, $yRad$jscomp$inline_108$$, 0, Math.PI, !1);
      $ctx$jscomp$inline_106$$.stroke();
      $ctx$jscomp$inline_106$$.strokeStyle = "#e60a0a";
      $ctx$jscomp$inline_106$$.beginPath();
      $ctx$jscomp$inline_106$$.ellipse($centerX$jscomp$inline_109$$, $centerY$jscomp$inline_110$$, $xRad$jscomp$inline_107$$, $yRad$jscomp$inline_108$$, Math.PI, 2 * Math.PI, !1);
      $ctx$jscomp$inline_106$$.stroke();
    } else {
      $ctx$jscomp$inline_106$$.strokeStyle = "#e60a0a", $ctx$jscomp$inline_106$$.strokeRect(32 * this._x, 32 * this._y, 32 * this.width, 32 * this.height);
    }
  }
  this.$getAttackTarget$() === $marauroa$$.$me$ && ($ctx$jscomp$inline_106$$.lineWidth = 1, $ctx$jscomp$inline_106$$.ellipse instanceof Function ? ($xRad$jscomp$inline_107$$ = 16 * this.width - 1, $yRad$jscomp$inline_108$$ = 16 * this.height / Math.SQRT2 - 1, $centerX$jscomp$inline_109$$ = 32 * this._x + $xRad$jscomp$inline_107$$ + 1, $centerY$jscomp$inline_110$$ = 32 * (this._y + this.height) - $yRad$jscomp$inline_108$$ - 1, $ctx$jscomp$inline_106$$.strokeStyle = "#ffc800", $ctx$jscomp$inline_106$$.beginPath(), 
  $ctx$jscomp$inline_106$$.ellipse($centerX$jscomp$inline_109$$, $centerY$jscomp$inline_110$$, $xRad$jscomp$inline_107$$, $yRad$jscomp$inline_108$$, 0, Math.PI, !1), $ctx$jscomp$inline_106$$.stroke(), $ctx$jscomp$inline_106$$.strokeStyle = "#ffdd0a", $ctx$jscomp$inline_106$$.beginPath(), $ctx$jscomp$inline_106$$.ellipse($centerX$jscomp$inline_109$$, $centerY$jscomp$inline_110$$, $xRad$jscomp$inline_107$$, $yRad$jscomp$inline_108$$, Math.PI, 2 * Math.PI, !1), $ctx$jscomp$inline_106$$.stroke()) : ($ctx$jscomp$inline_106$$.strokeStyle = 
  "#ffdd0a", $ctx$jscomp$inline_106$$.strokeRect(32 * this._x + 1, 32 * this._y + 1, 32 * this.width - 2, 32 * this.height - 2)));
  this.$attackResult$ && this.$attackResult$.$draw$($ctx$jscomp$inline_106$$, 32 * (this._x + this.width) - 10, 32 * (this._y + this.height) - 10) && (this.$attackResult$ = null);
}, $drawFloaters$:function($ctx$jscomp$inline_111$$) {
  if (this.hasOwnProperty("floaters")) {
    for (var $centerX$jscomp$inline_112$$ = 32 * (this._x + this.width / 2), $topY$jscomp$inline_113$$ = 32 * (this._y + 1) - this.drawHeight, $currentFloaters$jscomp$inline_114$$ = this.$floaters$, $i$jscomp$inline_115$$ = 0; $i$jscomp$inline_115$$ < $currentFloaters$jscomp$inline_114$$.length; $i$jscomp$inline_115$$++) {
      var $floater$jscomp$inline_116$$ = $currentFloaters$jscomp$inline_114$$[$i$jscomp$inline_115$$];
      $floater$jscomp$inline_116$$.$draw$($ctx$jscomp$inline_111$$, $centerX$jscomp$inline_112$$, $topY$jscomp$inline_113$$) && (this.$floaters$ = this.$floaters$.slice(), this.$floaters$.splice(this.$floaters$.indexOf($floater$jscomp$inline_116$$), 1));
    }
  }
}, $drawSprite$:function($ctx$jscomp$inline_117$$, $image$jscomp$inline_118$$) {
  var $localX$jscomp$inline_119$$ = 32 * this._x, $localY$jscomp$inline_120$$ = 32 * this._y;
  if ($image$jscomp$inline_118$$.height) {
    var $nFrames$jscomp$inline_121$$ = 3, $drawX$jscomp$inline_125_nDirections$jscomp$inline_122$$ = 4, $yRow$jscomp$inline_123$$ = this.dir - 1, $frame$jscomp$inline_124$$ = 1;
    "ent" == this["class"] && ($nFrames$jscomp$inline_121$$ = 1, $drawX$jscomp$inline_125_nDirections$jscomp$inline_122$$ = 2, $yRow$jscomp$inline_123$$ = Math.floor((this.dir - 1) / 2), $frame$jscomp$inline_124$$ = 0);
    this.drawHeight = $image$jscomp$inline_118$$.height / $drawX$jscomp$inline_125_nDirections$jscomp$inline_122$$;
    this.drawWidth = $image$jscomp$inline_118$$.width / $nFrames$jscomp$inline_121$$;
    $drawX$jscomp$inline_125_nDirections$jscomp$inline_122$$ = (32 * this.width - this.drawWidth) / 2;
    if (0 < this.speed && 1 != $nFrames$jscomp$inline_121$$) {
      var $animLength$jscomp$inline_126$$ = 2 * $nFrames$jscomp$inline_121$$ - 2;
      $frame$jscomp$inline_124$$ = Math.floor(Date.now() / 100) % $animLength$jscomp$inline_126$$;
      $frame$jscomp$inline_124$$ >= $nFrames$jscomp$inline_121$$ && ($frame$jscomp$inline_124$$ = $animLength$jscomp$inline_126$$ - $frame$jscomp$inline_124$$);
    }
    $ctx$jscomp$inline_117$$.drawImage($image$jscomp$inline_118$$, $frame$jscomp$inline_124$$ * this.drawWidth, $yRow$jscomp$inline_123$$ * this.drawHeight, this.drawWidth, this.drawHeight, $localX$jscomp$inline_119$$ + $drawX$jscomp$inline_125_nDirections$jscomp$inline_122$$, $localY$jscomp$inline_120$$ + (32 * this.height - this.drawHeight), this.drawWidth, this.drawHeight);
  }
}, $drawTop$:function($ctx$jscomp$inline_127$$) {
  var $localX$jscomp$inline_128$$ = 32 * this._x, $localY$jscomp$inline_129$$ = 32 * this._y;
  this.$drawHealthBar$($ctx$jscomp$inline_127$$, $localX$jscomp$inline_128$$, $localY$jscomp$inline_129$$);
  this.$drawTitle$($ctx$jscomp$inline_127$$, $localX$jscomp$inline_128$$, $localY$jscomp$inline_129$$);
}, $drawHealthBar$:function($ctx$jscomp$inline_130$$, $drawX$jscomp$inline_133_x$jscomp$inline_131$$, $drawY$jscomp$inline_134_y$jscomp$inline_132$$) {
  $drawX$jscomp$inline_133_x$jscomp$inline_131$$ += (32 * this.width - this.drawWidth) / 2;
  $drawY$jscomp$inline_134_y$jscomp$inline_132$$ = $drawY$jscomp$inline_134_y$jscomp$inline_132$$ + 32 * this.height - this.drawHeight - 6;
  $ctx$jscomp$inline_130$$.strokeStyle = "#000000";
  $ctx$jscomp$inline_130$$.lineWidth = 2;
  $ctx$jscomp$inline_130$$.beginPath();
  $ctx$jscomp$inline_130$$.rect($drawX$jscomp$inline_133_x$jscomp$inline_131$$, $drawY$jscomp$inline_134_y$jscomp$inline_132$$, this.drawWidth, 4);
  $ctx$jscomp$inline_130$$.stroke();
  $ctx$jscomp$inline_130$$.fillStyle = "#E0E0E0";
  $ctx$jscomp$inline_130$$.fillRect($drawX$jscomp$inline_133_x$jscomp$inline_131$$, $drawY$jscomp$inline_134_y$jscomp$inline_132$$, this.drawWidth, 4);
  var $hpRatio$jscomp$inline_135$$ = this.hp / this.base_hp;
  $ctx$jscomp$inline_130$$.fillStyle = "rgb(".concat(Math.floor(255 * Math.min(2 * (1 - $hpRatio$jscomp$inline_135$$), 1)), ",", Math.floor(255 * Math.min(2 * $hpRatio$jscomp$inline_135$$, 1)), ",0)");
  $ctx$jscomp$inline_130$$.fillRect($drawX$jscomp$inline_133_x$jscomp$inline_131$$, $drawY$jscomp$inline_134_y$jscomp$inline_132$$, this.drawWidth * $hpRatio$jscomp$inline_135$$, 4);
}, $drawTitle$:function($ctx$jscomp$inline_136$$, $x$jscomp$inline_137$$, $y$jscomp$inline_138$$) {
  var $title$jscomp$inline_139$$ = this.$getTitle$();
  void 0 == $title$jscomp$inline_139$$ && ($title$jscomp$inline_139$$ = this._name, void 0 == $title$jscomp$inline_139$$ || "" == $title$jscomp$inline_139$$) && ($title$jscomp$inline_139$$ = this["class"], void 0 == $title$jscomp$inline_139$$ && ($title$jscomp$inline_139$$ = this.type));
  "undefined" != typeof $title$jscomp$inline_139$$ && ($ctx$jscomp$inline_136$$.font = "14px Arial", this.$drawOutlineText$($ctx$jscomp$inline_136$$, $title$jscomp$inline_139$$, this.$titleStyle$, $x$jscomp$inline_137$$ + (32 * this.width - $ctx$jscomp$inline_136$$.measureText($title$jscomp$inline_139$$).width) / 2, $y$jscomp$inline_138$$ + 32 * this.height - this.drawHeight - 6 - 5 - 6));
}, $drawAttack$:function($ctx$jscomp$inline_140$$) {
  null != this.$attackSprite$ && (this.$attackSprite$.$expired$() ? this.$attackSprite$ = null : this.$attackSprite$.$draw$($ctx$jscomp$inline_140$$, 32 * this._x, 32 * this._y, this.drawWidth, this.drawHeight));
}, $getAttackTarget$:function() {
  !this.$_target$ && this.target && (this.$_target$ = $marauroa$$.$currentZone$[this.target]) && this.$_target$.$onTargeted$(this);
  return this.$_target$;
}, $onDamaged$:function() {
  this.$attackResult$ = this.$createResultIcon$("/data/sprites/combat/hitted.png");
  var $sounds$jscomp$inline_141$$ = "attack-melee-01 attack-melee-02 attack-melee-03 attack-melee-04 attack-melee-05 attack-melee-06 attack-melee-07".split(" ");
  $stendhal$$.$ui$.$sound$.$playLocalizedEffect$(this._x, this._y, 20, 3, $sounds$jscomp$inline_141$$[Math.floor(Math.random() * Math.floor($sounds$jscomp$inline_141$$.length))], 1);
}, $onBlocked$:function() {
  this.$attackResult$ = this.$createResultIcon$("/data/sprites/combat/blocked.png");
  var $sounds$jscomp$inline_142$$ = ["clang-metallic-1", "clang-dull-1"];
  $stendhal$$.$ui$.$sound$.$playLocalizedEffect$(this._x, this._y, 20, 3, $sounds$jscomp$inline_142$$[Math.floor(Math.random() * Math.floor($sounds$jscomp$inline_142$$.length))], 1);
}, $onMissed$:function() {
  this.$attackResult$ = this.$createResultIcon$("/data/sprites/combat/missed.png");
}, $onHPChanged$:function($change$jscomp$inline_143$$) {
  0 < $change$jscomp$inline_143$$ ? this.$addFloater$("+" + $change$jscomp$inline_143$$, "#00ff00") : 0 > $change$jscomp$inline_143$$ && this.$addFloater$($change$jscomp$inline_143$$.toString(), "#ff0000");
}, $onXPChanged$:function($change$jscomp$inline_144$$) {
  0 < $change$jscomp$inline_144$$ ? (this.$addFloater$("+" + $change$jscomp$inline_144$$, "#4169e1"), $stendhal$$.$ui$.$chatLog$.$addLine$("significant_positive", this.$getTitle$() + " earns " + $change$jscomp$inline_144$$ + " experience points.")) : 0 > $change$jscomp$inline_144$$ && (this.$addFloater$($change$jscomp$inline_144$$.toString(), "#ff8f8f"), $stendhal$$.$ui$.$chatLog$.$addLine$("significant_negative", this.$getTitle$() + " loses " + Math.abs($change$jscomp$inline_144$$) + " experience points."));
}, $addFloater$:function($message$jscomp$inline_145$$, $color$jscomp$inline_146$$) {
  this.hasOwnProperty("floaters") || (this.$floaters$ = []);
  var $self$jscomp$inline_147$$ = this;
  this.$floaters$.push({$initTime$:Date.now(), $textOffset$:null, $draw$:function($ctx$jscomp$inline_148$$, $x$jscomp$inline_149$$, $y$jscomp$inline_150$$) {
    $ctx$jscomp$inline_148$$.font = "14px Arial";
    this.$textOffset$ || (this.$textOffset$ = $ctx$jscomp$inline_148$$.measureText($message$jscomp$inline_145$$).width / 2);
    var $timeDiff$jscomp$inline_151$$ = Date.now() - this.$initTime$;
    $self$jscomp$inline_147$$.$drawOutlineText$($ctx$jscomp$inline_148$$, $message$jscomp$inline_145$$, $color$jscomp$inline_146$$, $x$jscomp$inline_149$$ - this.$textOffset$, $y$jscomp$inline_150$$ - $timeDiff$jscomp$inline_151$$ / 50);
    return 2000 < $timeDiff$jscomp$inline_151$$;
  }});
}, $createResultIcon$:function($imagePath$jscomp$inline_152$$) {
  return {$initTime$:Date.now(), image:$stendhal$$.data.$sprites$.get($imagePath$jscomp$inline_152$$), $draw$:function($ctx$jscomp$inline_153$$, $x$jscomp$inline_154$$, $y$jscomp$inline_155$$) {
    $ctx$jscomp$inline_153$$.drawImage(this.image, $x$jscomp$inline_154$$, $y$jscomp$inline_155$$);
    return 1200 < Date.now() - this.$initTime$;
  }};
}, $onAttackPerformed$:function($imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$, $color$jscomp$inline_158_ranged$jscomp$inline_157$$) {
  if ($color$jscomp$inline_158_ranged$jscomp$inline_157$$) {
    switch($imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$) {
      default:
        $color$jscomp$inline_158_ranged$jscomp$inline_157$$ = "#c0c0c0";
        break;
      case "1":
        $color$jscomp$inline_158_ranged$jscomp$inline_157$$ = "#ff6400";
        break;
      case "2":
        $color$jscomp$inline_158_ranged$jscomp$inline_157$$ = "#8c8cff";
        break;
      case "3":
        $color$jscomp$inline_158_ranged$jscomp$inline_157$$ = "#fff08c";
        break;
      case "4":
        $color$jscomp$inline_158_ranged$jscomp$inline_157$$ = "#404040";
    }
    $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$ = this.$getAttackTarget$();
    this.$attackSprite$ = function($color$jscomp$inline_161$$, $targetX$jscomp$inline_162$$, $targetY$jscomp$inline_163$$) {
      return {$initTime$:Date.now(), $expired$:function() {
        return 180 < Date.now() - this.$initTime$;
      }, $draw$:function($ctx$jscomp$inline_164$$, $startX$jscomp$inline_170_x$jscomp$inline_165$$, $startY$jscomp$inline_171_y$jscomp$inline_166$$, $endY$jscomp$inline_174_entityWidth$jscomp$inline_167_yLength$jscomp$inline_172$$, $entityHeight$jscomp$inline_168_xLength$jscomp$inline_173$$) {
        var $endX$jscomp$inline_175_frame$jscomp$inline_169$$ = Math.min((Date.now() - this.$initTime$) / 60, 4);
        $startX$jscomp$inline_170_x$jscomp$inline_165$$ += $endY$jscomp$inline_174_entityWidth$jscomp$inline_167_yLength$jscomp$inline_172$$ / 4;
        $startY$jscomp$inline_171_y$jscomp$inline_166$$ += $entityHeight$jscomp$inline_168_xLength$jscomp$inline_173$$ / 4;
        $endY$jscomp$inline_174_entityWidth$jscomp$inline_167_yLength$jscomp$inline_172$$ = ($targetY$jscomp$inline_163$$ - $startY$jscomp$inline_171_y$jscomp$inline_166$$) / 4;
        $entityHeight$jscomp$inline_168_xLength$jscomp$inline_173$$ = ($targetX$jscomp$inline_162$$ - $startX$jscomp$inline_170_x$jscomp$inline_165$$) / 4;
        $startY$jscomp$inline_171_y$jscomp$inline_166$$ += $endX$jscomp$inline_175_frame$jscomp$inline_169$$ * $endY$jscomp$inline_174_entityWidth$jscomp$inline_167_yLength$jscomp$inline_172$$;
        $endY$jscomp$inline_174_entityWidth$jscomp$inline_167_yLength$jscomp$inline_172$$ = $startY$jscomp$inline_171_y$jscomp$inline_166$$ + $endY$jscomp$inline_174_entityWidth$jscomp$inline_167_yLength$jscomp$inline_172$$;
        $startX$jscomp$inline_170_x$jscomp$inline_165$$ += $endX$jscomp$inline_175_frame$jscomp$inline_169$$ * $entityHeight$jscomp$inline_168_xLength$jscomp$inline_173$$;
        $endX$jscomp$inline_175_frame$jscomp$inline_169$$ = $startX$jscomp$inline_170_x$jscomp$inline_165$$ + $entityHeight$jscomp$inline_168_xLength$jscomp$inline_173$$;
        $ctx$jscomp$inline_164$$.strokeStyle = $color$jscomp$inline_161$$;
        $ctx$jscomp$inline_164$$.lineWidth = 2;
        $ctx$jscomp$inline_164$$.moveTo($startX$jscomp$inline_170_x$jscomp$inline_165$$, $startY$jscomp$inline_171_y$jscomp$inline_166$$);
        $ctx$jscomp$inline_164$$.lineTo($endX$jscomp$inline_175_frame$jscomp$inline_169$$, $endY$jscomp$inline_174_entityWidth$jscomp$inline_167_yLength$jscomp$inline_172$$);
        $ctx$jscomp$inline_164$$.stroke();
      }};
    }($color$jscomp$inline_158_ranged$jscomp$inline_157$$, 32 * ($imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$.x + $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$.width / 2), 32 * ($imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$.y + $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$.height / 2));
  } else {
    switch($imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$) {
      default:
        $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$ = "/data/sprites/combat/blade_strike_cut.png";
        break;
      case "1":
        $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$ = "/data/sprites/combat/blade_strike_fire.png";
        break;
      case "2":
        $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$ = "/data/sprites/combat/blade_strike_ice.png";
        break;
      case "3":
        $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$ = "/data/sprites/combat/blade_strike_light.png";
        break;
      case "4":
        $imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$ = "/data/sprites/combat/blade_strike_dark.png";
    }
    this.$attackSprite$ = function($imagePath$jscomp$inline_176$$, $ranged$jscomp$inline_177$$, $dir$jscomp$inline_178$$) {
      return {$initTime$:Date.now(), image:$stendhal$$.data.$sprites$.get($imagePath$jscomp$inline_176$$), frame:0, $expired$:function() {
        return 180 < Date.now() - this.$initTime$;
      }, $draw$:function($ctx$jscomp$inline_179$$, $x$jscomp$inline_180$$, $y$jscomp$inline_181$$, $entityWidth$jscomp$inline_182$$, $entityHeight$jscomp$inline_183$$) {
        if (this.image.height) {
          var $drawHeight$jscomp$inline_184$$ = this.image.height / 4, $drawWidth$jscomp$inline_185$$ = this.image.width / 3;
          $ctx$jscomp$inline_179$$.drawImage(this.image, Math.floor(Math.min((Date.now() - this.$initTime$) / 60, 3)) * $drawWidth$jscomp$inline_185$$, ($dir$jscomp$inline_178$$ - 1) * $drawHeight$jscomp$inline_184$$, $drawWidth$jscomp$inline_185$$, $drawHeight$jscomp$inline_184$$, $x$jscomp$inline_180$$ + ($entityWidth$jscomp$inline_182$$ - $drawWidth$jscomp$inline_185$$) / 2, $y$jscomp$inline_181$$ + ($entityHeight$jscomp$inline_183$$ - $drawHeight$jscomp$inline_184$$) / 2, $drawWidth$jscomp$inline_185$$, 
          $drawHeight$jscomp$inline_184$$);
        }
      }};
    }($imagePath$jscomp$inline_160_nature$jscomp$inline_156_tgt$jscomp$inline_159$$, $color$jscomp$inline_158_ranged$jscomp$inline_157$$, this.dir);
  }
}, $onTargeted$:function($attacker$jscomp$inline_186$$) {
  this.$attackers$ || (this.$attackers$ = {size:0});
  $attacker$jscomp$inline_186$$.id in this.$attackers$ || (this.$attackers$[$attacker$jscomp$inline_186$$.id] = !0, this.$attackers$.size += 1);
}, $onAttackStopped$:function($attacker$jscomp$inline_187$$) {
  $attacker$jscomp$inline_187$$.id in this.$attackers$ && (delete this.$attackers$[$attacker$jscomp$inline_187$$.id], --this.$attackers$.size);
}, $destroy$:function() {
  this.$_target$ && this.$_target$.$onAttackStopped$(this);
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.player = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.rpentity, {$minimapShow$:!0, $minimapStyle$:"rgb(255, 255, 255)", dir:3, set:function($key$jscomp$63$$, $value$jscomp$110$$) {
  $marauroa$$.$rpobjectFactory$.rpentity.set.apply(this, arguments);
  "ghostmode" === $key$jscomp$63$$ && (this.$minimapShow$ = !1);
  $marauroa$$.$me$ === this && -1 < $stendhal$$.$ui$.$stats$.keys.indexOf($key$jscomp$63$$) && ($stendhal$$.$ui$.$stats$.$dirty$ = !0);
}, $isAdmin$:function() {
  return "undefined" !== typeof this.adminlevel && 600 < this.adminlevel;
}, $buildActions$:function($list$jscomp$4$$) {
  $marauroa$$.$rpobjectFactory$.rpentity.$buildActions$.apply(this, arguments);
  var $playerName$$ = this._name, $isUnknown$$ = $marauroa$$.$me$ !== this && (null == $marauroa$$.$me$.buddies || !($playerName$$ in $marauroa$$.$me$.buddies));
  $isUnknown$$ && $list$jscomp$4$$.push({title:"Add to buddies", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"addbuddy", zone:$marauroa$$.$currentZoneName$, target:$playerName$$});
  }});
  this.$isIgnored$() ? $list$jscomp$4$$.push({title:"Remove ignore", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"unignore", zone:$marauroa$$.$currentZoneName$, target:$playerName$$});
  }}) : $isUnknown$$ && $list$jscomp$4$$.push({title:"Ignore", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"ignore", zone:$marauroa$$.$currentZoneName$, target:$playerName$$});
  }});
  $marauroa$$.$me$ === this && ($list$jscomp$4$$.push({title:"Set outfit", action:function() {
    new $stendhal$$.$ui$.$OutfitDialog$();
  }}), $list$jscomp$4$$.push({title:"Where", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"where", target:$playerName$$,});
  }}));
}, $isIgnored$:function() {
  if (!$marauroa$$.$me$ || !$marauroa$$.$me$["!ignore"]) {
    return !1;
  }
  var $temp$jscomp$2$$ = $marauroa$$.$me$["!ignore"].$_objects$;
  return 0 < $temp$jscomp$2$$.length && "_" + this._name in $temp$jscomp$2$$[0];
}, $draw$:function($ctx$jscomp$26$$) {
  this.$isIgnored$() || $marauroa$$.$rpobjectFactory$.rpentity.$draw$.apply(this, arguments);
}, $getResistance$:function() {
  return "undefined" !== typeof this.ghostmode ? 0 : this.resistance;
}, $say$:function($text$jscomp$17$$) {
  this.$isIgnored$() || $marauroa$$.$rpobjectFactory$.rpentity.$say$.apply(this, arguments);
}, $isInHearingRange$:function($entity$jscomp$8$$) {
  return this.$isAdmin$() || 15 > Math.abs(this.x - $entity$jscomp$8$$.x) && 15 > Math.abs(this.y - $entity$jscomp$8$$.y);
}, $getCursor$:function() {
  return this.$isVisibleToAction$() ? "url(/data/sprites/cursor/look.png) 1 3, auto" : "url(/data/sprites/cursor/walk.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$.creature = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.rpentity, {$minimapStyle$:"rgb(255,255,0)", $spritePath$:"monsters", $titleStyle$:"#ffc8c8", onclick:function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"attack", target:"#" + this.id});
}, $say$:function($text$jscomp$18$$) {
  this.$addSpeechBubble$($text$jscomp$18$$);
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/attack.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.domesticanimal = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.rpentity, {$drawSprite$:function($ctx$jscomp$27$$) {
  var $localX$jscomp$5$$ = 32 * this._x, $localY$jscomp$5$$ = 32 * this._y, $image$jscomp$10$$ = $stendhal$$.data.$sprites$.get(this.$imagePath$);
  if ($image$jscomp$10$$.height) {
    var $yRow$jscomp$2$$ = this.dir - 1;
    this.weight >= this.largeWeight && ($yRow$jscomp$2$$ += 4);
    this.drawHeight = $image$jscomp$10$$.height / 4 / 2;
    this.drawWidth = $image$jscomp$10$$.width / 3;
    var $drawX$jscomp$2$$ = (32 * this.width - this.drawWidth) / 2, $frame$jscomp$4$$ = 0;
    0 < this.speed && ($frame$jscomp$4$$ = Math.floor(Date.now() / 100) % 3);
    $ctx$jscomp$27$$.drawImage($image$jscomp$10$$, $frame$jscomp$4$$ * this.drawWidth, $yRow$jscomp$2$$ * this.drawHeight, this.drawWidth, this.drawHeight, $localX$jscomp$5$$ + $drawX$jscomp$2$$, $localY$jscomp$5$$ + (32 * this.height - this.drawHeight), this.drawWidth, this.drawHeight);
  }
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/look.png) 1 3, auto";
}});
$marauroa$$.$rpobjectFactory$.sheep = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.domesticanimal, {$imagePath$:"/data/sprites/sheep.png", $largeWeight$:60});
$marauroa$$.$rpobjectFactory$.cat = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.domesticanimal, {$imagePath$:"/data/sprites/cat.png", $largeWeight$:20});
$marauroa$$.$rpobjectFactory$.baby_dragon = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.domesticanimal, {$imagePath$:"/data/sprites/baby_dragon.png", $largeWeight$:20, title:"baby dragon"});
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$.npc = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.rpentity, {$minimapStyle$:"rgb(0,0,255)", $spritePath$:"npc", $titleStyle$:"#c8c8ff", hp:100, base_hp:100, $drawTop$:function($ctx$jscomp$28$$) {
  var $localX$jscomp$6$$ = 32 * this._x, $localY$jscomp$6$$ = 32 * this._y;
  "undefined" == typeof this.no_hpbar && this.$drawHealthBar$($ctx$jscomp$28$$, $localX$jscomp$6$$, $localY$jscomp$6$$);
  "undefined" == typeof this.unnamed && this.$drawTitle$($ctx$jscomp$28$$, $localX$jscomp$6$$, $localY$jscomp$6$$);
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/look.png) 1 3, auto";
}});
$marauroa$$.$rpobjectFactory$.training_dummy = $marauroa$$.$rpobjectFactory$.npc;
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.sign = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:5000, "class":"default", $draw$:function($ctx$jscomp$29$$) {
  this.$imagePath$ || (this.$imagePath$ = "/data/sprites/signs/" + this["class"] + ".png");
  var $image$jscomp$11$$ = $stendhal$$.data.$sprites$.get(this.$imagePath$);
  $image$jscomp$11$$.height && $ctx$jscomp$29$$.drawImage($image$jscomp$11$$, 32 * this.x, 32 * this.y);
}, $isVisibleToAction$:function() {
  return !0;
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/look.png) 1 3, auto";
}});
$marauroa$$.$rpobjectFactory$.blackboard = $marauroa$$.$rpobjectFactory$.sign;
$marauroa$$.$rpobjectFactory$.rented_sign = $marauroa$$.$rpobjectFactory$.sign;
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.growing_entity_spawner = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:3000, $isVisibleToAction$:function() {
  return !0;
}, $buildActions$:function($list$jscomp$5$$) {
  this.menu || $list$jscomp$5$$.push({title:"Harvest", type:"use",});
  $marauroa$$.$rpobjectFactory$.growing_entity_spawner.$proto$.$buildActions$.apply(this, arguments);
}, onclick:function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"use", target:"#" + this.id, zone:$marauroa$$.$currentZoneName$});
}, $draw$:function($ctx$jscomp$30$$) {
  var $localX$jscomp$8$$ = 32 * this.x, $localY$jscomp$8$$ = 32 * this.y, $image$jscomp$12$$ = $stendhal$$.data.$sprites$.get("/data/sprites/" + this["class"] + ".png");
  if ($image$jscomp$12$$.height) {
    var $drawHeight$jscomp$1$$ = $image$jscomp$12$$.height / (parseInt(this.max_ripeness, 10) + 1);
    $ctx$jscomp$30$$.drawImage($image$jscomp$12$$, 0, this.ripeness * $drawHeight$jscomp$1$$, $image$jscomp$12$$.width, $drawHeight$jscomp$1$$, $localX$jscomp$8$$, $localY$jscomp$8$$ - $drawHeight$jscomp$1$$ + 32, $image$jscomp$12$$.width, $drawHeight$jscomp$1$$);
  }
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/harvest.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$marauroa$$.$rpobjectFactory$.door = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.portal, {zIndex:5000, $draw$:function($ctx$jscomp$31$$) {
  var $image$jscomp$13$$ = $stendhal$$.data.$sprites$.get("/data/sprites/doors/" + this["class"] + ".png");
  if ($image$jscomp$13$$.height) {
    var $x$jscomp$134$$ = 32 * (this.x - 1), $y$jscomp$116$$ = 32 * (this.y - 1), $height$jscomp$28$$ = $image$jscomp$13$$.height / 2, $offsetY$jscomp$2$$ = $height$jscomp$28$$;
    "" === this.open && ($offsetY$jscomp$2$$ = 0);
    $ctx$jscomp$31$$.drawImage($image$jscomp$13$$, 0, $offsetY$jscomp$2$$, $image$jscomp$13$$.width, $height$jscomp$28$$, $x$jscomp$134$$, $y$jscomp$116$$, $image$jscomp$13$$.width, $height$jscomp$28$$);
  }
}, $buildActions$:function($list$jscomp$6$$) {
  $list$jscomp$6$$.push({title:"Look", type:"look"});
  $list$jscomp$6$$.push({title:"Use", type:"use"});
}, $isVisibleToAction$:function() {
  return !0;
}, $getDefaultAction$:function() {
  return {type:"moveto", x:"" + this.x, y:"" + this.y, zone:$marauroa$$.$currentZoneName$};
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/portal.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.useable_entity = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:3000, action:"use", $init$:function() {
  this.$sprite$ = {height:32, width:32};
}, set:function($key$jscomp$inline_189$$, $value$jscomp$inline_190$$) {
  $marauroa$$.$rpobjectFactory$.entity.set.apply(this, arguments);
  if ("class" === $key$jscomp$inline_189$$ || "name" === $key$jscomp$inline_189$$) {
    this.$sprite$.filename = "/data/sprites/" + this["class"] + "/" + this._name + ".png";
  }
  "state" === $key$jscomp$inline_189$$ && (this.$sprite$.offsetY = 32 * this.state);
}, $isVisibleToAction$:function() {
  return !0;
},});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.visible_entity = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:1, $init$:function() {
  this.$sprite$ = {height:32, width:32};
}, set:function($key$jscomp$65$$, $value$jscomp$112$$) {
  $marauroa$$.$rpobjectFactory$.visible_entity.$proto$.set.apply(this, arguments);
  "class" === $key$jscomp$65$$ || "subclass" === $key$jscomp$65$$ || "_name" === $key$jscomp$65$$ ? this.$sprite$.filename = "/data/sprites/" + (this["class"] || "") + "/" + (this.subclass || "") + "/" + (this._name || "") + ".png" : "state" === $key$jscomp$65$$ && (this.$sprite$.offsetY = 32 * $value$jscomp$112$$);
}, $isVisibleToAction$:function() {
  return !0;
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/look.png) 1 3, auto";
}});
$marauroa$$.$rpobjectFactory$.plant_grower = $marauroa$$.$rpobjectFactory$.visible_entity;
$marauroa$$.$rpobjectFactory$.block = $marauroa$$.$rpobjectFactory$.visible_entity;
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpobjectFactory$.walkblocker = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpobjectFactory$.entity, {zIndex:3000, $isVisibleToAction$:function() {
  return !0;
}, $getCursor$:function() {
  return "url(/data/sprites/cursor/stop.png) 1 3, auto";
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$zone$ = $stendhal$$.$zone$ || {};
$stendhal$$.$zone$ = {$entityAt$:function($x$jscomp$138$$, $y$jscomp$120$$, $filter$jscomp$22$$) {
  $x$jscomp$138$$ /= 32;
  $y$jscomp$120$$ /= 32;
  var $res$jscomp$5$$ = $stendhal$$.$zone$.$ground$, $i$jscomp$55$$;
  for ($i$jscomp$55$$ in $stendhal$$.$zone$.$entities$) {
    var $obj$jscomp$36$$ = $stendhal$$.$zone$.$entities$[$i$jscomp$55$$];
    $obj$jscomp$36$$.$isVisibleToAction$($filter$jscomp$22$$) && $obj$jscomp$36$$._x <= $x$jscomp$138$$ && $obj$jscomp$36$$._y <= $y$jscomp$120$$ && $obj$jscomp$36$$._x + ($obj$jscomp$36$$.width || 1) >= $x$jscomp$138$$ && $obj$jscomp$36$$._y + ($obj$jscomp$36$$.height || 1) >= $y$jscomp$120$$ && ($res$jscomp$5$$ = $obj$jscomp$36$$);
  }
  return $res$jscomp$5$$;
}, $sortEntities$:function() {
  this.$entities$ = [];
  for (var $i$jscomp$56$$ in $marauroa$$.$currentZone$) {
    $marauroa$$.$currentZone$.hasOwnProperty($i$jscomp$56$$) && "function" != typeof $marauroa$$.$currentZone$[$i$jscomp$56$$] && this.$entities$.push($marauroa$$.$currentZone$[$i$jscomp$56$$]);
  }
  this.$entities$.sort(function($entity1$$, $entity2$$) {
    var $rv$$ = $entity1$$.zIndex - $entity2$$.zIndex;
    0 == $rv$$ && ($rv$$ = $entity1$$.y + $entity1$$.height - ($entity2$$.y + $entity2$$.height), 0 == $rv$$ && ($rv$$ = $entity1$$.id - $entity2$$.id));
    return $rv$$;
  });
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$zone$ = $stendhal$$.$zone$ || {};
$stendhal$$.$zone$.$ground$ = {$isVisibleToAction$:function() {
  return !1;
}, $calculateZoneChangeDirection$:function($x$jscomp$139$$, $y$jscomp$121$$) {
  return 15 > $x$jscomp$139$$ ? "4" : $x$jscomp$139$$ > 32 * $stendhal$$.data.map.$zoneSizeX$ - 15 ? "2" : 15 > $y$jscomp$121$$ ? "1" : $y$jscomp$121$$ > 32 * $stendhal$$.data.map.$zoneSizeY$ - 15 ? "3" : null;
}, $getCursor$:function($x$jscomp$140$$, $y$jscomp$122$$) {
  return 15 > $x$jscomp$140$$ || 15 > $y$jscomp$122$$ || $x$jscomp$140$$ > 32 * $stendhal$$.data.map.$zoneSizeX$ - 15 || $y$jscomp$122$$ > 32 * $stendhal$$.data.map.$zoneSizeY$ - 15 ? "url(/data/sprites/cursor/walkborder.png) 1 3, auto" : $stendhal$$.data.map.$collision$(Math.floor($x$jscomp$140$$ / 32), Math.floor($y$jscomp$122$$ / 32)) ? "url(/data/sprites/cursor/stop.png) 1 3, auto" : "url(/data/sprites/cursor/walk.png) 1 3, auto";
}, onclick:function($gameX_x$jscomp$141$$, $action$jscomp$13_y$jscomp$123$$, $dblclick_extend$$) {
  $gameX_x$jscomp$141$$ += $stendhal$$.$ui$.$gamewindow$.offsetX;
  var $gameY$$ = $action$jscomp$13_y$jscomp$123$$ + $stendhal$$.$ui$.$gamewindow$.offsetY;
  $action$jscomp$13_y$jscomp$123$$ = {type:"moveto", x:"" + Math.floor($gameX_x$jscomp$141$$ / 32), y:"" + Math.floor($gameY$$ / 32)};
  "boolean" == typeof $dblclick_extend$$ && $dblclick_extend$$ && ($action$jscomp$13_y$jscomp$123$$.double_click = "");
  if ($dblclick_extend$$ = this.$calculateZoneChangeDirection$($gameX_x$jscomp$141$$, $gameY$$)) {
    $action$jscomp$13_y$jscomp$123$$.extend = $dblclick_extend$$;
  }
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$13_y$jscomp$123$$);
}};
/*
 setDragImage-IE - polyfill for setDragImage method for Internet Explorer 10+
 https://github.com/MihaiValentin/setDragImage-IE */
window.$C$ = function($image$jscomp$14$$) {
  var $bodyEl$$ = document.body;
  var $preloadEl$$ = document.createElement("div");
  $preloadEl$$.style.background = 'url("' + $image$jscomp$14$$.src + '")';
  $preloadEl$$.style.position = "absolute";
  $preloadEl$$.style.opacity = 0.001;
  $bodyEl$$.appendChild($preloadEl$$);
  setTimeout(function() {
    $bodyEl$$.removeChild($preloadEl$$);
  }, 5000);
};
"function" !== typeof DataTransfer.prototype.setDragImage && (DataTransfer.prototype.setDragImage = function($dragStylesCSS_image$jscomp$15$$) {
  var $randomDraggingClassName$$ = "setdragimage-ie-dragging-" + Math.round(Math.random() * Math.pow(10, 5)) + "-" + Date.now();
  $dragStylesCSS_image$jscomp$15$$ = ["." + $randomDraggingClassName$$, "{", 'background: url("' + $dragStylesCSS_image$jscomp$15$$.src + '") no-repeat #fff 0 0 !important;', "width: " + $dragStylesCSS_image$jscomp$15$$.width + "px !important;", "height: " + $dragStylesCSS_image$jscomp$15$$.height + "px !important;", "text-indent: -9999px !important;", "border: 0 !important;", "outline: 0 !important;", "}", "." + $randomDraggingClassName$$ + " * {", "display: none !important;", "}"];
  var $dragStylesEl$$ = document.createElement("style");
  $dragStylesEl$$.innerText = $dragStylesCSS_image$jscomp$15$$.join("");
  var $headEl$$ = document.getElementsByTagName("head")[0];
  $headEl$$.appendChild($dragStylesEl$$);
  var $eventTarget$$ = window.event.target;
  $eventTarget$$.classList.add($randomDraggingClassName$$);
  setTimeout(function() {
    $headEl$$.removeChild($dragStylesEl$$);
    $eventTarget$$.classList.remove($randomDraggingClassName$$);
  }, 0);
});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$buddyList$ = {$buddies$:[], update:function() {
  var $data$jscomp$84_html$$ = $marauroa$$.$me$.buddies, $buddies$$ = [], $buddy_i$jscomp$57$$;
  for ($buddy_i$jscomp$57$$ in $data$jscomp$84_html$$) {
    if ($data$jscomp$84_html$$.hasOwnProperty($buddy_i$jscomp$57$$)) {
      var $entry$$ = {name:$buddy_i$jscomp$57$$};
      "true" == $data$jscomp$84_html$$[$buddy_i$jscomp$57$$] ? ($entry$$.$isOnline$ = !0, $entry$$.status = "online") : ($entry$$.$isOnline$ = !1, $entry$$.status = "offline");
    }
    $buddies$$.push($entry$$);
  }
  $stendhal$$.$ui$.$buddyList$.sort($buddies$$);
  $data$jscomp$84_html$$ = "";
  for ($buddy_i$jscomp$57$$ = 0; $buddy_i$jscomp$57$$ < $buddies$$.length; $buddy_i$jscomp$57$$++) {
    $data$jscomp$84_html$$ += "<li class=" + $buddies$$[$buddy_i$jscomp$57$$].status + '><img src="', $data$jscomp$84_html$$ = "online" == $buddies$$[$buddy_i$jscomp$57$$].status ? $data$jscomp$84_html$$ + "/data/gui/buddy_online.png" : $data$jscomp$84_html$$ + "/data/gui/buddy_offline.png", $data$jscomp$84_html$$ += '"> ' + $stendhal$$.$ui$.$html$.$esc$($buddies$$[$buddy_i$jscomp$57$$].name) + "</li>";
  }
  $stendhal$$.$ui$.$buddyList$.$lastHtml$ !== $data$jscomp$84_html$$ && (document.getElementById("buddyListUL").innerHTML = $data$jscomp$84_html$$, $stendhal$$.$ui$.$buddyList$.$lastHtml$ = $data$jscomp$84_html$$);
  $stendhal$$.$ui$.$buddyList$.$buddies$ = $buddies$$;
}, sort:function($buddies$jscomp$1$$) {
  $buddies$jscomp$1$$.sort(function($a$jscomp$38$$, $b$jscomp$37$$) {
    if ($a$jscomp$38$$.$isOnline$) {
      if (!$b$jscomp$37$$.$isOnline$) {
        return -1;
      }
    } else {
      if ($b$jscomp$37$$.$isOnline$) {
        return 1;
      }
    }
    return $a$jscomp$38$$.name < $b$jscomp$37$$.name ? -1 : $a$jscomp$38$$.name > $b$jscomp$37$$.name ? 1 : 0;
  });
}, $setBuddyStatus$:function($buddy$jscomp$1$$, $status$$) {
  $stendhal$$.$ui$.$buddyList$.$removeBuddy$($buddy$jscomp$1$$);
  $stendhal$$.$ui$.$buddyList$.$buddies$.push({name:$buddy$jscomp$1$$, status:$status$$});
  $stendhal$$.$ui$.$buddyList$.sort();
}, $hasBuddy$:function($buddy$jscomp$2$$) {
  for (var $arrayLength$$ = $stendhal$$.$ui$.$buddyList$.$buddies$.length, $i$jscomp$58$$ = 0; $i$jscomp$58$$ < $arrayLength$$; $i$jscomp$58$$++) {
    if ($stendhal$$.$ui$.$buddyList$.$buddies$[$i$jscomp$58$$].name === $buddy$jscomp$2$$) {
      return !0;
    }
  }
  return !1;
}, $removeBuddy$:function($buddy$jscomp$3$$) {
  for (var $arrayLength$jscomp$1$$ = $stendhal$$.$ui$.$buddyList$.$buddies$.length, $i$jscomp$59$$ = 0; $i$jscomp$59$$ < $arrayLength$jscomp$1$$; $i$jscomp$59$$++) {
    if ($stendhal$$.$ui$.$buddyList$.$buddies$[$i$jscomp$59$$].name === $buddy$jscomp$3$$) {
      $stendhal$$.$ui$.$buddyList$.$buddies$.splice($i$jscomp$59$$, 1);
      break;
    }
  }
}, $buildActions$:function($actions$$) {
  "online" === $stendhal$$.$ui$.$buddyList$.current.className ? ($actions$$.push({title:"Talk", action:function() {
    $stendhal$$.$ui$.$chatinput$.$setText$("/msg " + $stendhal$$.$ui$.$buddyList$.current.textContent.trim() + " ");
  }}), $actions$$.push({title:"Where", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"where", target:$stendhal$$.$ui$.$buddyList$.current.textContent.trim(), zone:$marauroa$$.$currentZoneName$});
  }})) : $actions$$.push({title:"Leave Message", action:function() {
    $stendhal$$.$ui$.$chatinput$.$setText$("/storemessage " + $stendhal$$.$ui$.$buddyList$.current.textContent.trim() + " ");
  }});
  $actions$$.push({title:"Remove", action:function() {
    $marauroa$$.$clientFramework$.$sendAction$({type:"removebuddy", target:$stendhal$$.$ui$.$buddyList$.current.textContent.trim(), zone:$marauroa$$.$currentZoneName$});
    $stendhal$$.$ui$.$buddyList$.$removeBuddy$($stendhal$$.$ui$.$buddyList$.current.textContent.trim());
  }});
}, $onMouseUp$:function($event$jscomp$7$$) {
  $stendhal$$.$ui$.$buddyList$.current = null;
  "LI" === $event$jscomp$7$$.target.tagName ? $stendhal$$.$ui$.$buddyList$.current = $event$jscomp$7$$.target : "IMG" === $event$jscomp$7$$.target.tagName && ($stendhal$$.$ui$.$buddyList$.current = $event$jscomp$7$$.target.parentElement);
  $stendhal$$.$ui$.$buddyList$.current && new $stendhal$ui$Menu$$($stendhal$$.$ui$.$buddyList$, Math.max(10, $event$jscomp$7$$.pageX - 50), $event$jscomp$7$$.pageY - 5);
}};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$chatLog$ = {$addLine$:function($isAtBottom_type$jscomp$156$$, $message$jscomp$30$$) {
  var $chatElement$$ = document.getElementById("chat");
  if ($chatElement$$) {
    var $date$jscomp$3_div$$ = new Date(), $time$jscomp$2$$ = "" + $date$jscomp$3_div$$.getHours() + ":";
    10 > $date$jscomp$3_div$$.getHours() && ($time$jscomp$2$$ = "0" + $time$jscomp$2$$);
    10 > $date$jscomp$3_div$$.getMinutes() && ($time$jscomp$2$$ += "0");
    $time$jscomp$2$$ += $date$jscomp$3_div$$.getMinutes();
    $date$jscomp$3_div$$ = document.createElement("div");
    $date$jscomp$3_div$$.className = "log" + $isAtBottom_type$jscomp$156$$;
    $date$jscomp$3_div$$.innerHTML = "[" + $time$jscomp$2$$ + "] " + $stendhal$$.$ui$.$chatLog$.$formatLogEntry$($message$jscomp$30$$);
    $isAtBottom_type$jscomp$156$$ = $chatElement$$.scrollHeight - $chatElement$$.clientHeight === $chatElement$$.scrollTop;
    $chatElement$$.appendChild($date$jscomp$3_div$$);
    $isAtBottom_type$jscomp$156$$ && ($chatElement$$.scrollTop = $chatElement$$.scrollHeight);
  }
}, $formatLogEntry$:function($message$jscomp$31$$) {
  if (!$message$jscomp$31$$) {
    return "";
  }
  for (var $res$jscomp$6$$ = "", $delims$$ = " ,.!?:;".split(""), $length$jscomp$17$$ = $message$jscomp$31$$.length, $inHighlight$$ = !1, $inUnderline$$ = !1, $inHighlightQuote$$ = !1, $inUnderlineQuote$$ = !1, $i$jscomp$60$$ = 0; $i$jscomp$60$$ < $length$jscomp$17$$; $i$jscomp$60$$++) {
    var $c$jscomp$34$$ = $message$jscomp$31$$[$i$jscomp$60$$];
    if ("\\" === $c$jscomp$34$$) {
      var $n$jscomp$14$$ = $message$jscomp$31$$[$i$jscomp$60$$ + 1];
      $res$jscomp$6$$ += $n$jscomp$14$$;
      $i$jscomp$60$$++;
    } else {
      if ("#" === $c$jscomp$34$$) {
        $inHighlight$$ ? $res$jscomp$6$$ += $c$jscomp$34$$ : ($n$jscomp$14$$ = $message$jscomp$31$$[$i$jscomp$60$$ + 1], "#" === $n$jscomp$14$$ ? ($res$jscomp$6$$ += $c$jscomp$34$$, $i$jscomp$60$$++) : ("'" === $n$jscomp$14$$ && ($inHighlightQuote$$ = !0, $i$jscomp$60$$++), $inHighlight$$ = !0, $res$jscomp$6$$ += '<span class="logh">'));
      } else {
        if ("\u00a7" === $c$jscomp$34$$) {
          $inUnderline$$ ? $res$jscomp$6$$ += $c$jscomp$34$$ : ($n$jscomp$14$$ = $message$jscomp$31$$[$i$jscomp$60$$ + 1], "\u00a7" === $n$jscomp$14$$ ? ($res$jscomp$6$$ += $c$jscomp$34$$, $i$jscomp$60$$++) : ("'" === $n$jscomp$14$$ && ($inUnderlineQuote$$ = !0, $i$jscomp$60$$++), $inUnderline$$ = !0, $res$jscomp$6$$ += '<span class="logi">'));
        } else {
          if ("'" === $c$jscomp$34$$) {
            $inUnderlineQuote$$ ? ($inUnderlineQuote$$ = $inUnderline$$ = !1, $res$jscomp$6$$ += "</span>") : $inHighlightQuote$$ && ($inHighlightQuote$$ = $inHighlight$$ = !1, $res$jscomp$6$$ += "</span>");
          } else {
            if ("<" === $c$jscomp$34$$) {
              $res$jscomp$6$$ += "&lt;";
            } else {
              if (-1 < $delims$$.indexOf($c$jscomp$34$$) && ($n$jscomp$14$$ = $message$jscomp$31$$[$i$jscomp$60$$ + 1], " " === $c$jscomp$34$$ || " " === $n$jscomp$14$$ || void 0 == $n$jscomp$14$$)) {
                if ($inUnderline$$ && !$inUnderlineQuote$$ && !$inHighlightQuote$$) {
                  $inUnderline$$ = !1;
                  $res$jscomp$6$$ += "</span>" + $c$jscomp$34$$;
                  continue;
                }
                if ($inHighlight$$ && !$inUnderlineQuote$$ && !$inHighlightQuote$$) {
                  $inHighlight$$ = !1;
                  $res$jscomp$6$$ += "</span>" + $c$jscomp$34$$;
                  continue;
                }
              }
              $res$jscomp$6$$ += $c$jscomp$34$$;
            }
          }
        }
      }
    }
  }
  $inUnderline$$ && ($res$jscomp$6$$ += "</span>");
  $inHighlight$$ && ($res$jscomp$6$$ += "</span>");
  return $res$jscomp$6$$;
}, clear:function() {
  document.getElementById("chat").innerHTML = "";
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$gamewindow$ = {offsetX:0, offsetY:0, timeStamp:Date.now(), $textSprites$:[], $draw$:function() {
  var $startTime$jscomp$7$$ = (new Date()).getTime();
  if ($marauroa$$.$me$ && "visible" === document.visibilityState && ($marauroa$$.$currentZoneName$ === $stendhal$$.data.map.$currentZoneName$ || "int_vault" === $stendhal$$.data.map.$currentZoneName$ || "int_adventure_island" === $stendhal$$.data.map.$currentZoneName$)) {
    var $canvas$jscomp$3$$ = document.getElementById("gamewindow");
    this.$targetTileHeight$ = this.$targetTileWidth$ = 32;
    this.$drawingError$ = !1;
    this.$ctx$ = $canvas$jscomp$3$$.getContext("2d");
    this.$ctx$.globalAlpha = 1.0;
    this.$adjustView$($canvas$jscomp$3$$);
    this.$ctx$.fillStyle = "black";
    this.$ctx$.fillRect(0, 0, 10000, 10000);
    for (var $tileOffsetX$$ = Math.floor(this.offsetX / this.$targetTileWidth$), $tileOffsetY$$ = Math.floor(this.offsetY / this.$targetTileHeight$), $drawingLayer$$ = 0; $drawingLayer$$ < $stendhal$$.data.map.$layers$.length; $drawingLayer$$++) {
      var $name$jscomp$79$$ = $stendhal$$.data.map.$layerNames$[$drawingLayer$$];
      "protection" !== $name$jscomp$79$$ && "collision" !== $name$jscomp$79$$ && "objects" !== $name$jscomp$79$$ && "blend_ground" !== $name$jscomp$79$$ && "blend_roof" !== $name$jscomp$79$$ && this.$paintLayer$($canvas$jscomp$3$$, $drawingLayer$$, $tileOffsetX$$, $tileOffsetY$$);
      "2_object" === $name$jscomp$79$$ && this.$drawEntities$();
    }
    this.$drawEntitiesTop$();
    this.$drawTextSprites$();
  }
  setTimeout(function() {
    $stendhal$$.$ui$.$gamewindow$.$draw$.apply($stendhal$$.$ui$.$gamewindow$, arguments);
  }, Math.max(50 - ((new Date()).getTime() - $startTime$jscomp$7$$), 1));
}, $paintLayer$:function($canvas$jscomp$4_xMax$$, $drawingLayer$jscomp$1_layer$jscomp$4$$, $tileOffsetX$jscomp$1$$, $tileOffsetY$jscomp$1_y$jscomp$124$$) {
  $drawingLayer$jscomp$1_layer$jscomp$4$$ = $stendhal$$.data.map.$layers$[$drawingLayer$jscomp$1_layer$jscomp$4$$];
  var $yMax$$ = Math.min($tileOffsetY$jscomp$1_y$jscomp$124$$ + $canvas$jscomp$4_xMax$$.height / this.$targetTileHeight$ + 1, $stendhal$$.data.map.$zoneSizeY$);
  for ($canvas$jscomp$4_xMax$$ = Math.min($tileOffsetX$jscomp$1$$ + $canvas$jscomp$4_xMax$$.width / this.$targetTileWidth$ + 1, $stendhal$$.data.map.$zoneSizeX$); $tileOffsetY$jscomp$1_y$jscomp$124$$ < $yMax$$; $tileOffsetY$jscomp$1_y$jscomp$124$$++) {
    for (var $x$jscomp$142$$ = $tileOffsetX$jscomp$1$$; $x$jscomp$142$$ < $canvas$jscomp$4_xMax$$; $x$jscomp$142$$++) {
      var $gid_idx$jscomp$3$$ = $drawingLayer$jscomp$1_layer$jscomp$4$$[$tileOffsetY$jscomp$1_y$jscomp$124$$ * $stendhal$$.data.map.$zoneSizeX$ + $x$jscomp$142$$], $flip$$ = $gid_idx$jscomp$3$$ & 3758096384;
      $gid_idx$jscomp$3$$ &= 536870911;
      if (0 < $gid_idx$jscomp$3$$) {
        var $tileset$$ = $stendhal$$.data.map.$getTilesetForGid$($gid_idx$jscomp$3$$);
        $gid_idx$jscomp$3$$ -= $stendhal$$.data.map.$firstgids$[$tileset$$];
        try {
          0 < $stendhal$$.data.map.$aImages$[$tileset$$].height && this.$drawTile$($stendhal$$.data.map.$aImages$[$tileset$$], $gid_idx$jscomp$3$$, $x$jscomp$142$$, $tileOffsetY$jscomp$1_y$jscomp$124$$, $flip$$);
        } catch ($e$9$$) {
          console.error($e$9$$), this.$drawingError$ = !0;
        }
      }
    }
  }
}, $drawTile$:function($$jscomp$iter$2_tileset$jscomp$1$$, $$jscomp$key$args_idx$jscomp$4$$, $pixelX_restore_x$jscomp$143$$, $pixelY_y$jscomp$125$$, $flip$jscomp$1$$) {
  $flip$jscomp$1$$ = void 0 === $flip$jscomp$1$$ ? 0 : $flip$jscomp$1$$;
  var $ctx$jscomp$32_tilesPerRow$$ = Math.floor($$jscomp$iter$2_tileset$jscomp$1$$.width / $stendhal$$.data.map.$tileWidth$);
  $pixelX_restore_x$jscomp$143$$ *= this.$targetTileWidth$;
  $pixelY_y$jscomp$125$$ *= this.$targetTileHeight$;
  if (0 === $flip$jscomp$1$$) {
    this.$ctx$.drawImage($$jscomp$iter$2_tileset$jscomp$1$$, $$jscomp$key$args_idx$jscomp$4$$ % $ctx$jscomp$32_tilesPerRow$$ * $stendhal$$.data.map.$tileWidth$, Math.floor($$jscomp$key$args_idx$jscomp$4$$ / $ctx$jscomp$32_tilesPerRow$$) * $stendhal$$.data.map.$tileHeight$, $stendhal$$.data.map.$tileWidth$, $stendhal$$.data.map.$tileHeight$, $pixelX_restore_x$jscomp$143$$, $pixelY_y$jscomp$125$$, this.$targetTileWidth$, this.$targetTileHeight$);
  } else {
    for ($ctx$jscomp$32_tilesPerRow$$ = this.$ctx$, $ctx$jscomp$32_tilesPerRow$$.translate($pixelX_restore_x$jscomp$143$$, $pixelY_y$jscomp$125$$), $pixelX_restore_x$jscomp$143$$ = [[1, 0, 0, 1, -$pixelX_restore_x$jscomp$143$$, -$pixelY_y$jscomp$125$$]], 0 !== ($flip$jscomp$1$$ & 2147483648) && ($ctx$jscomp$32_tilesPerRow$$.transform(-1, 0, 0, 1, 0, 0), $ctx$jscomp$32_tilesPerRow$$.translate(-this.$targetTileWidth$, 0), $pixelX_restore_x$jscomp$143$$.push([-1, 0, 0, 1, 0, 0]), $pixelX_restore_x$jscomp$143$$.push([1, 
    0, 0, 1, this.$targetTileWidth$, 0])), 0 !== ($flip$jscomp$1$$ & 1073741824) && ($ctx$jscomp$32_tilesPerRow$$.transform(1, 0, 0, -1, 0, 0), $ctx$jscomp$32_tilesPerRow$$.translate(0, -this.$targetTileWidth$), $pixelX_restore_x$jscomp$143$$.push([1, 0, 0, -1, 0, 0]), $pixelX_restore_x$jscomp$143$$.push([1, 0, 0, 1, 0, this.$targetTileHeight$])), 0 !== ($flip$jscomp$1$$ & 536870912) && ($ctx$jscomp$32_tilesPerRow$$.transform(0, 1, 1, 0, 0, 0), $pixelX_restore_x$jscomp$143$$.push([0, 1, 1, 0, 0, 
    0])), this.$drawTile$($$jscomp$iter$2_tileset$jscomp$1$$, $$jscomp$key$args_idx$jscomp$4$$, 0, 0), $pixelX_restore_x$jscomp$143$$.reverse(), $$jscomp$iter$2_tileset$jscomp$1$$ = $$jscomp$makeIterator$$($pixelX_restore_x$jscomp$143$$), $$jscomp$key$args_idx$jscomp$4$$ = $$jscomp$iter$2_tileset$jscomp$1$$.next(); !$$jscomp$key$args_idx$jscomp$4$$.done; $$jscomp$key$args_idx$jscomp$4$$ = $$jscomp$iter$2_tileset$jscomp$1$$.next()) {
      $ctx$jscomp$32_tilesPerRow$$.transform.apply($ctx$jscomp$32_tilesPerRow$$, $$jscomp$key$args_idx$jscomp$4$$.value);
    }
  }
}, $drawEntities$:function() {
  var $currentTime_entity$jscomp$13$$ = (new Date()).getTime(), $time$jscomp$3$$ = $currentTime_entity$jscomp$13$$ - this.timeStamp;
  this.timeStamp = $currentTime_entity$jscomp$13$$;
  for (var $i$jscomp$61$$ in $stendhal$$.$zone$.$entities$) {
    $currentTime_entity$jscomp$13$$ = $stendhal$$.$zone$.$entities$[$i$jscomp$61$$], "undefined" != typeof $currentTime_entity$jscomp$13$$.$draw$ && ($currentTime_entity$jscomp$13$$.$updatePosition$($time$jscomp$3$$), $currentTime_entity$jscomp$13$$.$draw$(this.$ctx$));
  }
}, $drawEntitiesTop$:function() {
  for (var $i$jscomp$62$$ in $stendhal$$.$zone$.$entities$) {
    "undefined" != typeof $stendhal$$.$zone$.$entities$[$i$jscomp$62$$].$drawTop$ && $stendhal$$.$zone$.$entities$[$i$jscomp$62$$].$drawTop$(this.$ctx$);
  }
}, $drawTextSprites$:function() {
  for (var $i$jscomp$63$$ = 0; $i$jscomp$63$$ < this.$textSprites$.length; $i$jscomp$63$$++) {
    this.$textSprites$[$i$jscomp$63$$].$draw$(this.$ctx$) && (this.$textSprites$.splice($i$jscomp$63$$, 1), $i$jscomp$63$$--);
  }
}, $adjustView$:function($canvas$jscomp$5$$) {
  this.$ctx$.setTransform(1, 0, 0, 1, 0, 0);
  var $centerX$jscomp$3$$ = $marauroa$$.$me$._x * this.$targetTileWidth$ + this.$targetTileWidth$ / 2 - $canvas$jscomp$5$$.width / 2, $centerY$jscomp$2$$ = $marauroa$$.$me$._y * this.$targetTileHeight$ + this.$targetTileHeight$ / 2 - $canvas$jscomp$5$$.height / 2;
  $centerX$jscomp$3$$ = Math.min($centerX$jscomp$3$$, $stendhal$$.data.map.$zoneSizeX$ * this.$targetTileWidth$ - $canvas$jscomp$5$$.width);
  $centerX$jscomp$3$$ = Math.max($centerX$jscomp$3$$, 0);
  $centerY$jscomp$2$$ = Math.min($centerY$jscomp$2$$, $stendhal$$.data.map.$zoneSizeY$ * this.$targetTileHeight$ - $canvas$jscomp$5$$.height);
  $centerY$jscomp$2$$ = Math.max($centerY$jscomp$2$$, 0);
  this.offsetX = Math.round($centerX$jscomp$3$$);
  this.offsetY = Math.round($centerY$jscomp$2$$);
  this.$ctx$.translate(-this.offsetX, -this.offsetY);
}, $addTextSprite$:function($sprite$jscomp$1$$) {
  this.$textSprites$.push($sprite$jscomp$1$$);
}, $onMouseDown$:function() {
  function $onMouseUp$$($e$jscomp$37$$) {
    var $pos$jscomp$3$$ = $stendhal$$.$ui$.$html$.$extractPosition$($e$jscomp$37$$);
    if (300 < +new Date() - $stendhal$$.$ui$.$timestampMouseDown$ || ($e$jscomp$37$$.which ? 3 === $e$jscomp$37$$.which : 2 === $e$jscomp$37$$.button)) {
      $entity$jscomp$14$$ != $stendhal$$.$zone$.$ground$ && new $stendhal$ui$Menu$$($entity$jscomp$14$$, $pos$jscomp$3$$.pageX - 50, $pos$jscomp$3$$.pageY - 5);
    } else {
      $entity$jscomp$14$$.onclick($pos$jscomp$3$$.offsetX, $pos$jscomp$3$$.offsetY);
    }
    $cleanUp$$($pos$jscomp$3$$);
    $pos$jscomp$3$$.target.focus();
    $e$jscomp$37$$.preventDefault();
  }
  function $onDrag$$($e$jscomp$38$$) {
    var $pos$jscomp$4_yDiff$$ = $stendhal$$.$ui$.$html$.$extractPosition$($e$jscomp$38$$), $xDiff$$ = $startX$jscomp$2$$ - $pos$jscomp$4_yDiff$$.offsetX;
    $pos$jscomp$4_yDiff$$ = $startY$jscomp$2$$ - $pos$jscomp$4_yDiff$$.offsetY;
    5 < $xDiff$$ * $xDiff$$ + $pos$jscomp$4_yDiff$$ * $pos$jscomp$4_yDiff$$ && $cleanUp$$($e$jscomp$38$$);
  }
  function $cleanUp$$($e$jscomp$39$$) {
    $entity$jscomp$14$$ = null;
    $e$jscomp$39$$.target.removeEventListener("mouseup", $onMouseUp$$);
    $e$jscomp$39$$.target.removeEventListener("mousemove", $onDrag$$);
    $e$jscomp$39$$.target.removeEventListener("touchend", $onMouseUp$$);
    $e$jscomp$39$$.target.removeEventListener("touchmove", $onDrag$$);
  }
  var $entity$jscomp$14$$, $startX$jscomp$2$$, $startY$jscomp$2$$;
  return function($e$jscomp$35$$) {
    var $pos$jscomp$2$$ = $stendhal$$.$ui$.$html$.$extractPosition$($e$jscomp$35$$);
    $stendhal$$.$ui$.$globalpopup$ && $stendhal$$.$ui$.$globalpopup$.close();
    $startX$jscomp$2$$ = $pos$jscomp$2$$.offsetX;
    $startY$jscomp$2$$ = $pos$jscomp$2$$.offsetY;
    $entity$jscomp$14$$ = $stendhal$$.$zone$.$entityAt$($pos$jscomp$2$$.offsetX + $stendhal$$.$ui$.$gamewindow$.offsetX, $pos$jscomp$2$$.offsetY + $stendhal$$.$ui$.$gamewindow$.offsetY);
    $stendhal$$.$ui$.$timestampMouseDown$ = +new Date();
    if ("dblclick" !== $e$jscomp$35$$.type) {
      $e$jscomp$35$$.target.addEventListener("mousemove", $onDrag$$), $e$jscomp$35$$.target.addEventListener("mouseup", $onMouseUp$$), $e$jscomp$35$$.target.addEventListener("touchmove", $onDrag$$), $e$jscomp$35$$.target.addEventListener("touchend", $onMouseUp$$);
    } else {
      if ($entity$jscomp$14$$ == $stendhal$$.$zone$.$ground$) {
        $entity$jscomp$14$$.onclick($pos$jscomp$2$$.offsetX, $pos$jscomp$2$$.offsetY, !0);
      }
    }
  };
}(), $onMouseMove$:function($e$jscomp$40_x$jscomp$145$$) {
  var $pos$jscomp$5_y$jscomp$127$$ = $stendhal$$.$ui$.$html$.$extractPosition$($e$jscomp$40_x$jscomp$145$$);
  $e$jscomp$40_x$jscomp$145$$ = $pos$jscomp$5_y$jscomp$127$$.offsetX + $stendhal$$.$ui$.$gamewindow$.offsetX;
  $pos$jscomp$5_y$jscomp$127$$ = $pos$jscomp$5_y$jscomp$127$$.offsetY + $stendhal$$.$ui$.$gamewindow$.offsetY;
  document.getElementById("gamewindow").style.cursor = $stendhal$$.$zone$.$entityAt$($e$jscomp$40_x$jscomp$145$$, $pos$jscomp$5_y$jscomp$127$$).$getCursor$($e$jscomp$40_x$jscomp$145$$, $pos$jscomp$5_y$jscomp$127$$);
}, $onDragStart$:function($e$jscomp$41$$) {
  var $draggedEntity_pos$jscomp$6$$ = $stendhal$$.$ui$.$html$.$extractPosition$($e$jscomp$41$$);
  $draggedEntity_pos$jscomp$6$$ = $stendhal$$.$zone$.$entityAt$($draggedEntity_pos$jscomp$6$$.offsetX + $stendhal$$.$ui$.$gamewindow$.offsetX, $draggedEntity_pos$jscomp$6$$.offsetY + $stendhal$$.$ui$.$gamewindow$.offsetY);
  if ("item" === $draggedEntity_pos$jscomp$6$$.type) {
    var $img$jscomp$5$$ = $stendhal$$.data.$sprites$.$getAreaOf$($stendhal$$.data.$sprites$.get($draggedEntity_pos$jscomp$6$$.$sprite$.filename), 32, 32);
  } else {
    if ("corpse" === $draggedEntity_pos$jscomp$6$$.type) {
      $img$jscomp$5$$ = $stendhal$$.data.$sprites$.get($draggedEntity_pos$jscomp$6$$.$sprite$.filename);
    } else {
      $e$jscomp$41$$.preventDefault();
      return;
    }
  }
  window.event = $e$jscomp$41$$;
  $e$jscomp$41$$.dataTransfer.setDragImage($img$jscomp$5$$, 0, 0);
  $e$jscomp$41$$.dataTransfer.setData("Text", JSON.stringify({path:$draggedEntity_pos$jscomp$6$$.$getIdPath$(), $zone$:$marauroa$$.$currentZoneName$}));
}, $onDragOver$:function($e$jscomp$42$$) {
  $e$jscomp$42$$.preventDefault();
  $e$jscomp$42$$.dataTransfer.dropEffect = "move";
  return !1;
}, $onDrop$:function($e$jscomp$43$$) {
  var $pos$jscomp$7$$ = $stendhal$$.$ui$.$html$.$extractPosition$($e$jscomp$43$$), $data$jscomp$85_datastr$$ = $e$jscomp$43$$.dataTransfer.getData("Text") || $e$jscomp$43$$.dataTransfer.getData("text/x-stendhal");
  if ($data$jscomp$85_datastr$$) {
    $data$jscomp$85_datastr$$ = JSON.parse($data$jscomp$85_datastr$$);
    var $action$jscomp$16$$ = {x:Math.floor(($pos$jscomp$7$$.offsetX + $stendhal$$.$ui$.$gamewindow$.offsetX) / 32).toString(), y:Math.floor(($pos$jscomp$7$$.offsetY + $stendhal$$.$ui$.$gamewindow$.offsetY) / 32).toString(), zone:$data$jscomp$85_datastr$$.$zone$}, $id$jscomp$10$$ = $data$jscomp$85_datastr$$.path.substr(1, $data$jscomp$85_datastr$$.path.length - 2);
    /\t/.test($id$jscomp$10$$) ? ($action$jscomp$16$$.type = "drop", $action$jscomp$16$$.source_path = $data$jscomp$85_datastr$$.path) : ($action$jscomp$16$$.type = "displace", $action$jscomp$16$$.baseitem = $id$jscomp$10$$);
    $e$jscomp$43$$.ctrlKey ? new $stendhal$ui$DropNumberDialog$$($action$jscomp$16$$, $pos$jscomp$7$$.pageX - 50, $pos$jscomp$7$$.pageY - 25) : $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$16$$);
  }
  $e$jscomp$43$$.stopPropagation();
  $e$jscomp$43$$.preventDefault();
}, $onContentMenu$:function($e$jscomp$44$$) {
  $e$jscomp$44$$.preventDefault();
}};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$html$ = {$esc$:function($msg$jscomp$17$$) {
  return $msg$jscomp$17$$.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/\n/g, "<br>");
}, $extractKeyCode$:function($event$jscomp$8$$) {
  return $event$jscomp$8$$.which ? $event$jscomp$8$$.which : e.keyCode;
}, $niceName$:function($s$jscomp$9_temp$jscomp$3$$) {
  if (!$s$jscomp$9_temp$jscomp$3$$) {
    return "";
  }
  $s$jscomp$9_temp$jscomp$3$$ = $s$jscomp$9_temp$jscomp$3$$.replace(/_/g, " ").trim();
  return $s$jscomp$9_temp$jscomp$3$$.charAt(0).toUpperCase() + $s$jscomp$9_temp$jscomp$3$$.slice(1);
}, $extractPosition$:function($event$jscomp$9$$) {
  if ($event$jscomp$9$$.changedTouches) {
    var $pos$jscomp$8$$ = {pageX:Math.round($event$jscomp$9$$.changedTouches[0].pageX), pageY:Math.round($event$jscomp$9$$.changedTouches[0].pageY), target:$event$jscomp$9$$.changedTouches[0].target};
    $pos$jscomp$8$$.offsetX = $pos$jscomp$8$$.pageX - $event$jscomp$9$$.changedTouches[0].target.offsetLeft;
    $pos$jscomp$8$$.offsetY = $pos$jscomp$8$$.pageY - $event$jscomp$9$$.changedTouches[0].target.offsetTop;
    return $pos$jscomp$8$$;
  }
  return $event$jscomp$9$$;
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$minimap$ = {width:128, height:128, $titleHeight$:15, $minimumScale$:2, $zoneChange$:function() {
  $stendhal$$.$ui$.$minimap$.$mapWidth$ = $stendhal$$.data.map.$zoneSizeX$;
  $stendhal$$.$ui$.$minimap$.$mapHeight$ = $stendhal$$.data.map.$zoneSizeY$;
  $stendhal$$.$ui$.$minimap$.scale = Math.max($stendhal$$.$ui$.$minimap$.$minimumScale$, Math.min($stendhal$$.$ui$.$minimap$.height / $stendhal$$.$ui$.$minimap$.$mapHeight$, $stendhal$$.$ui$.$minimap$.width / $stendhal$$.$ui$.$minimap$.$mapWidth$));
  $stendhal$$.$ui$.$minimap$.$createBackgroundImage$();
}, $updateBasePosition$:function() {
  if ($marauroa$$.$me$) {
    $stendhal$$.$ui$.$minimap$.$xOffset$ = 0;
    $stendhal$$.$ui$.$minimap$.$yOffset$ = 0;
    var $imageWidth$$ = $stendhal$$.$ui$.$minimap$.$mapWidth$ * $stendhal$$.$ui$.$minimap$.scale, $imageHeight$$ = $stendhal$$.$ui$.$minimap$.$mapHeight$ * $stendhal$$.$ui$.$minimap$.scale, $xpos$$ = Math.round($marauroa$$.$me$.x * $stendhal$$.$ui$.$minimap$.scale + 0.5) - $stendhal$$.$ui$.$minimap$.width / 2, $ypos$$ = Math.round($marauroa$$.$me$.y * $stendhal$$.$ui$.$minimap$.scale + 0.5) - $stendhal$$.$ui$.$minimap$.width / 2;
    $imageWidth$$ > $stendhal$$.$ui$.$minimap$.width && ($xpos$$ + $stendhal$$.$ui$.$minimap$.width > $imageWidth$$ ? $stendhal$$.$ui$.$minimap$.$xOffset$ = $imageWidth$$ - $stendhal$$.$ui$.$minimap$.width : 0 < $xpos$$ && ($stendhal$$.$ui$.$minimap$.$xOffset$ = $xpos$$));
    $imageHeight$$ > $stendhal$$.$ui$.$minimap$.height && ($ypos$$ + $stendhal$$.$ui$.$minimap$.height > $imageHeight$$ ? $stendhal$$.$ui$.$minimap$.$yOffset$ = $imageHeight$$ - $stendhal$$.$ui$.$minimap$.height : 0 < $ypos$$ && ($stendhal$$.$ui$.$minimap$.$yOffset$ = $ypos$$));
  }
}, $draw$:function() {
  if ($marauroa$$.$currentZoneName$ === $stendhal$$.data.map.$currentZoneName$ || "int_vault" === $stendhal$$.data.map.$currentZoneName$ || "int_adventure_island" === $stendhal$$.data.map.$currentZoneName$) {
    $stendhal$$.$ui$.$minimap$.scale = 10;
    $stendhal$$.$ui$.$minimap$.$zoneChange$();
    $stendhal$$.$ui$.$minimap$.$updateBasePosition$();
    var $ctx$jscomp$34$$ = document.getElementById("minimap").getContext("2d");
    $ctx$jscomp$34$$.setTransform(1, 0, 0, 1, 0, 0);
    $ctx$jscomp$34$$.fillStyle = "#606060";
    $ctx$jscomp$34$$.fillRect(0, 0, $stendhal$$.$ui$.$minimap$.width, $stendhal$$.$ui$.$minimap$.height);
    $ctx$jscomp$34$$.translate(Math.round(-$stendhal$$.$ui$.$minimap$.$xOffset$), Math.round(-$stendhal$$.$ui$.$minimap$.$yOffset$));
    $stendhal$$.$ui$.$minimap$.$drawBackground$($ctx$jscomp$34$$);
    $stendhal$$.$ui$.$minimap$.$drawEntities$($ctx$jscomp$34$$);
  }
}, $drawBackground$:function($ctx$jscomp$35$$) {
  $ctx$jscomp$35$$.save();
  $ctx$jscomp$35$$.imageSmoothingEnabled = !1;
  $ctx$jscomp$35$$.$mozImageSmoothingEnabled$ = !1;
  $ctx$jscomp$35$$.$msImageSmoothingEnabled$ = !1;
  $ctx$jscomp$35$$.scale($stendhal$$.$ui$.$minimap$.scale, $stendhal$$.$ui$.$minimap$.scale);
  $stendhal$$.$ui$.$minimap$.$bgImage$ && $ctx$jscomp$35$$.drawImage($stendhal$$.$ui$.$minimap$.$bgImage$, 0, 0);
  $ctx$jscomp$35$$.restore();
}, $createBackgroundImage$:function() {
  var $width$jscomp$31$$ = $stendhal$$.$ui$.$minimap$.$mapWidth$, $height$jscomp$29$$ = $stendhal$$.$ui$.$minimap$.$mapHeight$;
  if (!(0 >= $width$jscomp$31$$ || 0 >= $height$jscomp$29$$) && $stendhal$$.data.map.$collisionData$ !== $stendhal$$.$ui$.$minimap$.$lastZone$) {
    $stendhal$$.$ui$.$minimap$.$lastZone$ = $stendhal$$.data.map.$collisionData$;
    $stendhal$$.$ui$.$minimap$.$bgImage$ = document.createElement("canvas");
    for (var $ctx$jscomp$36$$ = $stendhal$$.$ui$.$minimap$.$bgImage$.getContext("2d"), $imgData$jscomp$2$$ = $ctx$jscomp$36$$.createImageData($width$jscomp$31$$, $height$jscomp$29$$), $y$jscomp$128$$ = 0; $y$jscomp$128$$ < $height$jscomp$29$$; $y$jscomp$128$$++) {
      for (var $x$jscomp$146$$ = 0; $x$jscomp$146$$ < $width$jscomp$31$$; $x$jscomp$146$$++) {
        var $pos$jscomp$9$$ = 4 * ($y$jscomp$128$$ * $width$jscomp$31$$ + $x$jscomp$146$$);
        $stendhal$$.data.map.$collision$($x$jscomp$146$$, $y$jscomp$128$$) ? $imgData$jscomp$2$$.data[$pos$jscomp$9$$] = 255 : $stendhal$$.data.map.$isProtected$($x$jscomp$146$$, $y$jscomp$128$$) ? ($imgData$jscomp$2$$.data[$pos$jscomp$9$$] = 202, $imgData$jscomp$2$$.data[$pos$jscomp$9$$ + 1] = 230, $imgData$jscomp$2$$.data[$pos$jscomp$9$$ + 2] = 202) : ($imgData$jscomp$2$$.data[$pos$jscomp$9$$] = 224, $imgData$jscomp$2$$.data[$pos$jscomp$9$$ + 1] = 224, $imgData$jscomp$2$$.data[$pos$jscomp$9$$ + 
        2] = 224);
        $imgData$jscomp$2$$.data[$pos$jscomp$9$$ + 3] = 255;
      }
    }
    $stendhal$$.$ui$.$minimap$.$bgImage$.width = $width$jscomp$31$$;
    $stendhal$$.$ui$.$minimap$.$bgImage$.height = $height$jscomp$29$$;
    $ctx$jscomp$36$$.putImageData($imgData$jscomp$2$$, 0, 0);
  }
}, $drawEntities$:function($ctx$jscomp$37$$) {
  $ctx$jscomp$37$$.fillStyle = "rgb(255,0,0)";
  $ctx$jscomp$37$$.strokeStyle = "rgb(0,0,0)";
  for (var $i$jscomp$64$$ in $marauroa$$.$currentZone$) {
    var $o$jscomp$4$$ = $marauroa$$.$currentZone$[$i$jscomp$64$$];
    "undefined" != typeof $o$jscomp$4$$.x && "undefined" != typeof $o$jscomp$4$$.y && ($o$jscomp$4$$.$minimapShow$ || $marauroa$$.$me$.adminlevel && 600 <= $marauroa$$.$me$.adminlevel) && ("undefined" != typeof $o$jscomp$4$$.$minimapStyle$ ? $ctx$jscomp$37$$.strokeStyle = $o$jscomp$4$$.$minimapStyle$ : $ctx$jscomp$37$$.strokeStyle = "rgb(128, 128, 128)", $ctx$jscomp$37$$.strokeRect($o$jscomp$4$$.x * $stendhal$$.$ui$.$minimap$.scale, $o$jscomp$4$$.y * $stendhal$$.$ui$.$minimap$.scale, $o$jscomp$4$$.width * 
    $stendhal$$.$ui$.$minimap$.scale, $o$jscomp$4$$.height * $stendhal$$.$ui$.$minimap$.scale));
  }
}, $onClick$:function($e$jscomp$45$$) {
  var $action$jscomp$17_x$jscomp$147$$ = Math.floor(($e$jscomp$45$$.offsetX + $stendhal$$.$ui$.$minimap$.$xOffset$) / $stendhal$$.$ui$.$minimap$.scale), $y$jscomp$129$$ = Math.floor(($e$jscomp$45$$.offsetY + $stendhal$$.$ui$.$minimap$.$yOffset$) / $stendhal$$.$ui$.$minimap$.scale);
  $stendhal$$.data.map.$collision$($action$jscomp$17_x$jscomp$147$$, $y$jscomp$129$$) || ($action$jscomp$17_x$jscomp$147$$ = {type:"moveto", x:$action$jscomp$17_x$jscomp$147$$.toString(), y:$y$jscomp$129$$.toString()}, "type" in $e$jscomp$45$$ && "dblclick" === $e$jscomp$45$$.type && ($action$jscomp$17_x$jscomp$147$$.double_click = ""), $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$17_x$jscomp$147$$));
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$keyhandler$ = {$pressedKeys$:[], $extractMoveOrFaceActionFromEvent$:function($event$jscomp$10$$) {
  return $event$jscomp$10$$.shiftKey ? "face" : "move";
}, $extractDirectionFromKeyCode$:function($code$jscomp$2_dir$jscomp$1$$) {
  $code$jscomp$2_dir$jscomp$1$$ -= 37;
  0 === $code$jscomp$2_dir$jscomp$1$$ && ($code$jscomp$2_dir$jscomp$1$$ = 4);
  return $code$jscomp$2_dir$jscomp$1$$;
}, $onKeyDown$:function($e$jscomp$46_event$jscomp$11$$) {
  $e$jscomp$46_event$jscomp$11$$ || ($e$jscomp$46_event$jscomp$11$$ = window.event);
  var $code$jscomp$3$$ = $stendhal$$.$ui$.$html$.$extractKeyCode$($e$jscomp$46_event$jscomp$11$$);
  37 <= $code$jscomp$3$$ && 40 >= $code$jscomp$3$$ ? -1 < $stendhal$$.$ui$.$keyhandler$.$pressedKeys$.indexOf($code$jscomp$3$$) || ($stendhal$$.$ui$.$keyhandler$.$pressedKeys$.push($code$jscomp$3$$), $marauroa$$.$clientFramework$.$sendAction$({type:$stendhal$$.$ui$.$keyhandler$.$extractMoveOrFaceActionFromEvent$($e$jscomp$46_event$jscomp$11$$), dir:"" + $stendhal$$.$ui$.$keyhandler$.$extractDirectionFromKeyCode$($code$jscomp$3$$)})) : $e$jscomp$46_event$jscomp$11$$.altKey || $e$jscomp$46_event$jscomp$11$$.metaKey || 
  $e$jscomp$46_event$jscomp$11$$.ctrlKey || "Control" === $e$jscomp$46_event$jscomp$11$$.key || "input" !== document.activeElement.localName && document.getElementById("chatinput").focus();
}, $onKeyUp$:function($e$jscomp$47_event$jscomp$12$$) {
  $e$jscomp$47_event$jscomp$12$$ || ($e$jscomp$47_event$jscomp$12$$ = window.event);
  var $action$jscomp$19_code$jscomp$4_i$jscomp$65$$ = $stendhal$$.$ui$.$html$.$extractKeyCode$($e$jscomp$47_event$jscomp$12$$);
  37 <= $action$jscomp$19_code$jscomp$4_i$jscomp$65$$ && 40 >= $action$jscomp$19_code$jscomp$4_i$jscomp$65$$ && ($action$jscomp$19_code$jscomp$4_i$jscomp$65$$ = $stendhal$$.$ui$.$html$.$extractKeyCode$($e$jscomp$47_event$jscomp$12$$), $action$jscomp$19_code$jscomp$4_i$jscomp$65$$ = $stendhal$$.$ui$.$keyhandler$.$pressedKeys$.indexOf($action$jscomp$19_code$jscomp$4_i$jscomp$65$$), -1 < $action$jscomp$19_code$jscomp$4_i$jscomp$65$$ && $stendhal$$.$ui$.$keyhandler$.$pressedKeys$.splice($action$jscomp$19_code$jscomp$4_i$jscomp$65$$, 
  1), $action$jscomp$19_code$jscomp$4_i$jscomp$65$$ = {type:"stop"}, $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$19_code$jscomp$4_i$jscomp$65$$), 0 < $stendhal$$.$ui$.$keyhandler$.$pressedKeys$.length && ($action$jscomp$19_code$jscomp$4_i$jscomp$65$$ = $stendhal$$.$ui$.$keyhandler$.$pressedKeys$[0], $action$jscomp$19_code$jscomp$4_i$jscomp$65$$ = {type:$stendhal$$.$ui$.$keyhandler$.$extractMoveOrFaceActionFromEvent$($e$jscomp$47_event$jscomp$12$$), dir:"" + $stendhal$$.$ui$.$keyhandler$.$extractDirectionFromKeyCode$($action$jscomp$19_code$jscomp$4_i$jscomp$65$$)}, 
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$19_code$jscomp$4_i$jscomp$65$$)));
}};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$sound$ = {$layers$:["music", "ambient", "creature", "sfx", "gui"], $playLocalizedEffect$:function($x$jscomp$148_xdist$$, $dist2_y$jscomp$130_ydist$$, $radius$jscomp$6_sound$$, $layer$jscomp$5$$, $soundName$$, $volume$$) {
  if ($stendhal$$.$config$.$sound$.play) {
    if ($radius$jscomp$6_sound$$) {
      if (!$marauroa$$.$me$ || !$x$jscomp$148_xdist$$) {
        return;
      }
      $x$jscomp$148_xdist$$ = $marauroa$$.$me$._x - $x$jscomp$148_xdist$$;
      $dist2_y$jscomp$130_ydist$$ = $marauroa$$.$me$._y - $dist2_y$jscomp$130_ydist$$;
      $dist2_y$jscomp$130_ydist$$ = $x$jscomp$148_xdist$$ * $x$jscomp$148_xdist$$ + $dist2_y$jscomp$130_ydist$$ * $dist2_y$jscomp$130_ydist$$;
      if ($dist2_y$jscomp$130_ydist$$ > $radius$jscomp$6_sound$$ * $radius$jscomp$6_sound$$) {
        return;
      }
      $volume$$ *= Math.min($radius$jscomp$6_sound$$ * $radius$jscomp$6_sound$$ / (20 * $dist2_y$jscomp$130_ydist$$), 1);
    }
    $radius$jscomp$6_sound$$ = new Audio();
    $radius$jscomp$6_sound$$.autoplay = !0;
    $radius$jscomp$6_sound$$.volume = $volume$$;
    $radius$jscomp$6_sound$$.src = "/data/sounds/" + $soundName$$ + ".ogg";
  }
}, $playGlobalizedEffect$:function($sound$jscomp$1_soundName$jscomp$1$$, $volume$jscomp$1$$) {
  $stendhal$$.$config$.$sound$.play && ($sound$jscomp$1_soundName$jscomp$1$$ = new Audio("/data/sounds/" + $sound$jscomp$1_soundName$jscomp$1$$ + ".ogg"), null != $volume$jscomp$1$$ && ($sound$jscomp$1_soundName$jscomp$1$$.volume = $volume$jscomp$1$$), $sound$jscomp$1_soundName$jscomp$1$$.autoplay = !0);
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$stats$ = {keys:"hp base_hp atk atk_item atk_xp def def_item def_xp xp level".split(" "), update:function() {
  if ($stendhal$$.$ui$.$stats$.$dirty$) {
    $stendhal$$.$ui$.$stats$.$dirty$ = !1;
    var $object$jscomp$8$$ = $marauroa$$.$me$;
    document.getElementById("stats").innerText = "HP: " + $object$jscomp$8$$.hp + " / " + $object$jscomp$8$$.base_hp + "\r\nATK: " + $object$jscomp$8$$.atk + " x " + $object$jscomp$8$$.atk_item + "\r\nDEF: " + $object$jscomp$8$$.def + " x " + $object$jscomp$8$$.def_item + "\r\nXP: " + $object$jscomp$8$$.xp + "\r\nLevel: " + $object$jscomp$8$$.level;
  }
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$chatinput$ = {history:[], $historyIndex$:0, clear:function() {
  document.getElementById("chatinput").value = "";
}, $setText$:function($text$jscomp$19$$) {
  var $chatinput$$ = document.getElementById("chatinput");
  $chatinput$$.value = $text$jscomp$19$$;
  $chatinput$$.focus();
}, $fromHistory$:function($i$jscomp$66$$) {
  $stendhal$$.$ui$.$chatinput$.$historyIndex$ += $i$jscomp$66$$;
  0 > $stendhal$$.$ui$.$chatinput$.$historyIndex$ && ($stendhal$$.$ui$.$chatinput$.$historyIndex$ = 0);
  $stendhal$$.$ui$.$chatinput$.$historyIndex$ >= $stendhal$$.$ui$.$chatinput$.history.length ? ($stendhal$$.$ui$.$chatinput$.$historyIndex$ = $stendhal$$.$ui$.$chatinput$.history.length, $stendhal$$.$ui$.$chatinput$.clear()) : document.getElementById("chatinput").value = $stendhal$$.$ui$.$chatinput$.history[$stendhal$$.$ui$.$chatinput$.$historyIndex$];
}, $onKeyDown$:function($e$jscomp$48_event$jscomp$13$$) {
  $e$jscomp$48_event$jscomp$13$$ || ($e$jscomp$48_event$jscomp$13$$ = window.event);
  var $code$jscomp$5$$ = $stendhal$$.$ui$.$html$.$extractKeyCode$($e$jscomp$48_event$jscomp$13$$);
  $e$jscomp$48_event$jscomp$13$$.shiftKey && (38 === $code$jscomp$5$$ ? $stendhal$$.$ui$.$chatinput$.$fromHistory$(-1) : 40 === $code$jscomp$5$$ && $stendhal$$.$ui$.$chatinput$.$fromHistory$(1));
}, $onKeyPress$:function($e$jscomp$49$$) {
  return 13 === $e$jscomp$49$$.keyCode ? ($stendhal$$.$ui$.$chatinput$.send(), !1) : !0;
}, $remember$:function($text$jscomp$20$$) {
  100 < $stendhal$$.$ui$.$chatinput$.history.length && $stendhal$$.$ui$.$chatinput$.history.shift();
  $stendhal$$.$ui$.$chatinput$.history[$stendhal$$.$ui$.$chatinput$.history.length] = $text$jscomp$20$$;
  $stendhal$$.$ui$.$chatinput$.$historyIndex$ = $stendhal$$.$ui$.$chatinput$.history.length;
}, send:function() {
  var $val$$ = document.getElementById("chatinput").value, $array$jscomp$10$$ = $val$$.split(" ");
  "/choosecharacter" === $array$jscomp$10$$[0] ? $marauroa$$.$clientFramework$.$chooseCharacter$($array$jscomp$10$$[1]) : "/close" === $val$$ ? $marauroa$$.$clientFramework$.close() : $stendhal$$.$slashActionRepository$.$execute$($val$$) && $stendhal$$.$ui$.$chatinput$.$remember$($val$$);
  $stendhal$$.$ui$.$chatinput$.clear();
}};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$slashActionRepository$ = {add:{$execute$:function($type$jscomp$159$$, $params$jscomp$1$$) {
  if (null == $params$jscomp$1$$) {
    return !1;
  }
  $marauroa$$.$clientFramework$.$sendAction$({type:"addbuddy", target:$params$jscomp$1$$[0]});
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, adminnote:{$execute$:function($type$jscomp$160$$, $params$jscomp$2$$, $remainder$jscomp$1$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:$type$jscomp$160$$, target:$params$jscomp$2$$[0], note:$remainder$jscomp$1$$});
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, adminlevel:{$execute$:function($action$jscomp$22_type$jscomp$161$$, $params$jscomp$3$$) {
  $action$jscomp$22_type$jscomp$161$$ = {type:$action$jscomp$22_type$jscomp$161$$, target:$params$jscomp$3$$[0],};
  2 <= $params$jscomp$3$$.length && ($action$jscomp$22_type$jscomp$161$$.newlevel = $params$jscomp$3$$[1]);
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$22_type$jscomp$161$$);
  return !0;
}, $getMinParams$:1, $getMaxParams$:2}, alter:{$execute$:function($type$jscomp$162$$, $params$jscomp$4$$, $remainder$jscomp$3$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:$type$jscomp$162$$, target:$params$jscomp$4$$[0], stat:$params$jscomp$4$$[1], mode:$params$jscomp$4$$[2], value:$remainder$jscomp$3$$});
  return !0;
}, $getMinParams$:3, $getMaxParams$:3}, altercreature:{$execute$:function($type$jscomp$163$$, $params$jscomp$5$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"altercreature", target:$params$jscomp$5$$[0], text:$params$jscomp$5$$[1]});
  return !0;
}, $getMinParams$:2, $getMaxParams$:2}, alterkill:{$execute$:function($target$jscomp$101_type$jscomp$164$$, $creature_params$jscomp$6$$, $action$jscomp$25_remainder$jscomp$5$$) {
  $target$jscomp$101_type$jscomp$164$$ = $creature_params$jscomp$6$$[0];
  var $killtype$$ = $creature_params$jscomp$6$$[1], $count$jscomp$40$$ = $creature_params$jscomp$6$$[2];
  $creature_params$jscomp$6$$ = null;
  null != $action$jscomp$25_remainder$jscomp$5$$ && "" != $action$jscomp$25_remainder$jscomp$5$$ && ($creature_params$jscomp$6$$ = $action$jscomp$25_remainder$jscomp$5$$);
  $action$jscomp$25_remainder$jscomp$5$$ = {type:"alterkill", target:$target$jscomp$101_type$jscomp$164$$, killtype:$killtype$$, count:$count$jscomp$40$$};
  null != $creature_params$jscomp$6$$ && ($action$jscomp$25_remainder$jscomp$5$$.creature = $creature_params$jscomp$6$$);
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$25_remainder$jscomp$5$$);
  return !0;
}, $getMinParams$:3, $getMaxParams$:3}, alterquest:{$execute$:function($action$jscomp$26_type$jscomp$165$$, $params$jscomp$7$$) {
  $action$jscomp$26_type$jscomp$165$$ = {type:"alterquest", target:$params$jscomp$7$$[0], name:$params$jscomp$7$$[1]};
  null != $params$jscomp$7$$[2] && ($action$jscomp$26_type$jscomp$165$$.state = $params$jscomp$7$$[2]);
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$26_type$jscomp$165$$);
  return !0;
}, $getMinParams$:2, $getMaxParams$:3}, answer:{$execute$:function($type$jscomp$166$$, $params$jscomp$8$$, $remainder$jscomp$7$$) {
  if (null == $remainder$jscomp$7$$ || "" == $remainder$jscomp$7$$) {
    return !1;
  }
  $marauroa$$.$clientFramework$.$sendAction$({type:"answer", text:$remainder$jscomp$7$$});
  return !0;
}, $getMinParams$:1, $getMaxParams$:0}, away:{$execute$:function($msg$jscomp$18_type$jscomp$167$$, $params$jscomp$9$$, $remainder$jscomp$8$$) {
  $msg$jscomp$18_type$jscomp$167$$ = null;
  0 != $remainder$jscomp$8$$.length && ($msg$jscomp$18_type$jscomp$167$$ = $remainder$jscomp$8$$);
  $marauroa$$.$clientFramework$.$sendAction$({type:"away", message:$msg$jscomp$18_type$jscomp$167$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, ban:{$execute$:function($type$jscomp$168$$, $params$jscomp$10$$, $remainder$jscomp$9$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"ban", target:$params$jscomp$10$$[0], hours:$params$jscomp$10$$[1], reason:$remainder$jscomp$9$$});
  return !0;
}, $getMinParams$:2, $getMaxParams$:2}, chat:{$execute$:function($type$jscomp$169$$, $params$jscomp$11$$, $remainder$jscomp$10$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:$type$jscomp$169$$, text:$remainder$jscomp$10$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, clear:{$execute$:function() {
  $stendhal$$.$ui$.$chatLog$.clear();
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, drop:{$execute$:function($name$jscomp$80_type$jscomp$171$$, $action$jscomp$31_params$jscomp$13$$, $remainder$jscomp$12$$) {
  console.log($name$jscomp$80_type$jscomp$171$$, $action$jscomp$31_params$jscomp$13$$, $remainder$jscomp$12$$);
  $name$jscomp$80_type$jscomp$171$$ = $remainder$jscomp$12$$;
  var $quantity$$ = parseInt($action$jscomp$31_params$jscomp$13$$[0], 10);
  console.log($name$jscomp$80_type$jscomp$171$$, $quantity$$);
  isNaN($quantity$$) && ($name$jscomp$80_type$jscomp$171$$ = ($action$jscomp$31_params$jscomp$13$$[0] + " " + $remainder$jscomp$12$$).trim(), $quantity$$ = 0);
  console.log($name$jscomp$80_type$jscomp$171$$, $quantity$$);
  $action$jscomp$31_params$jscomp$13$$ = {type:"drop", source_name:$name$jscomp$80_type$jscomp$171$$, quantity:"" + $quantity$$, x:"" + $marauroa$$.$me$.x, y:"" + $marauroa$$.$me$.y};
  console.log($action$jscomp$31_params$jscomp$13$$);
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$31_params$jscomp$13$$);
  return !0;
}, $getMinParams$:0, $getMaxParams$:1}, gag:{$execute$:function($type$jscomp$172$$, $params$jscomp$14$$, $remainder$jscomp$13$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"gag", target:$params$jscomp$14$$[0], minutes:$params$jscomp$14$$[1], reason:$remainder$jscomp$13$$});
  return !0;
}, $getMinParams$:2, $getMaxParams$:2}, group:{$execute$:function($type$jscomp$173$$, $params$jscomp$15$$, $remainder$jscomp$14$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"group_management", action:$params$jscomp$15$$[0], params:$remainder$jscomp$14$$});
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, grumpy:{$execute$:function($reason$jscomp$12_type$jscomp$174$$, $params$jscomp$16$$, $remainder$jscomp$15$$) {
  $reason$jscomp$12_type$jscomp$174$$ = null;
  0 != $remainder$jscomp$15$$.length && ($reason$jscomp$12_type$jscomp$174$$ = $remainder$jscomp$15$$);
  $marauroa$$.$clientFramework$.$sendAction$({type:"grumpy", reason:$reason$jscomp$12_type$jscomp$174$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, help:{$execute$:function() {
  for (var $msg$jscomp$19$$ = "For a detailed reference, visit #https://stendhalgame.org/wiki/Stendhal_Manual;Here are the most-used commands:;* CHATTING:;- /me <action> \tShow a message about what you are doing.;- /tell <player> <message> \tSend a private message to #player.;- /answer <message>;\t\tSend a private message to the last player who sent a message to you.;- // <message>\tSend a private message to the last player you sent a message to.;- /storemessage <player> <message> \t\tStore a private message to deliver for an offline #player.;- /who \tList all players currently online.;- /where <player> \tShow the current location of #player.;- /sentence <text> \tSet message on stendhalgame.org profile page and what players see when using #Look.;* SUPPORT:;- /support <message> \t\tAsk an administrator for help.;- /faq \t\tOpen Stendhal FAQs wiki page in browser.;* ITEM MANIPULATION:;- /markscroll <text> \t\tMark your empty scroll and add a #text label.;* BUDDIES AND ENEMIES:;- /add <player> \tAdd #player to your buddy list.;- /remove <player>;\t\tRemove #player from your buddy list.;- /ignore <player> [minutes|*|- [reason...]] \t\tAdd #player to your ignore list.;- /ignore \tFind out who is on your ignore list.;- /unignore <player> \t\tRemove #player from your ignore list.;* STATUS:;- /away <message> \t\tSet an away message.;- /away \tRemove away status.;- /grumpy <message> \t\tSet a message to ignore all non-buddies.;- /grumpy \tRemove grumpy status.;- /name <pet> <name> \t\tGive a name to your pet.;- /profile [name] \tOpens a player profile page on stendhalgame.org.;* PLAYER CONTROL:;- /walk \tToggles autowalk on/off.;- /stopwalk \tTurns autowalk off.;- /movecont <on|off> \tToggle continuous movement (allows players to continue walking after map change or teleport without releasing direction key).;* MISC:;- /info \t\tFind out what the current server time is.;- /clear \tClear chat log.;- /help \tShow help information.;- /removedetail \tRemove the detail layer (e.g. balloon, umbrella, etc.) from character.".split(";"), 
  $i$jscomp$67$$ = 0; $i$jscomp$67$$ < $msg$jscomp$19$$.length; $i$jscomp$67$$++) {
    $stendhal$$.$ui$.$chatLog$.$addLine$("info", $msg$jscomp$19$$[$i$jscomp$67$$]);
  }
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, gmhelp:{$execute$:function($msg$jscomp$20_type$jscomp$176$$, $i$jscomp$68_params$jscomp$18$$) {
  if (null == $i$jscomp$68_params$jscomp$18$$[0]) {
    $msg$jscomp$20_type$jscomp$176$$ = 'For a detailed reference, visit #https://stendhalgame.org/wiki/Stendhal:Administration{Here are the most-used GM commands:{* GENERAL:{- /gmhelp [alter|script|support]{\t\tFor more info about alter, script or the supportanswer shortcuts.{- /adminnote <player> <note>{\t\tLogs a note about #player.{- /inspect <player>{\t\tShow complete details of #player.{- /inspectkill <player> <creature>{\t\tShow creature kill counts of #player for #creature.{- /inspectquest <player> <quest_slot>{\t\tShow the state of quest for #player.{- /script <scriptname>{\t\tLoad (or reload) a script on the server. See #/gmhelp #script for details.{* CHATTING:{- /supportanswer <player> <message>{\t\tReplies to a support question. Replace #message with $faq, $faqsocial, $ignore, $faqpvp, $wiki, $knownbug, $bugstracker, $rules, $notsupport or $spam shortcuts if desired.{- /tellall <message>{\t\tSend a private message to all logged-in players.{* PLAYER CONTROL:{- /teleportto <name>{\t\tTeleport yourself near the specified player or NPC.{- /teleclickmode \tMakes you teleport to the location you double click.{- /ghostmode \tMakes yourself invisible and intangible.{- /invisible \tToggles whether or not you are invisible to creatures.{* ENTITY MANIPULATION:{- /adminlevel <player> [<newlevel>]{\t\tDisplay or set the adminlevel of the specified #player.{- /jail <player> <minutes> <reason>{\t\tImprisons #player for a given length of time.{- /gag <player> <minutes> <reason>{\t\tGags #player for a given length of time (player is unable to send messages to anyone).{- /ban <character> <hours> <reason>{\t\tBans the account of the character from logging onto the game server or website for the specified amount of hours (-1 till end of time).{- /teleport <player> <zone> <x> <y>{\t\tTeleport #player to the given location.{- /alter <player> <attrib> <mode> <value>{\t\tAlter stat #attrib of #player by the given amount; #mode can be ADD, SUB, SET or UNSET. See #/gmhelp #alter for details.{- /altercreature <id> name;atk;def;hp;xp{\t\tChange values of the creature. Use #- as a placeholder to keep default value. Useful in raids.{- /alterkill <player> <type> <count> <creature>{\t\tChange number of #creature killed #type ("solo" or "shared") to #count for #player.{- /alterquest <player> <questslot> <value>{\t\tUpdate the #questslot for #player to be #value.{- /summon <creature|item> [x] [y]{- /summon <stackable item> [quantity]{- /summon <stackable item> <x> <y> [quantity]{\t\tSummon the specified item or creature at co-ordinates #x, #y in the current zone.{- /summonat <player> <slot> [amount] <item>{\t\tSummon the specified item into the specified slot of <player>; <amount> defaults to 1 if not specified.{- /destroy <entity> \tDestroy an entity completely.{* MISC:{- /jailreport [<player>]{\t\tList the jailed players and their sentences.'.split("{");
  } else {
    if (1 == $i$jscomp$68_params$jscomp$18$$.length && null != $i$jscomp$68_params$jscomp$18$$[0]) {
      if ("alter" == $i$jscomp$68_params$jscomp$18$$[0]) {
        $msg$jscomp$20_type$jscomp$176$$ = "/alter <player> <attrib> <mode> <value>{\t\tAlter stat <attrib> of <player> by the given amount; <mode> can be ADD, SUB, SET or UNSET.{\t\t- Examples of <attrib>: atk, def, base_hp, hp, atk_xp, def_xp, xp, outfit{\t\t- When modifying 'outfit', you should use SET mode and provide an 8-digit number; the first 2 digits are the 'hair' setting, then 'head', 'outfit', then 'body'{\t\t  For example: #'/alter testplayer outfit set 12109901'{\t\t  This will make <testplayer> look like danter".split("{");
      } else {
        if ("script" == $i$jscomp$68_params$jscomp$18$$[0]) {
          $msg$jscomp$20_type$jscomp$176$$ = "usage: /script [-list|-load|-unload|-execute] [params];\t-list : shows available scripts. In this mode can be given one optional parameter for filenames filtering, with using well-known wildcards for filenames ('*' and '?', for example \"*.class\" for java-only scripts).;\t-load : load script with first parameter's filename.;\t-unload : unload script with first parameter's filename from server;\t-execute : run selected script.;;All scripts are ran using: /script scriptname [params]. After running a script you can remove any traces of it with /script -unload scriptname, this would remove any summoned creatures, for example. It's good practise to do this after summoning creatures for a raid using scripts.;#/script #AdminMaker.class : For test servers only, summons an adminmaker to aid testing.;#/script #AdminSign.class #zone #x #y #text : Makes an AdminSign in zone at (x,y) with text. To put it next to you do /script AdminSign.class - - - text.;#/script #AlterQuest.class #player #questname #state : Update the quest for a player to be in a certain state. Omit #state to remove the quest.;#/script #DeepInspect.class #player : Deep inspects a player and all his/her items.;#/script #DropPlayerItems.class #player #[amount] #item : Drop the specified amount of items from the player if they are equipped in the bag or body.;#/script #EntitySearch.class #nonrespawn : Shows the locations of all creatures that don't respawn, for example creatures that were summoned by a GM, deathmatch creatures, etc.;#/script #FixDM.class #player : sets a player's DeathMatch slot to victory status.;#/script #ListNPCs.class : lists all npcs and their position.;#/script #LogoutPlayer.class #player : kicks a player from the game.;#/script #NPCShout.class #npc #text : NPC shouts text.;#/script #NPCShoutZone.class #npc #zone #text : NPC shouts text to players in given zone. Use - in place of zone to make it your current zone.;#/script #Plague.class #1 #creature : summon a plague of raid creatures around you.;#/script #WhereWho.class : Lists where all the online players are;#/script #Maria.class : Summons Maria, who sells food&drinks. Don't forget to -unload her after you're done.;#/script #ServerReset.class : use only in a real emergency to shut down server. If possible please warn the players to logout and give them some time. It kills the server the hard way.;#/script #ResetSlot.class #player #slot : Resets the named slot such as !kills or !quests. Useful for debugging.".split(";");
        } else {
          return !1;
        }
      }
    } else {
      return !1;
    }
  }
  for ($i$jscomp$68_params$jscomp$18$$ = 0; $i$jscomp$68_params$jscomp$18$$ < $msg$jscomp$20_type$jscomp$176$$.length; $i$jscomp$68_params$jscomp$18$$++) {
    $stendhal$$.$ui$.$chatLog$.$addLine$("info", $msg$jscomp$20_type$jscomp$176$$[$i$jscomp$68_params$jscomp$18$$]);
  }
  return !0;
}, $getMinParams$:0, $getMaxParams$:1}, ignore:{$execute$:function($action$jscomp$35_type$jscomp$177$$, $duration$jscomp$1_params$jscomp$19$$, $remainder$jscomp$18$$) {
  $action$jscomp$35_type$jscomp$177$$ = {type:"ignore"};
  if (null == $duration$jscomp$1_params$jscomp$19$$[0]) {
    $action$jscomp$35_type$jscomp$177$$.list = "1";
  } else {
    $action$jscomp$35_type$jscomp$177$$.target = $duration$jscomp$1_params$jscomp$19$$[0];
    $duration$jscomp$1_params$jscomp$19$$ = $duration$jscomp$1_params$jscomp$19$$[1];
    if (null != $duration$jscomp$1_params$jscomp$19$$ && ("*" != $duration$jscomp$1_params$jscomp$19$$ || "-" != $duration$jscomp$1_params$jscomp$19$$)) {
      if (isNaN($duration$jscomp$1_params$jscomp$19$$)) {
        return !1;
      }
      $action$jscomp$35_type$jscomp$177$$.duration = $duration$jscomp$1_params$jscomp$19$$;
    }
    0 != $remainder$jscomp$18$$.length && ($action$jscomp$35_type$jscomp$177$$.reason = $remainder$jscomp$18$$);
  }
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$35_type$jscomp$177$$);
  return !0;
}, $getMinParams$:0, $getMaxParams$:2}, inspectkill:{$execute$:function($creature$jscomp$1_type$jscomp$178$$, $params$jscomp$20_target$jscomp$102$$, $action$jscomp$36_remainder$jscomp$19$$) {
  $params$jscomp$20_target$jscomp$102$$ = $params$jscomp$20_target$jscomp$102$$[0];
  $creature$jscomp$1_type$jscomp$178$$ = null;
  null != $action$jscomp$36_remainder$jscomp$19$$ && "" != $action$jscomp$36_remainder$jscomp$19$$ && ($creature$jscomp$1_type$jscomp$178$$ = $action$jscomp$36_remainder$jscomp$19$$);
  $action$jscomp$36_remainder$jscomp$19$$ = {type:"inspectkill", target:$params$jscomp$20_target$jscomp$102$$};
  null != $creature$jscomp$1_type$jscomp$178$$ && ($action$jscomp$36_remainder$jscomp$19$$.creature = $creature$jscomp$1_type$jscomp$178$$);
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$36_remainder$jscomp$19$$);
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, inspectquest:{$execute$:function($type$jscomp$179$$, $params$jscomp$21$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"inspectquest", target:$params$jscomp$21$$[0], quest_slot:$params$jscomp$21$$[1]});
  return !0;
}, $getMinParams$:2, $getMaxParams$:2}, jail:{$execute$:function($type$jscomp$180$$, $params$jscomp$22$$, $remainder$jscomp$21$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"jail", target:$params$jscomp$22$$[0], minutes:$params$jscomp$22$$[1], reason:$remainder$jscomp$21$$});
  return !0;
}, $getMinParams$:2, $getMaxParams$:2}, "/":{$execute$:function($type$jscomp$181$$, $params$jscomp$23$$, $remainder$jscomp$22$$) {
  if ("undefined" != typeof $stendhal$$.$slashActionRepository$.$lastPlayerTell$) {
    return $marauroa$$.$clientFramework$.$sendAction$({type:"tell", target:$stendhal$$.$slashActionRepository$.$lastPlayerTell$, text:$remainder$jscomp$22$$}), !0;
  }
}, $getMinParams$:0, $getMaxParams$:0}, me:{$execute$:function($type$jscomp$182$$, $params$jscomp$24$$, $remainder$jscomp$23$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"emote", text:$remainder$jscomp$23$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, movecont:{$execute$:function($action$jscomp$41_msg$jscomp$21_type$jscomp$183$$, $params$jscomp$25_state$$) {
  $action$jscomp$41_msg$jscomp$21_type$jscomp$183$$ = {type:"move.continuous",};
  $params$jscomp$25_state$$ = $params$jscomp$25_state$$[0].toLowerCase();
  if ("on" == $params$jscomp$25_state$$) {
    $action$jscomp$41_msg$jscomp$21_type$jscomp$183$$["move.continuous"] = "";
  } else {
    if ("off" != $params$jscomp$25_state$$) {
      return $stendhal$$.$ui$.$chatLog$.$addLine$("error", 'Argument must be either "on" or "off".'), !1;
    }
  }
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$41_msg$jscomp$21_type$jscomp$183$$);
  $action$jscomp$41_msg$jscomp$21_type$jscomp$183$$ = "Continuous movement ";
  $stendhal$$.$ui$.$chatLog$.$addLine$("info", ("on" == $params$jscomp$25_state$$ ? $action$jscomp$41_msg$jscomp$21_type$jscomp$183$$ + "enabled" : $action$jscomp$41_msg$jscomp$21_type$jscomp$183$$ + "disabled") + ".");
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, msg:{$execute$:function($type$jscomp$184$$, $params$jscomp$26$$, $remainder$jscomp$25$$) {
  $stendhal$$.$slashActionRepository$.$lastPlayerTell$ = $params$jscomp$26$$[0];
  $marauroa$$.$clientFramework$.$sendAction$({type:"tell", target:$params$jscomp$26$$[0], text:$remainder$jscomp$25$$});
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, p:{$execute$:function($type$jscomp$185$$, $params$jscomp$27$$, $remainder$jscomp$26$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"group_message", text:$remainder$jscomp$26$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, progressstatus:{$execute$:function($action$jscomp$44_type$jscomp$186$$, $params$jscomp$28$$, $remainder$jscomp$27$$) {
  $action$jscomp$44_type$jscomp$186$$ = {type:$action$jscomp$44_type$jscomp$186$$};
  0 < $remainder$jscomp$27$$.length && (-1 < $remainder$jscomp$27$$.indexOf("Open Quests") ? ($action$jscomp$44_type$jscomp$186$$.progress_type = "Open Quests", $remainder$jscomp$27$$ = $remainder$jscomp$27$$.substring(12)) : -1 < $remainder$jscomp$27$$.indexOf("Completed Quests") ? ($action$jscomp$44_type$jscomp$186$$.progress_type = "Completed Quests", $remainder$jscomp$27$$ = $remainder$jscomp$27$$.substring(17)) : -1 < $remainder$jscomp$27$$.indexOf("Production") && ($action$jscomp$44_type$jscomp$186$$.progress_type = 
  "Production", $remainder$jscomp$27$$ = $remainder$jscomp$27$$.substring(11)), $remainder$jscomp$27$$ && ($action$jscomp$44_type$jscomp$186$$.item = $remainder$jscomp$27$$));
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$44_type$jscomp$186$$);
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, remove:{$execute$:function($type$jscomp$187$$, $params$jscomp$29$$) {
  if (null == $params$jscomp$29$$) {
    return !1;
  }
  $marauroa$$.$clientFramework$.$sendAction$({type:"removebuddy", target:$params$jscomp$29$$[0]});
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, sentence:{$execute$:function($type$jscomp$188$$, $params$jscomp$30$$, $remainder$jscomp$29$$) {
  if (null == $params$jscomp$30$$) {
    return !1;
  }
  $marauroa$$.$clientFramework$.$sendAction$({type:"sentence", value:$remainder$jscomp$29$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, settings:{$execute$:function() {
  $stendhal$$.$ui$.$settings$.$onOpenSettingsMenu$();
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, stopwalk:{$execute$:function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"walk", mode:"stop"});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, summon:{$execute$:function($type$jscomp$191$$, $creature$jscomp$2_params$jscomp$33$$) {
  for (var $x$jscomp$149$$ = null, $y$jscomp$131$$ = null, $quantity$jscomp$1$$ = null, $nameBuilder$$ = [], $idx$jscomp$5$$ = 0; $idx$jscomp$5$$ < $creature$jscomp$2_params$jscomp$33$$.length; $idx$jscomp$5$$++) {
    var $str$jscomp$7$$ = $creature$jscomp$2_params$jscomp$33$$[$idx$jscomp$5$$];
    $str$jscomp$7$$.match("[0-9].*") ? null == $x$jscomp$149$$ ? $x$jscomp$149$$ = $str$jscomp$7$$ : null == $y$jscomp$131$$ ? $y$jscomp$131$$ = $str$jscomp$7$$ : null == $quantity$jscomp$1$$ ? $quantity$jscomp$1$$ = $str$jscomp$7$$ : $nameBuilder$$.push($str$jscomp$7$$) : $nameBuilder$$.push($str$jscomp$7$$);
  }
  null == $quantity$jscomp$1$$ && null == $y$jscomp$131$$ && null != $x$jscomp$149$$ && ($quantity$jscomp$1$$ = $x$jscomp$149$$, $x$jscomp$149$$ = null);
  $creature$jscomp$2_params$jscomp$33$$ = $nameBuilder$$.join(" ");
  if (null == $x$jscomp$149$$ || null == $y$jscomp$131$$) {
    $x$jscomp$149$$ = $marauroa$$.$me$.x.toString(), $y$jscomp$131$$ = $marauroa$$.$me$.y.toString();
  }
  $marauroa$$.$clientFramework$.$sendAction$({type:$type$jscomp$191$$, creature:$creature$jscomp$2_params$jscomp$33$$, x:$x$jscomp$149$$, y:$y$jscomp$131$$, quantity:$quantity$jscomp$1$$});
  return !0;
}, $getMinParams$:1, $getMaxParams$:-1}, summonat:{$execute$:function($type$jscomp$192$$, $params$jscomp$34$$, $remainder$jscomp$33$$) {
  var $amount$jscomp$1$$ = $params$jscomp$34$$[2];
  isNaN($amount$jscomp$1$$) && ($remainder$jscomp$33$$ ? $remainder$jscomp$33$$ = $amount$jscomp$1$$ + " " + $remainder$jscomp$33$$ : $remainder$jscomp$33$$ = $amount$jscomp$1$$, $amount$jscomp$1$$ = "1");
  $marauroa$$.$clientFramework$.$sendAction$({type:$type$jscomp$192$$, target:$params$jscomp$34$$[0], slot:$params$jscomp$34$$[1], amount:$amount$jscomp$1$$, item:$remainder$jscomp$33$$});
  return !0;
}, $getMinParams$:3, $getMaxParams$:3}, support:{$execute$:function($type$jscomp$193$$, $params$jscomp$35$$, $remainder$jscomp$34$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"support", text:$remainder$jscomp$34$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, supportanswer:{$execute$:function($type$jscomp$194$$, $params$jscomp$36$$, $remainder$jscomp$35$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"supportanswer", target:$params$jscomp$36$$[0], text:$remainder$jscomp$35$$});
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, teleport:{$execute$:function($type$jscomp$195$$, $params$jscomp$37$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"teleport", target:$params$jscomp$37$$[0], zone:$params$jscomp$37$$[1], x:$params$jscomp$37$$[2], y:$params$jscomp$37$$[3]});
  return !0;
}, $getMinParams$:4, $getMaxParams$:4}, teleportto:{$execute$:function($type$jscomp$196$$, $params$jscomp$38$$, $remainder$jscomp$37$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"teleportto", target:$remainder$jscomp$37$$,});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, tellall:{$execute$:function($type$jscomp$197$$, $params$jscomp$39$$, $remainder$jscomp$38$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"tellall", text:$remainder$jscomp$38$$});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, walk:{$execute$:function() {
  $marauroa$$.$clientFramework$.$sendAction$({type:"walk"});
  return !0;
}, $getMinParams$:0, $getMaxParams$:0}, atlas:{$execute$:function() {
  window.location = "https://stendhalgame.org/world/atlas.html?me=" + $marauroa$$.$currentZoneName$ + "." + $marauroa$$.$me$.x + "." + $marauroa$$.$me$.y;
}, $getMinParams$:0, $getMaxParams$:0}, beginnersguide:{$execute$:function() {
  window.location = "https://stendhalgame.org/wiki/Stendhal_Beginner's_Guide";
}, $getMinParams$:0, $getMaxParams$:0}, characterselector:{$execute$:function() {
  window.location = "https://stendhalgame.org/account/mycharacters.html";
}, $getMinParams$:0, $getMaxParams$:0}, faq:{$execute$:function() {
  window.location = "https://stendhalgame.org/wiki/Stendhal_FAQ";
}, $getMinParams$:0, $getMaxParams$:0}, manual:{$execute$:function() {
  window.location = "https://stendhalgame.org/wiki/Stendhal_Manual/Controls_and_Game_Settings";
}, $getMinParams$:0, $getMaxParams$:0}, profile:{$execute$:function($type$jscomp$204_url$jscomp$23$$, $name$jscomp$81_params$jscomp$46$$) {
  $type$jscomp$204_url$jscomp$23$$ = "https://stendhalgame.org/character/";
  if (0 < $name$jscomp$81_params$jscomp$46$$.length && null != $name$jscomp$81_params$jscomp$46$$[0]) {
    $name$jscomp$81_params$jscomp$46$$ = $name$jscomp$81_params$jscomp$46$$[0];
  } else {
    if ($name$jscomp$81_params$jscomp$46$$ = $marauroa$$.$me$._name, null == $name$jscomp$81_params$jscomp$46$$) {
      return console.log("Getting default username failed!"), !0;
    }
  }
  $type$jscomp$204_url$jscomp$23$$ += $name$jscomp$81_params$jscomp$46$$ + ".html";
  $stendhal$$.$ui$.$chatLog$.$addLine$("info", "Trying to open #" + $type$jscomp$204_url$jscomp$23$$ + " in your browser.");
  window.location = $type$jscomp$204_url$jscomp$23$$;
  return !0;
}, $getMinParams$:0, $getMaxParams$:1}, rules:{$execute$:function() {
  window.location = "https://stendhalgame.org/wiki/Stendhal_Rules";
}, $getMinParams$:0, $getMaxParams$:0}, changepassword:{$execute$:function() {
  window.location = "https://stendhalgame.org/account/change-password.html";
}, $getMinParams$:0, $getMaxParams$:0}, loginhistory:{$execute$:function() {
  window.location = "https://stendhalgame.org/account/history.html";
}, $getMinParams$:0, $getMaxParams$:0}, halloffame:{$execute$:function() {
  window.location = "https://stendhalgame.org/world/hall-of-fame/active_overview.html";
}, $getMinParams$:0, $getMaxParams$:0}, storemessage:{$execute$:function($type$jscomp$209$$, $params$jscomp$51$$, $remainder$jscomp$50$$) {
  $marauroa$$.$clientFramework$.$sendAction$({type:"storemessage", target:$params$jscomp$51$$[0], text:$remainder$jscomp$50$$});
  return !0;
}, $getMinParams$:1, $getMaxParams$:1}, _default:{$execute$:function($action$jscomp$57_type$jscomp$210$$, $params$jscomp$52$$, $remainder$jscomp$51$$) {
  $action$jscomp$57_type$jscomp$210$$ = {type:$action$jscomp$57_type$jscomp$210$$};
  typeof("undefined" != $params$jscomp$52$$[0]) && ($action$jscomp$57_type$jscomp$210$$.target = $params$jscomp$52$$[0], "" != $remainder$jscomp$51$$ && ($action$jscomp$57_type$jscomp$210$$.args = $remainder$jscomp$51$$));
  $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$57_type$jscomp$210$$);
  return !0;
}, $getMinParams$:0, $getMaxParams$:1}, $execute$:function($array$jscomp$11_line$$) {
  $array$jscomp$11_line$$ = $array$jscomp$11_line$$.trim().split(" ");
  for (var $i$jscomp$69$$ in $array$jscomp$11_line$$) {
    $array$jscomp$11_line$$[$i$jscomp$69$$] = $array$jscomp$11_line$$[$i$jscomp$69$$].trim();
  }
  $array$jscomp$11_line$$ = $array$jscomp$11_line$$.filter(Boolean);
  if (0 == $array$jscomp$11_line$$.length) {
    return !1;
  }
  var $name$jscomp$82$$ = $array$jscomp$11_line$$[0];
  "/" != $name$jscomp$82$$[0] ? $name$jscomp$82$$ = "/chat" : $array$jscomp$11_line$$.shift();
  $name$jscomp$82$$ = $name$jscomp$82$$.substr(1);
  var $action$jscomp$58$$ = "undefined" == typeof $stendhal$$.$slashActionRepository$[$name$jscomp$82$$] ? $stendhal$$.$slashActionRepository$._default : $stendhal$$.$slashActionRepository$[$name$jscomp$82$$];
  "where" == $name$jscomp$82$$ && 0 == $array$jscomp$11_line$$.length && ($array$jscomp$11_line$$[0] = $marauroa$$.$me$._name);
  if ($action$jscomp$58$$.$getMinParams$ <= $array$jscomp$11_line$$.length) {
    var $remainder$jscomp$52$$ = "";
    for ($i$jscomp$69$$ = $action$jscomp$58$$.$getMaxParams$; $i$jscomp$69$$ < $array$jscomp$11_line$$.length; $i$jscomp$69$$++) {
      $remainder$jscomp$52$$ = $remainder$jscomp$52$$ + $array$jscomp$11_line$$[$i$jscomp$69$$] + " ";
    }
    return $action$jscomp$58$$.$execute$($name$jscomp$82$$, $array$jscomp$11_line$$, $remainder$jscomp$52$$.trim());
  }
  $stendhal$$.$ui$.$chatLog$.$addLine$("error", "Missing arguments. Try /help");
  return !1;
}};
$stendhal$$.$slashActionRepository$.supporta = $stendhal$$.$slashActionRepository$.supportanswer;
$stendhal$$.$slashActionRepository$.tell = $stendhal$$.$slashActionRepository$.msg;
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$marauroa$$.$rpeventFactory$.attack = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function($entity$jscomp$16$$) {
  var $target$jscomp$103$$ = $entity$jscomp$16$$.$getAttackTarget$();
  if ($target$jscomp$103$$) {
    if (this.hasOwnProperty("hit")) {
      var $damage$jscomp$1$$ = parseInt(this.damage, 10);
      0 !== $damage$jscomp$1$$ ? $target$jscomp$103$$.$onDamaged$($entity$jscomp$16$$, $damage$jscomp$1$$) : $target$jscomp$103$$.$onBlocked$($entity$jscomp$16$$);
    } else {
      $target$jscomp$103$$.$onMissed$($entity$jscomp$16$$);
    }
    $entity$jscomp$16$$.$onAttackPerformed$(this.type, this.hasOwnProperty("ranged"));
  }
}});
$marauroa$$.$rpeventFactory$.examine = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function($rpobject$jscomp$1$$) {
  $rpobject$jscomp$1$$ === $marauroa$$.$me$ && new $stendhal$ui$ImageViewer$$(this.title, this.caption, this.path);
}});
$marauroa$$.$rpeventFactory$.global_visual_effect = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
}});
$marauroa$$.$rpeventFactory$.group_change_event = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function($rpobject$jscomp$3$$) {
  $rpobject$jscomp$3$$ === $marauroa$$.$me$ && $stendhal$$.data.group.$updateGroupStatus$(this.members, this.leader, this.lootmode);
}});
$marauroa$$.$rpeventFactory$.group_invite_event = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function($rpobject$jscomp$4$$) {
  $rpobject$jscomp$4$$ === $marauroa$$.$me$ && (this.expire ? $stendhal$$.$ui$.$chatLog$.$addLine$("normal", "Your group invite by " + this.leader + " has expired.") : ($stendhal$$.$ui$.$chatLog$.$addLine$("normal", "Your have been invited by " + this.leader + " to join a group."), $stendhal$$.$ui$.$chatLog$.$addLine$("normal", "To join, type: /group join " + this.leader), $stendhal$$.$ui$.$chatLog$.$addLine$("normal", "To leave the group at any time, type: /group part " + this.leader)));
}});
$marauroa$$.$rpeventFactory$.image_event = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function($rpobject$jscomp$5$$) {
  console.log("image_event", this, $rpobject$jscomp$5$$);
}});
$marauroa$$.$rpeventFactory$.player_logged_on = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
}});
$marauroa$$.$rpeventFactory$.player_logged_out = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
}});
$marauroa$$.$rpeventFactory$.private_text = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
  $stendhal$$.$ui$.$chatLog$.$addLine$(this.texttype.toLowerCase(), this.text);
}});
$marauroa$$.$rpeventFactory$.progress_status_event = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
  var $progressType$$ = this.progress_type, $dataItems$$ = this.data.substring(1, this.data.length - 1).split(/\t/);
  this.progress_type ? this.item ? $stendhal$$.$ui$.$travellog$.$itemData$($progressType$$, this.item, this.description, $dataItems$$) : $stendhal$$.$ui$.$travellog$.$progressTypeData$($progressType$$, $dataItems$$) : $stendhal$$.$ui$.$travellog$.open($dataItems$$);
}});
$marauroa$$.$rpeventFactory$.reached_achievement = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
}});
$marauroa$$.$rpeventFactory$.show_item_list = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
  this.hasOwnProperty("title") && $stendhal$$.$ui$.$chatLog$.$addLine$("normal", this.title);
  this.hasOwnProperty("caption") && $stendhal$$.$ui$.$chatLog$.$addLine$("normal", this.caption);
  if (this.hasOwnProperty("content")) {
    $stendhal$$.$ui$.$chatLog$.$addLine$("normal", "Item\t-\tPrice\t-\tDescription");
    for (var $obj$jscomp$37$$ in this.content) {
      if (this.content.hasOwnProperty($obj$jscomp$37$$)) {
        var $data$jscomp$86$$ = this.content[$obj$jscomp$37$$].a;
        $stendhal$$.$ui$.$chatLog$.$addLine$("normal", $data$jscomp$86$$.subclass + "\t" + $data$jscomp$86$$.price + "\t" + $data$jscomp$86$$.description_info);
      }
    }
  }
}});
$marauroa$$.$rpeventFactory$.sound_event = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function($rpobject$jscomp$12$$) {
  var $volume$jscomp$2$$ = 1;
  this.hasOwnProperty("volume") && ($volume$jscomp$2$$ *= parseInt(this.volume, 10) / 100);
  $stendhal$$.$ui$.$sound$.$playLocalizedEffect$($rpobject$jscomp$12$$._x, $rpobject$jscomp$12$$._y, parseInt(this.radius, 10), this.layer, this.sound, $volume$jscomp$2$$);
}});
$marauroa$$.$rpeventFactory$.text = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function($rpobject$jscomp$13$$) {
  $rpobject$jscomp$13$$.$say$(this.text);
}});
$marauroa$$.$rpeventFactory$.trade_state_change_event = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
}});
$marauroa$$.$rpeventFactory$.transition_graph = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
}});
$marauroa$$.$rpeventFactory$.view_change = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
}});
$marauroa$$.$rpeventFactory$.bestiary = $marauroa$$.$util$.$fromProto$($marauroa$$.$rpeventFactory$._default, {$execute$:function() {
  if (this.hasOwnProperty("enemies")) {
    var $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$ = ['"???" = unknown'], $$jscomp$key$e_$jscomp$key$h_hasRare_info$$ = this.enemies.includes("(rare)"), $hasAbnormal_name$jscomp$83$$ = this.enemies.includes("(abnormal)");
    if ($$jscomp$key$e_$jscomp$key$h_hasRare_info$$ || $hasAbnormal_name$jscomp$83$$) {
      var $solo_subheader$$ = "";
      $$jscomp$key$e_$jscomp$key$h_hasRare_info$$ ? ($solo_subheader$$ += '"rare"', $hasAbnormal_name$jscomp$83$$ && ($solo_subheader$$ += ' and "abnormal"')) : $solo_subheader$$ += '"abnormal"';
      $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$[1] = $solo_subheader$$ + " creatures not required for achievements";
    }
    $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$[2] = "------------------";
    $stendhal$$.$ui$.$chatLog$.$addLine$("normal", "Bestiary:");
    $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$ = $$jscomp$makeIterator$$($$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$);
    for ($$jscomp$key$e_$jscomp$key$h_hasRare_info$$ = $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$.next(); !$$jscomp$key$e_$jscomp$key$h_hasRare_info$$.done; $$jscomp$key$e_$jscomp$key$h_hasRare_info$$ = $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$.next()) {
      h = $$jscomp$key$e_$jscomp$key$h_hasRare_info$$.value, $stendhal$$.$ui$.$chatLog$.$addLine$("normal", h);
    }
    $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$ = $$jscomp$makeIterator$$(this.enemies.split(";"));
    for ($$jscomp$key$e_$jscomp$key$h_hasRare_info$$ = $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$.next(); !$$jscomp$key$e_$jscomp$key$h_hasRare_info$$.done; $$jscomp$key$e_$jscomp$key$h_hasRare_info$$ = $$jscomp$iter$3_$jscomp$iter$4_header$jscomp$2$$.next()) {
      e = $$jscomp$key$e_$jscomp$key$h_hasRare_info$$.value;
      $$jscomp$key$e_$jscomp$key$h_hasRare_info$$ = e.split(",");
      $hasAbnormal_name$jscomp$83$$ = $$jscomp$key$e_$jscomp$key$h_hasRare_info$$[0];
      var $shared$$ = $solo_subheader$$ = " ";
      "true" == $$jscomp$key$e_$jscomp$key$h_hasRare_info$$[1] && ($solo_subheader$$ = "\u2714");
      "true" == $$jscomp$key$e_$jscomp$key$h_hasRare_info$$[2] && ($shared$$ = "\u2714");
      $stendhal$$.$ui$.$chatLog$.$addLine$("normal", $hasAbnormal_name$jscomp$83$$ + ":   solo [" + $solo_subheader$$ + "], shared [" + $shared$$ + "]");
    }
  } else {
    console.log('ERROR: event does not have "enemies" attribute');
  }
}});
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
function $stendhal$ui$Popup$$($title$jscomp$14$$, $content$jscomp$3$$, $temp$jscomp$4_x$jscomp$150$$, $y$jscomp$132$$) {
  function $onMouseMovedDuringDrag$$($e$jscomp$52$$) {
    $that$jscomp$2$$.$popupdiv$.style.left = $e$jscomp$52$$.clientX - $that$jscomp$2$$.offsetX + "px";
    $that$jscomp$2$$.$popupdiv$.style.top = $e$jscomp$52$$.clientY - $that$jscomp$2$$.offsetY + "px";
  }
  function $onMouseUpDuringDrag$$() {
    window.removeEventListener("mousemove", $onMouseMovedDuringDrag$$, !0);
    window.removeEventListener("mouseup", $onMouseUpDuringDrag$$, !0);
  }
  this.close = function() {
    $that$jscomp$2$$.$v$ && $that$jscomp$2$$.$v$.call($that$jscomp$2$$);
    var $popupcontainer$jscomp$1$$ = document.getElementById("popupcontainer");
    $popupcontainer$jscomp$1$$.contains($that$jscomp$2$$.$popupdiv$) && $popupcontainer$jscomp$1$$.removeChild($that$jscomp$2$$.$popupdiv$);
  };
  var $that$jscomp$2$$ = this, $popupcontainer$$ = document.getElementById("popupcontainer");
  this.$popupdiv$ = document.createElement("div");
  this.$popupdiv$.style.position = "absolute";
  this.$popupdiv$.style.left = $temp$jscomp$4_x$jscomp$150$$ + "px";
  this.$popupdiv$.style.top = $y$jscomp$132$$ + "px";
  this.$popupdiv$.className = "popupdiv";
  $temp$jscomp$4_x$jscomp$150$$ = $content$jscomp$3$$;
  $title$jscomp$14$$ && ($temp$jscomp$4_x$jscomp$150$$ = "<div class='popuptitle background'><div class='popuptitleclose'>X</div>" + $stendhal$$.$ui$.$html$.$esc$($title$jscomp$14$$) + "</div>" + $content$jscomp$3$$);
  this.$popupdiv$.innerHTML = $temp$jscomp$4_x$jscomp$150$$;
  this.$popupdiv$.querySelector(".popuptitle").addEventListener("mousedown", function($e$jscomp$51$$) {
    window.addEventListener("mousemove", $onMouseMovedDuringDrag$$, !0);
    window.addEventListener("mouseup", $onMouseUpDuringDrag$$, !0);
    $e$jscomp$51$$.preventDefault();
    var $box$$ = $that$jscomp$2$$.$popupdiv$.getBoundingClientRect();
    $that$jscomp$2$$.offsetX = $e$jscomp$51$$.clientX - $box$$.left - window.pageXOffset;
    $that$jscomp$2$$.offsetY = $e$jscomp$51$$.clientY - $box$$.top - window.pageYOffset;
  });
  this.$popupdiv$.querySelector(".popuptitleclose").addEventListener("click", function($e$jscomp$50$$) {
    $that$jscomp$2$$.close();
    $e$jscomp$50$$.preventDefault();
    $stendhal$$.$ui$.$sound$.$playGlobalizedEffect$("click-1");
  });
  $popupcontainer$$.appendChild(this.$popupdiv$);
}
function $stendhal$ui$Menu$$($entity$jscomp$17$$, $x$jscomp$151$$, $y$jscomp$133$$) {
  $stendhal$$.$ui$.$globalpopup$ && $stendhal$$.$ui$.$globalpopup$.$popup$.close();
  var $actions$jscomp$1$$ = [], $that$jscomp$3$$ = this;
  $entity$jscomp$17$$.$buildActions$($actions$jscomp$1$$);
  $marauroa$$.$me$.adminlevel && 600 <= $marauroa$$.$me$.adminlevel && ($actions$jscomp$1$$.push({title:"(*) Inspect", action:function($entity$jscomp$18$$) {
    console.log($entity$jscomp$18$$);
  }}), $actions$jscomp$1$$.push({title:"(*) Destroy", action:function($entity$jscomp$19$$) {
    $marauroa$$.$clientFramework$.$sendAction$({type:"destroy", target:"#" + $entity$jscomp$19$$.id,});
  }}), $actions$jscomp$1$$.push({title:"(*) Alter", action:function($entity$jscomp$20$$) {
    $stendhal$$.$ui$.$chatinput$.$setText$("/alter #" + $entity$jscomp$20$$.id + " ");
  }}));
  for (var $content$jscomp$4$$ = '<div class="actionmenu">', $i$jscomp$70$$ = 0; $i$jscomp$70$$ < $actions$jscomp$1$$.length; $i$jscomp$70$$++) {
    $content$jscomp$4$$ += '<button id="actionbutton.' + $i$jscomp$70$$ + '">' + $stendhal$$.$ui$.$html$.$esc$($actions$jscomp$1$$[$i$jscomp$70$$].title) + "</button><br>";
  }
  this.$popup$ = new $stendhal$ui$Popup$$("Action", $content$jscomp$4$$ + "</div>", $x$jscomp$151$$, $y$jscomp$133$$);
  this.$popup$.$popupdiv$.addEventListener("click", function($action$jscomp$60_e$jscomp$54_i$jscomp$71$$) {
    $action$jscomp$60_e$jscomp$54_i$jscomp$71$$ = $action$jscomp$60_e$jscomp$54_i$jscomp$71$$.target.id.substring(13);
    void 0 === $action$jscomp$60_e$jscomp$54_i$jscomp$71$$ || "" === $action$jscomp$60_e$jscomp$54_i$jscomp$71$$ || 0 > $action$jscomp$60_e$jscomp$54_i$jscomp$71$$ || ($that$jscomp$3$$.$popup$.close(), $actions$jscomp$1$$ && $action$jscomp$60_e$jscomp$54_i$jscomp$71$$ < $actions$jscomp$1$$.length && ($actions$jscomp$1$$[$action$jscomp$60_e$jscomp$54_i$jscomp$71$$].action ? $actions$jscomp$1$$[$action$jscomp$60_e$jscomp$54_i$jscomp$71$$].action($entity$jscomp$17$$) : ($action$jscomp$60_e$jscomp$54_i$jscomp$71$$ = 
    {type:$actions$jscomp$1$$[$action$jscomp$60_e$jscomp$54_i$jscomp$71$$].type, target_path:$entity$jscomp$17$$.$getIdPath$(), zone:$marauroa$$.$currentZoneName$}, "[" + $entity$jscomp$17$$.id + "]" === $entity$jscomp$17$$.$getIdPath$() && ($action$jscomp$60_e$jscomp$54_i$jscomp$71$$.target = "#" + $entity$jscomp$17$$.id), $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$60_e$jscomp$54_i$jscomp$71$$))));
  });
  this.close = function() {
    this.$popup$.close();
    $stendhal$$.$ui$.$globalpopup$ = null;
  };
  $stendhal$$.$ui$.$globalpopup$ = this;
}
function $stendhal$ui$DropNumberDialog$$($action$jscomp$61$$, $x$jscomp$152$$, $y$jscomp$134$$) {
  $stendhal$$.$ui$.$globalpopup$ && $stendhal$$.$ui$.$globalpopup$.$popup$.close();
  this.action = $action$jscomp$61$$;
  this.$popup$ = new $stendhal$ui$Popup$$("Quantity", '<input type="number" min="0" value="1" id="dropnumberdialogvalue"><button id="dropnumberdialogbutton">Drop</button>', $x$jscomp$152$$, $y$jscomp$134$$);
  var $that$jscomp$4$$ = this;
  document.getElementById("dropnumberdialogbutton").addEventListener("click", function() {
    var $quantity$jscomp$2$$ = document.getElementById("dropnumberdialogvalue").value;
    $quantity$jscomp$2$$ && 0 < $quantity$jscomp$2$$ && ($that$jscomp$4$$.action.quantity = $quantity$jscomp$2$$, $marauroa$$.$clientFramework$.$sendAction$($that$jscomp$4$$.action));
    $that$jscomp$4$$.$popup$.close();
  });
  this.close = function() {
    this.$popup$.close();
    $stendhal$$.$ui$.$globalpopup$ = null;
  };
  document.getElementById("dropnumberdialogvalue").focus();
  $stendhal$$.$ui$.$globalpopup$ = this;
}
function $stendhal$ui$ImageViewer$$($title$jscomp$15$$, $caption_content$jscomp$6$$, $path$jscomp$5$$) {
  $stendhal$$.$ui$.$globalpopup$ && $stendhal$$.$ui$.$globalpopup$.$popup$.close();
  $caption_content$jscomp$6$$ = "<h3>" + $stendhal$$.$ui$.$html$.$esc$($caption_content$jscomp$6$$) + '</h3><img src="' + $stendhal$$.$ui$.$html$.$esc$($path$jscomp$5$$) + '">';
  this.$popup$ = new $stendhal$ui$Popup$$($title$jscomp$15$$, $caption_content$jscomp$6$$, 100, 50);
  this.close = function() {
    this.$popup$.close();
    $stendhal$$.$ui$.$globalpopup$ = null;
  };
  $stendhal$$.$ui$.$globalpopup$ = this;
}
;$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
function $stendhal$ui$ItemContainerWindow$$($slot$jscomp$5$$, $size$jscomp$28$$, $object$jscomp$9$$, $suffix$$, $quickPickup$$, $defaultImage$$) {
  function $onDragStart$$($e$jscomp$58$$) {
    var $item$jscomp$1_myobject$jscomp$1$$ = $object$jscomp$9$$ || $marauroa$$.$me$;
    if ($item$jscomp$1_myobject$jscomp$1$$[$slot$jscomp$5$$]) {
      if ($item$jscomp$1_myobject$jscomp$1$$ = $item$jscomp$1_myobject$jscomp$1$$[$slot$jscomp$5$$].$getByIndex$($e$jscomp$58$$.target.id.slice($slot$jscomp$5$$.length + $suffix$$.length))) {
        var $img$jscomp$6$$ = $stendhal$$.data.$sprites$.$getAreaOf$($stendhal$$.data.$sprites$.get($item$jscomp$1_myobject$jscomp$1$$.$sprite$.filename), 32, 32);
        window.event = $e$jscomp$58$$;
        $e$jscomp$58$$.dataTransfer.setDragImage($img$jscomp$6$$, 0, 0);
        $e$jscomp$58$$.dataTransfer.setData("Text", JSON.stringify({path:$item$jscomp$1_myobject$jscomp$1$$.$getIdPath$(), $zone$:$marauroa$$.$currentZoneName$}));
      } else {
        $e$jscomp$58$$.preventDefault();
      }
    } else {
      $e$jscomp$58$$.preventDefault();
    }
  }
  function $onDragOver$$($e$jscomp$59$$) {
    $e$jscomp$59$$.preventDefault();
    $e$jscomp$59$$.dataTransfer.dropEffect = "move";
    return !1;
  }
  function $onDrop$$($e$jscomp$60$$) {
    var $action$jscomp$62_myobject$jscomp$2$$ = $object$jscomp$9$$ || $marauroa$$.$me$, $data$jscomp$87_datastr$jscomp$1$$ = $e$jscomp$60$$.dataTransfer.getData("Text") || $e$jscomp$60$$.dataTransfer.getData("text/x-stendhal");
    $data$jscomp$87_datastr$jscomp$1$$ && ($data$jscomp$87_datastr$jscomp$1$$ = JSON.parse($data$jscomp$87_datastr$jscomp$1$$), $action$jscomp$62_myobject$jscomp$2$$ = {type:"equip", source_path:$data$jscomp$87_datastr$jscomp$1$$.path, target_path:"[" + $action$jscomp$62_myobject$jscomp$2$$.id + "\t" + $slot$jscomp$5$$ + "]", zone:$data$jscomp$87_datastr$jscomp$1$$.$zone$}, $e$jscomp$60$$.ctrlKey ? new $stendhal$ui$DropNumberDialog$$($action$jscomp$62_myobject$jscomp$2$$, $e$jscomp$60$$.pageX - 50, 
    $e$jscomp$60$$.pageY - 25) : $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$62_myobject$jscomp$2$$));
    $e$jscomp$60$$.stopPropagation();
    $e$jscomp$60$$.preventDefault();
  }
  function $onContextMenu$$($e$jscomp$61$$) {
    $e$jscomp$61$$.preventDefault();
  }
  function $onMouseDown$jscomp$1$$() {
    $stendhal$$.$ui$.$timestampMouseDown$ = +new Date();
  }
  function $onMouseUp$jscomp$1$$($e$jscomp$64$$) {
    $e$jscomp$64$$.preventDefault();
    var $event$jscomp$14$$ = $stendhal$$.$ui$.$html$.$extractPosition$($e$jscomp$64$$);
    if ($event$jscomp$14$$.target.$dataItem$) {
      if ($quickPickup$$) {
        $marauroa$$.$clientFramework$.$sendAction$({type:"equip", source_path:$event$jscomp$14$$.target.$dataItem$.$getIdPath$(), target_path:"[" + $marauroa$$.$me$.id + "\tbag]", clicked:"", zone:$marauroa$$.$currentZoneName$});
        return;
      }
      300 < +new Date() - $stendhal$$.$ui$.$timestampMouseDown$ || ($e$jscomp$64$$.which ? 3 === $e$jscomp$64$$.which : 2 === $e$jscomp$64$$.button) ? new $stendhal$ui$Menu$$($event$jscomp$14$$.target.$dataItem$, $event$jscomp$14$$.pageX - 50, $event$jscomp$14$$.pageY - 5) : $marauroa$$.$clientFramework$.$sendAction$({type:"use", target_path:$event$jscomp$14$$.target.$dataItem$.$getIdPath$(), zone:$marauroa$$.$currentZoneName$});
    }
    document.getElementById("gamewindow").focus();
  }
  this.update = function() {
    var $myobject$jscomp$inline_192$$ = $object$jscomp$9$$ || $marauroa$$.$me$, $cnt$jscomp$inline_193$$ = 0;
    if ($myobject$jscomp$inline_192$$[$slot$jscomp$5$$]) {
      for (var $i$jscomp$inline_194$$ = 0; $i$jscomp$inline_194$$ < $myobject$jscomp$inline_192$$[$slot$jscomp$5$$].count(); $i$jscomp$inline_194$$++) {
        var $o$jscomp$inline_195$$ = $myobject$jscomp$inline_192$$[$slot$jscomp$5$$].$getByIndex$($i$jscomp$inline_194$$), $e$jscomp$inline_196$$ = document.getElementById($slot$jscomp$5$$ + $suffix$$ + $cnt$jscomp$inline_193$$);
        $e$jscomp$inline_196$$.style.backgroundImage = "url(/data/sprites/items/" + $o$jscomp$inline_195$$["class"] + "/" + $o$jscomp$inline_195$$.subclass + ".png )";
        $e$jscomp$inline_196$$.textContent = $o$jscomp$inline_195$$.$formatQuantity$();
        $e$jscomp$inline_196$$.$dataItem$ = $o$jscomp$inline_195$$;
        $cnt$jscomp$inline_193$$++;
      }
    }
    for ($i$jscomp$inline_194$$ = $cnt$jscomp$inline_193$$; $i$jscomp$inline_194$$ < $size$jscomp$28$$; $i$jscomp$inline_194$$++) {
      $e$jscomp$inline_196$$ = document.getElementById($slot$jscomp$5$$ + $suffix$$ + $i$jscomp$inline_194$$), $e$jscomp$inline_196$$.style.backgroundImage = $defaultImage$$ ? "url(/data/gui/" + $defaultImage$$ + ")" : "none", $e$jscomp$inline_196$$.textContent = "", $e$jscomp$inline_196$$.$dataItem$ = null;
    }
  };
  for (var $i$jscomp$72$$ = 0; $i$jscomp$72$$ < $size$jscomp$28$$; $i$jscomp$72$$++) {
    var $e$jscomp$56$$ = document.getElementById($slot$jscomp$5$$ + $suffix$$ + $i$jscomp$72$$);
    $e$jscomp$56$$.setAttribute("draggable", !0);
    $e$jscomp$56$$.addEventListener("dragstart", $onDragStart$$);
    $e$jscomp$56$$.addEventListener("dragover", $onDragOver$$);
    $e$jscomp$56$$.addEventListener("drop", $onDrop$$);
    $e$jscomp$56$$.addEventListener("mousedown", $onMouseDown$jscomp$1$$);
    $e$jscomp$56$$.addEventListener("mouseup", $onMouseUp$jscomp$1$$);
    $e$jscomp$56$$.addEventListener("touchstart", $onMouseDown$jscomp$1$$);
    $e$jscomp$56$$.addEventListener("touchend", $onMouseUp$jscomp$1$$);
    $e$jscomp$56$$.addEventListener("contextmenu", $onContextMenu$$);
  }
}
$stendhal$$.$ui$.$equip$ = {$slotNames$:"head lhand rhand finger armor cloak legs feet pouch bag keyring portfolio".split(" "), $slotSizes$:[1, 1, 1, 1, 1, 1, 1, 1, 1, 12, 8, 9], $slotImages$:["slot-helmet.png", "slot-shield.png", "slot-weapon.png", "slot-ring.png", "slot-armor.png", "slot-cloak.png", "slot-legs.png", "slot-boots.png", "slot-pouch.png", null, "slot-key.png", "slot-portfolio.png"], counter:0, $pouchVisible$:!1, $init$:function() {
  $stendhal$$.$ui$.$equip$.$inventory$ = [];
  for (var $i$jscomp$74$$ in this.$slotNames$) {
    $stendhal$$.$ui$.$equip$.$inventory$.push(new $stendhal$ui$ItemContainerWindow$$(this.$slotNames$[$i$jscomp$74$$], this.$slotSizes$[$i$jscomp$74$$], null, "", !1, this.$slotImages$[$i$jscomp$74$$]));
  }
  $stendhal$$.$ui$.$showPouch$(!1);
}, update:function() {
  for (var $features$jscomp$2_i$jscomp$75$$ in this.$inventory$) {
    $stendhal$$.$ui$.$equip$.$inventory$[$features$jscomp$2_i$jscomp$75$$].update();
  }
  this.$pouchVisible$ || ($features$jscomp$2_i$jscomp$75$$ = null, null != $marauroa$$.$me$ && ($features$jscomp$2_i$jscomp$75$$ = $marauroa$$.$me$.features), null != $features$jscomp$2_i$jscomp$75$$ && null != $features$jscomp$2_i$jscomp$75$$.pouch && $stendhal$$.$ui$.$showPouch$(!0));
}, $createInventoryWindow$:function($slot$jscomp$6$$, $sizeX$$, $sizeY$$, $object$jscomp$10$$, $popup_title$jscomp$16$$, $quickPickup$jscomp$1$$) {
  $stendhal$$.$ui$.$equip$.counter++;
  var $suffix$jscomp$1$$ = "." + $stendhal$$.$ui$.$equip$.counter + ".", $html$jscomp$1$$ = '<div class="inventorypopup inventorypopup_' + $sizeX$$;
  $quickPickup$jscomp$1$$ && ($html$jscomp$1$$ += " quickPickup");
  $html$jscomp$1$$ += '">';
  for (var $i$jscomp$76$$ = 0; $i$jscomp$76$$ < $sizeX$$ * $sizeY$$; $i$jscomp$76$$++) {
    $html$jscomp$1$$ += "<div id='" + $slot$jscomp$6$$ + $suffix$jscomp$1$$ + $i$jscomp$76$$ + "' class='itemSlot'></div>";
  }
  $popup_title$jscomp$16$$ = new $stendhal$ui$Popup$$($popup_title$jscomp$16$$, $html$jscomp$1$$ + "</div>", 160, 370);
  var $itemContainer$$ = new $stendhal$ui$ItemContainerWindow$$($slot$jscomp$6$$, $sizeX$$ * $sizeY$$, $object$jscomp$10$$, $suffix$jscomp$1$$, $quickPickup$jscomp$1$$, null);
  $stendhal$$.$ui$.$equip$.$inventory$.push($itemContainer$$);
  $itemContainer$$.update();
  $popup_title$jscomp$16$$.$v$ = function() {
    $stendhal$$.$ui$.$equip$.$inventory$.splice($stendhal$$.$ui$.$equip$.$inventory$.indexOf($itemContainer$$), 1);
  };
  return $popup_title$jscomp$16$$;
}};
$stendhal$$.$ui$.$showSlot$ = function($show$jscomp$1$$) {
  var $slot$jscomp$7$$ = document.getElementById("pouch0"), $prevState$$ = $slot$jscomp$7$$.style.display;
  $slot$jscomp$7$$.style.display = !0 === $show$jscomp$1$$ ? "block" : "none";
  return $prevState$$ != $slot$jscomp$7$$.style.display;
};
$stendhal$$.$ui$.$showPouch$ = function($show$jscomp$2$$) {
  $stendhal$$.$ui$.$showSlot$($show$jscomp$2$$) && (document.getElementById("equipment").style.height = $show$jscomp$2$$ ? "200px" : "160px", pouchVisible = $show$jscomp$2$$);
};
$stendhal$$.$ui$.$equip$.$init$();
window.$v$ = window.$v$ || {};
window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$menu$ = {$onOpenAppMenu$:function($e$jscomp$65$$) {
  $stendhal$$.$ui$.$globalpopup$ && $stendhal$$.$ui$.$globalpopup$.$popup$.close();
  for (var $actions$jscomp$2$$ = [{title:"Account", children:[{title:"Change Password", action:"changepassword",}, {title:"Select character", action:"characterselector",}, {title:"Login History", action:"loginhistory",}]}, {title:"Commands", children:[{title:"Atlas", action:"atlas",}, {title:"Online Players", action:"who",}, {title:"Hall of Fame", action:"halloffame",}, {title:"Travel Log", action:"progressstatus",}]}, {title:"Help", children:[{title:"Manual", action:"manual",}, {title:"FAQ", action:"faq",
  }, {title:"Beginners Guide", action:"beginnersguide",}, {title:"Commands", action:"help",}, {title:"Rules", action:"rules",}]},], $that$jscomp$5$$ = this, $content$jscomp$7$$ = '<div class="actionmenu">', $i$jscomp$77$$ = 0; $i$jscomp$77$$ < $actions$jscomp$2$$.length; $i$jscomp$77$$++) {
    $content$jscomp$7$$ += '<div class="inlineblock"><h4 class="menugroup">' + $stendhal$$.$ui$.$html$.$esc$($actions$jscomp$2$$[$i$jscomp$77$$].title) + "</h4>";
    for (var $j$jscomp$3$$ = 0; $j$jscomp$3$$ < $actions$jscomp$2$$[$i$jscomp$77$$].children.length; $j$jscomp$3$$++) {
      $content$jscomp$7$$ += '<button id="menubutton.' + $actions$jscomp$2$$[$i$jscomp$77$$].children[$j$jscomp$3$$].action + '">' + $stendhal$$.$ui$.$html$.$esc$($actions$jscomp$2$$[$i$jscomp$77$$].children[$j$jscomp$3$$].title) + "</button><br>";
    }
    $content$jscomp$7$$ += "</div>";
  }
  this.$popup$ = new $stendhal$ui$Popup$$("Action", $content$jscomp$7$$ + "</div>", 150, $e$jscomp$65$$.pageY + 20);
  this.$popup$.$popupdiv$.addEventListener("click", function($cmd_e$jscomp$66$$) {
    $cmd_e$jscomp$66$$ = $cmd_e$jscomp$66$$.target.id.substring(11);
    $that$jscomp$5$$.$popup$.close();
    $cmd_e$jscomp$66$$ && $stendhal$$.$slashActionRepository$.$execute$("/" + $cmd_e$jscomp$66$$);
  });
  this.close = function() {
    this.$popup$.close();
    $stendhal$$.$ui$.$globalpopup$ = null;
  };
  $stendhal$$.$ui$.$globalpopup$ = this;
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$outfitCount$ = {hat:14, hair:48, mask:9, eyes:26, mouth:5, head:4, dress:63, body:3};
$stendhal$$.$ui$.$OutfitDialog$ = function() {
  function $indexString$$($index$jscomp$79$$) {
    return -1 < $index$jscomp$79$$ && 100 > $index$jscomp$79$$ ? 10 > $index$jscomp$79$$ ? "00" + $index$jscomp$79$$ : "0" + $index$jscomp$79$$ : "" + $index$jscomp$79$$;
  }
  function $makeSelector$$($part$jscomp$4$$, $index$jscomp$80$$, $partChanged$jscomp$1$$) {
    0 > $index$jscomp$80$$ && ($index$jscomp$80$$ = 0);
    var $selector$jscomp$1$$ = new $PartSelector$$($part$jscomp$4$$, $index$jscomp$80$$, $stendhal$$.$ui$.$outfitCount$[$part$jscomp$4$$] - 1, $partChanged$jscomp$1$$);
    document.getElementById("setoutfitprev" + $part$jscomp$4$$).addEventListener("click", function() {
      $selector$jscomp$1$$.$J$();
    });
    document.getElementById("setoutfitnext" + $part$jscomp$4$$).addEventListener("click", function() {
      $selector$jscomp$1$$.next();
    });
    $selector$jscomp$1$$.$draw$();
    return $selector$jscomp$1$$;
  }
  function $drawComposite$$() {
    function $draw$$($ctx$jscomp$42$$, $selector$jscomp$2$$) {
      $selector$jscomp$2$$.image.then(function($img$jscomp$10$$) {
        return $ctx$jscomp$42$$.drawImage($img$jscomp$10$$, -48, -128);
      });
    }
    var $canvas$jscomp$10_drawHair$$ = document.getElementById("setoutfitcompositecanvas"), $ctx$jscomp$41$$ = $canvas$jscomp$10_drawHair$$.getContext("2d");
    $ctx$jscomp$41$$.fillStyle = "white";
    $ctx$jscomp$41$$.fillRect(0, 0, $canvas$jscomp$10_drawHair$$.width, $canvas$jscomp$10_drawHair$$.height);
    $canvas$jscomp$10_drawHair$$ = !0;
    null !== $stendhal$$.$HATS_NO_HAIR$ && void 0 !== $stendhal$$.$HATS_NO_HAIR$ && ($canvas$jscomp$10_drawHair$$ = !$stendhal$$.$HATS_NO_HAIR$.includes(parseInt($hatSelector$$.index)));
    $draw$$($ctx$jscomp$41$$, $bodySelector$$);
    $draw$$($ctx$jscomp$41$$, $dressSelector$$);
    $draw$$($ctx$jscomp$41$$, $headSelector$$);
    $draw$$($ctx$jscomp$41$$, $mouthSelector$$);
    $draw$$($ctx$jscomp$41$$, $eyesSelector$$);
    $draw$$($ctx$jscomp$41$$, $maskSelector$$);
    $canvas$jscomp$10_drawHair$$ && $draw$$($ctx$jscomp$41$$, $hairSelector$$);
    $draw$$($ctx$jscomp$41$$, $hatSelector$$);
  }
  function $partChanged$$() {
    $drawComposite$$();
  }
  function $initialColorValue$$($part$jscomp$5$$) {
    var $colors$jscomp$2$$ = $marauroa$$.$me$.outfit_colors;
    if (null != $colors$jscomp$2$$) {
      var $colorName$$ = $part$jscomp$5$$;
      if ("body" === $part$jscomp$5$$ || "head" === $part$jscomp$5$$) {
        $colorName$$ = "skin";
      }
      return $colors$jscomp$2$$[$colorName$$];
    }
    return null;
  }
  function $createColorSelector$$($classObject$$, $part$jscomp$6$$) {
    var $partSelectors$$ = $$jscomp$getRestArguments$$.apply(2, arguments), $toggle$$ = document.getElementById("setoutfit" + $part$jscomp$6$$ + "colortoggle"), $selector$jscomp$3$$ = new $classObject$$(document.getElementById("setoutfit" + $part$jscomp$6$$ + "colorcanvas"), document.getElementById("setoutfit" + $part$jscomp$6$$ + "colorgradient"), function($color$jscomp$9$$) {
      for (var $$jscomp$iter$5$$ = $$jscomp$makeIterator$$($partSelectors$$), $$jscomp$key$partSelector$$ = $$jscomp$iter$5$$.next(); !$$jscomp$key$partSelector$$.done; $$jscomp$key$partSelector$$ = $$jscomp$iter$5$$.next()) {
        $$jscomp$key$partSelector$$.value.color = $color$jscomp$9$$;
      }
    }), $initialColor$$ = $initialColorValue$$($part$jscomp$6$$);
    null != $initialColor$$ && ($toggle$$.checked = !0, $selector$jscomp$3$$.color = $initialColor$$);
    $toggle$$.addEventListener("change", function() {
      $selector$jscomp$3$$.enabled = $toggle$$.checked;
    });
    $selector$jscomp$3$$.$draw$();
    return $selector$jscomp$3$$;
  }
  if (null == $stendhal$$.$ui$.$OutfitDialog$.instance) {
    var $self$jscomp$4$$ = this, $PartSelector$$ = function($canvas$jscomp$7_part$jscomp$2$$, $initialIndex$$, $maxindex$$, $onPartChanged$$) {
      this.$H$ = $canvas$jscomp$7_part$jscomp$2$$;
      this.$C$ = $onPartChanged$$;
      this.$v$ = $initialIndex$$;
      this.$B$ = $maxindex$$;
      $canvas$jscomp$7_part$jscomp$2$$ = document.getElementById("setoutfit" + $canvas$jscomp$7_part$jscomp$2$$ + "canvas");
      $canvas$jscomp$7_part$jscomp$2$$.style.margin = "5px";
      this.$A$ = $canvas$jscomp$7_part$jscomp$2$$.getContext("2d");
      this.$I$ = $canvas$jscomp$7_part$jscomp$2$$.width;
      this.$G$ = $canvas$jscomp$7_part$jscomp$2$$.height;
      this.$_image$ = this.$D$ = null;
    };
    $PartSelector$$.prototype.$draw$ = function() {
      var $$jscomp$this$jscomp$1$$ = this, $image$jscomp$16$$ = this.$F$(this.$H$, this.$v$, this.$D$);
      this.$_image$ = $image$jscomp$16$$;
      this.$A$.fillStyle = "white";
      this.$A$.fillRect(0, 0, this.$I$, this.$G$);
      $image$jscomp$16$$.then(function($img$jscomp$7$$) {
        $$jscomp$this$jscomp$1$$.$A$.drawImage($img$jscomp$7$$, -48, -128);
        $$jscomp$this$jscomp$1$$.$C$();
      });
    };
    $PartSelector$$.prototype.$J$ = function() {
      var $numOutfits$$ = this.$B$ + 1;
      this.$v$ += this.$B$;
      this.$v$ %= $numOutfits$$;
      this.$draw$();
      this.$C$();
    };
    $PartSelector$$.prototype.next = function() {
      var $numOutfits$jscomp$1$$ = this.$B$ + 1;
      this.$v$++;
      this.$v$ %= $numOutfits$jscomp$1$$;
      this.$draw$();
      this.$C$();
    };
    $PartSelector$$.prototype.$F$ = function($fname_part$jscomp$3$$, $index$jscomp$78$$, $color$jscomp$7$$) {
      $color$jscomp$7$$ = void 0 === $color$jscomp$7$$ ? null : $color$jscomp$7$$;
      $fname_part$jscomp$3$$ = "/data/sprites/outfit/" + $fname_part$jscomp$3$$ + "/" + $fname_part$jscomp$3$$ + "_" + $indexString$$($index$jscomp$78$$) + ".png";
      return null != $color$jscomp$7$$ ? $stendhal$$.data.$sprites$.$getFilteredWithPromise$($fname_part$jscomp$3$$, "trueColor", $color$jscomp$7$$) : $stendhal$$.data.$sprites$.$getWithPromise$($fname_part$jscomp$3$$);
    };
    $$jscomp$global$$.Object.defineProperties($PartSelector$$.prototype, {image:{configurable:!0, enumerable:!0, get:function() {
      return this.$_image$;
    }}, index:{configurable:!0, enumerable:!0, get:function() {
      return $indexString$$(this.$v$);
    }, set:function($newIndex$$) {
      this.$v$ = $newIndex$$;
      this.$draw$();
    }}, color:{configurable:!0, enumerable:!0, set:function($newColor$$) {
      this.$D$ = $newColor$$;
      this.$draw$();
    }}});
    var $ColorSelector$$ = function($canvas$jscomp$8$$, $gradientCanvas$$, $onColorChanged$$) {
      var $$jscomp$this$jscomp$2$$ = this;
      this.$ctx$ = $canvas$jscomp$8$$.getContext("2d");
      this.$D$ = $gradientCanvas$$.getContext("2d");
      this.$v$ = this.$M$($canvas$jscomp$8$$.width, $canvas$jscomp$8$$.height);
      this.$K$ = $onColorChanged$$;
      this.$O$ = !1;
      this.x = this.$v$.width / 2;
      this.y = this.$v$.height / 2;
      this.$A$ = this.x;
      $canvas$jscomp$8$$.addEventListener("mousedown", function($e$jscomp$67$$) {
        return $$jscomp$this$jscomp$2$$.$P$($e$jscomp$67$$);
      });
      $canvas$jscomp$8$$.addEventListener("mousemove", function($e$jscomp$68$$) {
        return $$jscomp$this$jscomp$2$$.$U$($e$jscomp$68$$);
      });
      $gradientCanvas$$.addEventListener("mousedown", function($e$jscomp$69$$) {
        return $$jscomp$this$jscomp$2$$.$R$($e$jscomp$69$$);
      });
      $gradientCanvas$$.addEventListener("mousemove", function($e$jscomp$70$$) {
        return $$jscomp$this$jscomp$2$$.$V$($e$jscomp$70$$);
      });
      $gradientCanvas$$.style.margin = "5px 0px 0px 0px";
    };
    $ColorSelector$$.prototype.$M$ = function($width$jscomp$32$$, $height$jscomp$30$$) {
      var $img$jscomp$8$$ = document.createElement("canvas");
      $img$jscomp$8$$.width = $width$jscomp$32$$;
      $img$jscomp$8$$.height = $height$jscomp$30$$;
      for (var $ctx$jscomp$38$$ = $img$jscomp$8$$.getContext("2d"), $x$jscomp$153$$ = 0; $x$jscomp$153$$ < $width$jscomp$32$$; $x$jscomp$153$$++) {
        for (var $y$jscomp$135$$ = 0; $y$jscomp$135$$ < $height$jscomp$30$$; $y$jscomp$135$$++) {
          $ctx$jscomp$38$$.fillStyle = this.$C$($stendhal$$.data.$sprites$.filter.$hsl2rgb$([$x$jscomp$153$$ / $width$jscomp$32$$, 1 - $y$jscomp$135$$ / $height$jscomp$30$$, 0.5])), $ctx$jscomp$38$$.fillRect($x$jscomp$153$$, $y$jscomp$135$$, 1, 1);
        }
      }
      return $img$jscomp$8$$;
    };
    $ColorSelector$$.prototype.$C$ = function($rgb$jscomp$4$$) {
      return "rgb(".concat($rgb$jscomp$4$$[0], ",", $rgb$jscomp$4$$[1], ",", $rgb$jscomp$4$$[2], ")");
    };
    $ColorSelector$$.prototype.$draw$ = function() {
      this.enabled ? (this.$ctx$.drawImage(this.$v$, 0, 0), this.$N$()) : (this.$ctx$.fillStyle = "gray", this.$ctx$.fillRect(0, 0, this.$v$.width, this.$v$.height));
      this.$T$();
    };
    $ColorSelector$$.prototype.$N$ = function() {
      this.$ctx$.strokeStyle = "black";
      this.$ctx$.beginPath();
      this.$ctx$.moveTo(this.x, 0);
      this.$ctx$.lineTo(this.x, this.$v$.height);
      this.$ctx$.moveTo(0, this.y);
      this.$ctx$.lineTo(this.$v$.width, this.y);
      this.$ctx$.stroke();
    };
    $ColorSelector$$.prototype.$T$ = function() {
      if (this.enabled) {
        var $gradient$$ = this.$D$.createLinearGradient(0, 0, this.$v$.width, 0), $ctx$jscomp$39_stops$$ = this.$L$();
        $gradient$$.addColorStop(0, this.$C$($ctx$jscomp$39_stops$$[0]));
        $gradient$$.addColorStop(0.5, this.$C$($ctx$jscomp$39_stops$$[1]));
        $gradient$$.addColorStop(1, this.$C$($ctx$jscomp$39_stops$$[2]));
        $ctx$jscomp$39_stops$$ = this.$D$;
        $ctx$jscomp$39_stops$$.fillStyle = $gradient$$;
        $ctx$jscomp$39_stops$$.fillRect(0, 0, this.$v$.width, 10);
        $ctx$jscomp$39_stops$$.fillStyle = "black";
        $ctx$jscomp$39_stops$$.beginPath();
        $ctx$jscomp$39_stops$$.moveTo(this.$A$, 0);
        $ctx$jscomp$39_stops$$.lineTo(this.$A$, 10);
        $ctx$jscomp$39_stops$$.stroke();
      } else {
        this.$D$.fillStyle = "gray", this.$D$.fillRect(0, 0, this.$v$.width, 10);
      }
    };
    $ColorSelector$$.prototype.$L$ = function() {
      var $width$jscomp$33$$ = this.$v$.width, $height$jscomp$31$$ = this.$v$.height;
      return [$stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.x / $width$jscomp$33$$, 1 - this.y / $height$jscomp$31$$, 0.08]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.x / $width$jscomp$33$$, 1 - this.y / $height$jscomp$31$$, 0.5]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.x / $width$jscomp$33$$, 1 - this.y / $height$jscomp$31$$, 0.92])];
    };
    $ColorSelector$$.prototype.$P$ = function($event$jscomp$15$$) {
      this.enabled && (this.x = $event$jscomp$15$$.offsetX, this.y = $event$jscomp$15$$.offsetY, this.$draw$(), this.$K$(this.color));
    };
    $ColorSelector$$.prototype.$U$ = function($event$jscomp$16$$) {
      $event$jscomp$16$$.buttons && this.$P$($event$jscomp$16$$);
    };
    $ColorSelector$$.prototype.$R$ = function($event$jscomp$17$$) {
      this.enabled && (this.$A$ = $event$jscomp$17$$.offsetX, this.$draw$(), this.$K$(this.color));
    };
    $ColorSelector$$.prototype.$V$ = function($event$jscomp$18$$) {
      $event$jscomp$18$$.buttons && this.$R$($event$jscomp$18$$);
    };
    $$jscomp$global$$.Object.defineProperties($ColorSelector$$.prototype, {enabled:{configurable:!0, enumerable:!0, set:function($value$jscomp$113$$) {
      this.$O$ = $value$jscomp$113$$ ? !0 : !1;
      this.$draw$();
      this.$K$(this.color);
    }, get:function() {
      return this.$O$;
    }}, color:{configurable:!0, enumerable:!0, get:function() {
      return this.enabled ? $stendhal$$.data.$sprites$.filter.$mergergb$($stendhal$$.data.$sprites$.filter.$hsl2rgb$([this.x / this.$v$.width, 1 - this.y / this.$v$.height, this.$A$ / this.$v$.width])) : null;
    }, set:function($hsl$jscomp$4_rgb$jscomp$5$$) {
      null != $hsl$jscomp$4_rgb$jscomp$5$$ ? ($hsl$jscomp$4_rgb$jscomp$5$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$($stendhal$$.data.$sprites$.filter.$splitrgb$($hsl$jscomp$4_rgb$jscomp$5$$)), this.x = $hsl$jscomp$4_rgb$jscomp$5$$[0] * this.$v$.width, this.y = (1 - $hsl$jscomp$4_rgb$jscomp$5$$[1]) * this.$v$.height, this.$A$ = $hsl$jscomp$4_rgb$jscomp$5$$[2] * this.$v$.width, this.enabled = !0) : this.enabled = !1;
    }}});
    var $PaletteColorSelector$$ = function($$jscomp$super$this_canvas$jscomp$9$$, $gradientCanvas$jscomp$1$$, $onColorChanged$jscomp$1$$) {
      $$jscomp$super$this_canvas$jscomp$9$$ = $ColorSelector$$.call(this, $$jscomp$super$this_canvas$jscomp$9$$, $gradientCanvas$jscomp$1$$, $onColorChanged$jscomp$1$$) || this;
      $$jscomp$super$this_canvas$jscomp$9$$.$G$ = $$jscomp$super$this_canvas$jscomp$9$$.$v$.width / 4;
      $$jscomp$super$this_canvas$jscomp$9$$.$F$ = $$jscomp$super$this_canvas$jscomp$9$$.$v$.height / 4;
      $$jscomp$super$this_canvas$jscomp$9$$.$I$ = 0;
      $$jscomp$super$this_canvas$jscomp$9$$.$J$ = 0;
      $$jscomp$super$this_canvas$jscomp$9$$.$A$ = $$jscomp$super$this_canvas$jscomp$9$$.$v$.width / 2;
      $$jscomp$super$this_canvas$jscomp$9$$.$H$ = $$jscomp$super$this_canvas$jscomp$9$$.$H$;
      $$jscomp$super$this_canvas$jscomp$9$$.$B$ = $$jscomp$super$this_canvas$jscomp$9$$.$B$;
      return $$jscomp$super$this_canvas$jscomp$9$$;
    };
    $$jscomp$inherits$$($PaletteColorSelector$$, $ColorSelector$$);
    $PaletteColorSelector$$.prototype.$S$ = function() {
      for (var $hues$$ = [0.05, 0.07, 0.09, 0.11], $saturations$$ = [0.70, 0.55, 0.40, 0.25], $hsMap$$ = [[], [], [], []], $colors$jscomp$1$$ = [[], [], [], []], $i$jscomp$79$$ = 0; 4 > $i$jscomp$79$$; $i$jscomp$79$$++) {
        for (var $j$jscomp$4$$ = 0; 4 > $j$jscomp$4$$; $j$jscomp$4$$++) {
          var $hue$jscomp$2$$ = $hues$$[$j$jscomp$4$$], $sat$$ = $saturations$$[$i$jscomp$79$$];
          $hsMap$$[$i$jscomp$79$$].push([$hue$jscomp$2$$, $sat$$]);
          $colors$jscomp$1$$[$i$jscomp$79$$].push($stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hue$jscomp$2$$, $sat$$, 0.5]));
        }
      }
      this.$B$ = $hsMap$$;
      this.$H$ = $colors$jscomp$1$$;
    };
    $PaletteColorSelector$$.prototype.$M$ = function($blockWidth_width$jscomp$34$$, $blockHeight_height$jscomp$32$$) {
      this.$S$();
      var $img$jscomp$9$$ = document.createElement("canvas");
      $img$jscomp$9$$.width = $blockWidth_width$jscomp$34$$;
      $img$jscomp$9$$.height = $blockHeight_height$jscomp$32$$;
      var $ctx$jscomp$40$$ = $img$jscomp$9$$.getContext("2d");
      $blockWidth_width$jscomp$34$$ /= 4;
      $blockHeight_height$jscomp$32$$ /= 4;
      for (var $x$jscomp$154$$ = 0; 4 > $x$jscomp$154$$; $x$jscomp$154$$++) {
        for (var $y$jscomp$136$$ = 0; 4 > $y$jscomp$136$$; $y$jscomp$136$$++) {
          $ctx$jscomp$40$$.fillStyle = this.$C$(this.$H$[$x$jscomp$154$$][$y$jscomp$136$$]), $ctx$jscomp$40$$.fillRect($x$jscomp$154$$ * $blockWidth_width$jscomp$34$$, $y$jscomp$136$$ * $blockHeight_height$jscomp$32$$, $blockWidth_width$jscomp$34$$, $blockHeight_height$jscomp$32$$);
        }
      }
      return $img$jscomp$9$$;
    };
    $PaletteColorSelector$$.prototype.$L$ = function() {
      var $hs$$ = this.$B$[this.x][this.y];
      return [$stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$$[0], $hs$$[1], 0.08]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$$[0], $hs$$[1], 0.5]), $stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$$[0], $hs$$[1], 0.92])];
    };
    $PaletteColorSelector$$.prototype.$N$ = function() {
      this.$ctx$.strokeStyle = "white";
      this.$ctx$.strokeRect(this.x * this.$G$, this.y * this.$F$, this.$G$, this.$F$);
    };
    $$jscomp$global$$.Object.defineProperties($PaletteColorSelector$$.prototype, {x:{configurable:!0, enumerable:!0, set:function($newX$$) {
      this.$I$ = Math.floor($newX$$ / this.$G$);
    }, get:function() {
      return this.$I$;
    }}, y:{configurable:!0, enumerable:!0, set:function($newY$$) {
      this.$J$ = Math.floor($newY$$ / this.$F$);
    }, get:function() {
      return this.$J$;
    }}, color:{configurable:!0, enumerable:!0, get:function() {
      if (this.enabled) {
        var $hs$jscomp$1$$ = this.$B$[this.x][this.y];
        return $stendhal$$.data.$sprites$.filter.$mergergb$($stendhal$$.data.$sprites$.filter.$hsl2rgb$([$hs$jscomp$1$$[0], $hs$jscomp$1$$[1], this.$A$ / this.$v$.width]));
      }
      return null;
    }, set:function($hsl$jscomp$6_rgb$jscomp$7$$) {
      if (null != $hsl$jscomp$6_rgb$jscomp$7$$) {
        this.enabled = !0;
        $hsl$jscomp$6_rgb$jscomp$7$$ = $stendhal$$.data.$sprites$.filter.$rgb2hsl$($stendhal$$.data.$sprites$.filter.$splitrgb$($hsl$jscomp$6_rgb$jscomp$7$$));
        this.$A$ = $hsl$jscomp$6_rgb$jscomp$7$$[2] * this.$v$.width;
        for (var $bestDelta$$ = Number.MAX_VALUE, $i$jscomp$80$$ = 0; 4 > $i$jscomp$80$$; $i$jscomp$80$$++) {
          for (var $j$jscomp$5$$ = 0; 4 > $j$jscomp$5$$; $j$jscomp$5$$++) {
            var $hs$jscomp$2_satDelta$$ = this.$B$[$i$jscomp$80$$][$j$jscomp$5$$], $delta$jscomp$3_hueDelta$$ = $hs$jscomp$2_satDelta$$[0] - $hsl$jscomp$6_rgb$jscomp$7$$[0];
            $hs$jscomp$2_satDelta$$ = $hs$jscomp$2_satDelta$$[1] - $hsl$jscomp$6_rgb$jscomp$7$$[1];
            $delta$jscomp$3_hueDelta$$ = $delta$jscomp$3_hueDelta$$ * $delta$jscomp$3_hueDelta$$ + $hs$jscomp$2_satDelta$$ * $hs$jscomp$2_satDelta$$;
            $delta$jscomp$3_hueDelta$$ < $bestDelta$$ && ($bestDelta$$ = $delta$jscomp$3_hueDelta$$, this.$I$ = $i$jscomp$80$$, this.$J$ = $j$jscomp$5$$);
          }
        }
      } else {
        this.enabled = !1;
      }
    }}});
    $self$jscomp$4$$.$popup$ = new $stendhal$ui$Popup$$("Set outfit", "<div class='background'><div class='horizontalgroup'><div class='verticalgroup'><div class='horizontalgroup'><button type='button' id='setoutfitprevhair'>&lt;</button><canvas id='setoutfithaircanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnexthair'>&gt;</button></div><div class='horizontalgroup'><button type='button' id='setoutfitpreveyes'>&lt;</button><canvas id='setoutfiteyescanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnexteyes'>&gt;</button><br></div><div class='horizontalgroup'><button type='button' id='setoutfitprevmouth'>&lt;</button><canvas id='setoutfitmouthcanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnextmouth'>&gt;</button><br></div><div class='horizontalgroup'><button type='button' id='setoutfitprevhead'>&lt;</button><canvas id='setoutfitheadcanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnexthead'>&gt;</button><br></div><div class='horizontalgroup'><button type='button' id='setoutfitprevbody'>&lt;</button><canvas id='setoutfitbodycanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnextbody'>&gt;</button><br></div><div class='horizontalgroup'><button type='button' id='setoutfitprevdress'>&lt;</button><canvas id='setoutfitdresscanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnextdress'>&gt;</button></div></div><div class='verticalgroup'><div class='verticalgroup'><div class='horizontalgroup'><input type='checkbox' id='setoutfithaircolortoggle'><label for='setoutfithaircolortoggle'>Hair color</label></div><canvas id='setoutfithaircolorcanvas' width='80' height='52'></canvas><canvas id='setoutfithaircolorgradient' width='80' height='10'></canvas></div><div class='verticalgroup'><div class='horizontalgroup'><input type='checkbox' id='setoutfiteyescolortoggle'><label for='setoutfiteyescolortoggle'>Eyes color</label></div><canvas id='setoutfiteyescolorcanvas' width='80' height='52'></canvas><canvas id='setoutfiteyescolorgradient' width='80' height='10'></canvas></div><div class='verticalgroup'><div class='horizontalgroup'><input type='checkbox' id='setoutfitskincolortoggle'><label for='setoutfitskincolortoggle'>Skin color</label></div><canvas id='setoutfitskincolorcanvas' width='80' height='52'></canvas><canvas id='setoutfitskincolorgradient' width='80' height='10'></canvas></div><div class='verticalgroup'><div class='horizontalgroup'><input type='checkbox' id='setoutfitdresscolortoggle'><label for='setoutfitdresscolortoggle'>Dress color</label></div><canvas id='setoutfitdresscolorcanvas' width='80' height='52'></canvas><canvas id='setoutfitdresscolorgradient' width='80' height='10'></canvas></div></div><div class='verticalgroup'><div class='horizontalgroup'><button type='button' id='setoutfitprevhat'>&lt;</button><canvas id='setoutfithatcanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnexthat'>&gt;</button></div><div class='horizontalgroup'><button type='button' id='setoutfitprevmask'>&lt;</button><canvas id='setoutfitmaskcanvas' width='48' height='64'></canvas><button type='button' id='setoutfitnextmask'>&gt;</button></div></div><div><canvas id='setoutfitcompositecanvas' width='48' height='64'></canvas></div></div><div align='right'><button type='button' id='setoutfitcancel'>Cancel</button><button type='button' id='setoutfitapply'>Change Outfit</button></div></div>", 
    300, 200);
    $self$jscomp$4$$.$popup$.$v$ = function() {
      $stendhal$$.$ui$.$OutfitDialog$.instance = null;
    };
    $stendhal$$.$ui$.$OutfitDialog$.instance = $self$jscomp$4$$;
    for (var $entries$$ = $marauroa$$.$me$.outfit_ext.split(","), $currentOutfit$$ = {}, $i$jscomp$78$$ = 0; $i$jscomp$78$$ < $entries$$.length; $i$jscomp$78$$++) {
      var $entry$jscomp$1$$ = $entries$$[$i$jscomp$78$$].split("=");
      $currentOutfit$$[$entry$jscomp$1$$[0]] = $entry$jscomp$1$$[1];
    }
    var $hatSelector$$ = $makeSelector$$("hat", $currentOutfit$$.hat, $partChanged$$), $hairSelector$$ = $makeSelector$$("hair", $currentOutfit$$.hair, $partChanged$$), $maskSelector$$ = $makeSelector$$("mask", $currentOutfit$$.mask, $partChanged$$), $eyesSelector$$ = $makeSelector$$("eyes", $currentOutfit$$.eyes, $partChanged$$), $mouthSelector$$ = $makeSelector$$("mouth", $currentOutfit$$.mouth, $partChanged$$), $headSelector$$ = $makeSelector$$("head", $currentOutfit$$.head, $partChanged$$), $bodySelector$$ = 
    $makeSelector$$("body", $currentOutfit$$.body, $partChanged$$), $dressSelector$$ = $makeSelector$$("dress", $currentOutfit$$.dress, $partChanged$$), $hairColorSelector$$ = $createColorSelector$$($ColorSelector$$, "hair", $hairSelector$$), $eyesColorSelector$$ = $createColorSelector$$($ColorSelector$$, "eyes", $eyesSelector$$), $dressColorSelector$$ = $createColorSelector$$($ColorSelector$$, "dress", $dressSelector$$), $skinColorSelector$$ = $createColorSelector$$($PaletteColorSelector$$, "skin", 
    $headSelector$$, $bodySelector$$);
    $drawComposite$$();
    document.getElementById("setoutfitcancel").addEventListener("click", function() {
      $self$jscomp$4$$.$popup$.close();
    });
    document.getElementById("setoutfitapply").addEventListener("click", function() {
      var $action$jscomp$63$$ = {type:"outfit_ext", zone:$marauroa$$.$currentZoneName$, value:"body=" + $bodySelector$$.index.toString() + ",dress=" + $dressSelector$$.index.toString() + ",head=" + $headSelector$$.index.toString() + ",mouth=" + $mouthSelector$$.index.toString() + ",eyes=" + $eyesSelector$$.index.toString() + ",mask=" + $maskSelector$$.index.toString() + ",hair=" + $hairSelector$$.index.toString() + ",hat=" + $hatSelector$$.index.toString()}, $color$jscomp$10$$ = $hairColorSelector$$.color;
      null != $color$jscomp$10$$ && ($action$jscomp$63$$.hair = $color$jscomp$10$$.toString());
      $color$jscomp$10$$ = $eyesColorSelector$$.color;
      null != $color$jscomp$10$$ && ($action$jscomp$63$$.eyes = $color$jscomp$10$$.toString());
      $color$jscomp$10$$ = $dressColorSelector$$.color;
      null != $color$jscomp$10$$ && ($action$jscomp$63$$.dress = $color$jscomp$10$$.toString());
      $color$jscomp$10$$ = $skinColorSelector$$.color;
      null != $color$jscomp$10$$ && ($action$jscomp$63$$.skin = $color$jscomp$10$$.toString());
      $marauroa$$.$clientFramework$.$sendAction$($action$jscomp$63$$);
      $self$jscomp$4$$.$popup$.close();
    });
  }
};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$travellog$ = {$currentProgressType$:"", open:function($dataItems$jscomp$1$$) {
  document.getElementById("tavellogpopup") || $stendhal$$.$ui$.$travellog$.$createWindow$($dataItems$jscomp$1$$);
  $stendhal$$.$ui$.$travellog$.$currentProgressType$ = $dataItems$jscomp$1$$[0];
  $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$stendhal$$.$ui$.$travellog$.$currentProgressType$});
}, $progressTypeData$:function($progressType$jscomp$1$$, $dataItems$jscomp$2$$) {
  if ($progressType$jscomp$1$$ === $stendhal$$.$ui$.$travellog$.$currentProgressType$) {
    for (var $html$jscomp$2$$ = "", $i$jscomp$81$$ = 0; $i$jscomp$81$$ < $dataItems$jscomp$2$$.length; $i$jscomp$81$$++) {
      $html$jscomp$2$$ += '<option value="' + $stendhal$$.$ui$.$html$.$esc$($dataItems$jscomp$2$$[$i$jscomp$81$$]) + '">' + $stendhal$$.$ui$.$html$.$esc$($dataItems$jscomp$2$$[$i$jscomp$81$$]) + "</option>";
    }
    document.getElementById("travellogitems").innerHTML = $html$jscomp$2$$;
    $dataItems$jscomp$2$$ && (document.getElementById("travellogitems").value = $dataItems$jscomp$2$$[0], $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$progressType$jscomp$1$$, item:$dataItems$jscomp$2$$[0]}));
  }
}, $itemData$:function($html$jscomp$3_progressType$jscomp$2$$, $selectedItem$$, $description$jscomp$4_i$jscomp$82$$, $dataItems$jscomp$3$$) {
  $html$jscomp$3_progressType$jscomp$2$$ = "<h3>" + $stendhal$$.$ui$.$html$.$esc$($selectedItem$$) + "</h3>";
  $html$jscomp$3_progressType$jscomp$2$$ += '<p id="travellogdescription">' + $stendhal$$.$ui$.$html$.$esc$($description$jscomp$4_i$jscomp$82$$) + "</p>";
  $html$jscomp$3_progressType$jscomp$2$$ += "<ul>";
  for ($description$jscomp$4_i$jscomp$82$$ = 0; $description$jscomp$4_i$jscomp$82$$ < $dataItems$jscomp$3$$.length; $description$jscomp$4_i$jscomp$82$$++) {
    $html$jscomp$3_progressType$jscomp$2$$ += "<li>" + $stendhal$$.$ui$.$html$.$esc$($dataItems$jscomp$3$$[$description$jscomp$4_i$jscomp$82$$]);
  }
  document.getElementById("travellogdetails").innerHTML = $html$jscomp$3_progressType$jscomp$2$$ + "</ul>";
}, $createWindow$:function($dataItems$jscomp$4_popup$jscomp$1$$) {
  for (var $html$jscomp$4$$ = '<div class="tavellogpopup">', $i$jscomp$83$$ = 0; $i$jscomp$83$$ < $dataItems$jscomp$4_popup$jscomp$1$$.length; $i$jscomp$83$$++) {
    $html$jscomp$4$$ += '<button id="' + $stendhal$$.$ui$.$html$.$esc$($dataItems$jscomp$4_popup$jscomp$1$$[$i$jscomp$83$$]) + '" class="progressTypeButton">' + $stendhal$$.$ui$.$html$.$esc$($dataItems$jscomp$4_popup$jscomp$1$$[$i$jscomp$83$$]) + "</button>";
  }
  $dataItems$jscomp$4_popup$jscomp$1$$ = new $stendhal$ui$Popup$$("Travel Log", $html$jscomp$4$$ + '<div><select id="travellogitems" size="20"></select><div id="travellogdetails"></div></div></div>', 160, 50);
  progressTypeButtons = document.querySelectorAll(".progressTypeButton").forEach(function($button$$) {
    $button$$.addEventListener("click", function($e$jscomp$77$$) {
      $stendhal$$.$ui$.$travellog$.$currentProgressType$ = $e$jscomp$77$$.target.id;
      $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$stendhal$$.$ui$.$travellog$.$currentProgressType$});
    });
  });
  document.getElementById("travellogitems").addEventListener("change", function($e$jscomp$78$$) {
    $marauroa$$.$clientFramework$.$sendAction$({type:"progressstatus", progress_type:$stendhal$$.$ui$.$travellog$.$currentProgressType$, item:$e$jscomp$78$$.target.value});
  });
  return $dataItems$jscomp$4_popup$jscomp$1$$;
}};
window.$v$ = window.$v$ || {};
window.$A$ = window.$A$ || {};
$stendhal$$.$ui$ = $stendhal$$.$ui$ || {};
$stendhal$$.$ui$.$settings$ = {$onOpenSettingsMenu$:function() {
  console.log("FIXME: not yet functional");
}};
$marauroa$$ = window.$v$ = window.$v$ || {};
$stendhal$$ = window.$A$ = window.$A$ || {};
$stendhal$$.$main$ = {$errorCounter$:0, $zoneFile$:null, loaded:!1, $onDataMap$:function($data$jscomp$88$$) {
  var $zoneinfo$$ = {};
  $marauroa$$.$Deserializer$.$fromBase64$($data$jscomp$88$$).$readAttributes$($zoneinfo$$);
  document.getElementById("zoneinfo").textContent = $zoneinfo$$.readable_name;
  $stendhal$$.$main$.$zoneFile$ = $zoneinfo$$.file;
}, $registerMarauroaEventHandlers$:function() {
  $marauroa$$.$clientFramework$.$onDisconnect$ = function($reason$jscomp$13$$, $error$jscomp$9$$) {
    $stendhal$$.$ui$.$chatLog$.$addLine$("error", "Disconnected: " + $error$jscomp$9$$);
  };
  $marauroa$$.$clientFramework$.$onLoginRequired$ = function() {
    window.location = "/index.php?id=content/account/login&url=" + escape(window.location.pathname + window.location.hash);
  };
  $marauroa$$.$clientFramework$.$onLoginFailed$ = function() {
    alert("Login failed. Please login on the Stendhal website first and make sure you open the client on an https://-URL");
    $marauroa$$.$clientFramework$.close();
    document.getElementById("chatinput").disabled = !0;
    document.getElementById("chat").style.backgroundColor = "#AAA";
  };
  $marauroa$$.$clientFramework$.$onAvailableCharacterDetails$ = function($characters$jscomp$1$$) {
    if (window.location.hash) {
      var $name$jscomp$84$$ = window.location.hash.substring(1);
    } else {
      $name$jscomp$84$$ = $marauroa$$.$util$.first($characters$jscomp$1$$).a.name;
      var $admin$$ = 0, $i$jscomp$84$$;
      for ($i$jscomp$84$$ in $characters$jscomp$1$$) {
        $characters$jscomp$1$$.hasOwnProperty($i$jscomp$84$$) && $characters$jscomp$1$$[$i$jscomp$84$$].a.adminlevel > $admin$$ && ($admin$$ = $characters$jscomp$1$$[$i$jscomp$84$$].a.adminlevel, $name$jscomp$84$$ = $characters$jscomp$1$$[$i$jscomp$84$$].a.name);
      }
    }
    $marauroa$$.$clientFramework$.$chooseCharacter$($name$jscomp$84$$);
    document.getElementById("body").style.cursor = "auto";
    $stendhal$$.$ui$.$chatLog$.$addLine$("client", "Loading world...");
  };
  $marauroa$$.$clientFramework$.$onTransferREQ$ = function($items$jscomp$5$$) {
    for (var $i$jscomp$85$$ in $items$jscomp$5$$) {
      "undefined" != typeof $items$jscomp$5$$[$i$jscomp$85$$].name && ($items$jscomp$5$$[$i$jscomp$85$$].ack = !0);
    }
  };
  $marauroa$$.$clientFramework$.$onTransfer$ = function($items$jscomp$6$$) {
    var $data$jscomp$89$$ = {}, $zoneName$jscomp$1$$ = "", $i$jscomp$86$$;
    for ($i$jscomp$86$$ in $items$jscomp$6$$) {
      var $name$jscomp$85$$ = $items$jscomp$6$$[$i$jscomp$86$$].name;
      $zoneName$jscomp$1$$ = $name$jscomp$85$$.substring(0, $name$jscomp$85$$.indexOf("."));
      $name$jscomp$85$$ = $name$jscomp$85$$.substring($name$jscomp$85$$.indexOf(".") + 1);
      $data$jscomp$89$$[$name$jscomp$85$$] = $items$jscomp$6$$[$i$jscomp$86$$].data;
      "data_map" === $name$jscomp$85$$ && $stendhal$$.$main$.$onDataMap$($items$jscomp$6$$[$i$jscomp$86$$].data);
    }
    $stendhal$$.data.map.$onTransfer$($zoneName$jscomp$1$$, $data$jscomp$89$$);
  };
  document.getElementById("gamewindow") && ($marauroa$$.$perceptionListener$.$onPerceptionEnd$ = function() {
    $stendhal$$.$zone$.$sortEntities$();
    $stendhal$$.$ui$.$minimap$.$draw$();
    $stendhal$$.$ui$.$buddyList$.update();
    $stendhal$$.$ui$.$equip$.update();
    $stendhal$$.$ui$.$stats$.update();
    $stendhal$$.$main$.loaded || ($stendhal$$.$main$.loaded = !0, setTimeout(function() {
      document.getElementById("client").style.display = "block";
      document.getElementById("loginpopup").style.display = "none";
    }, 300));
  });
}, $toggleSound$:function() {
  $stendhal$$.$config$.$sound$.play = !$stendhal$$.$config$.$sound$.play;
  $stendhal$$.$main$.$onSoundToggled$();
}, $onSoundToggled$:function() {
  document.getElementById("soundbutton").textContent = $stendhal$$.$config$.$sound$.play ? "\ud83d\udd0a" : "\ud83d\udd07";
}, $registerBrowserEventHandlers$:function() {
  document.addEventListener("keydown", $stendhal$$.$ui$.$keyhandler$.$onKeyDown$);
  document.addEventListener("keyup", $stendhal$$.$ui$.$keyhandler$.$onKeyUp$);
  document.addEventListener("contextmenu", $stendhal$$.$main$.$preventContextMenu$);
  var $buddyList_chatinput$jscomp$1_gamewindow_minimap$$ = document.getElementById("gamewindow");
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.setAttribute("draggable", !0);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("mousedown", $stendhal$$.$ui$.$gamewindow$.$onMouseDown$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("touchstart", $stendhal$$.$ui$.$gamewindow$.$onMouseDown$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("dblclick", $stendhal$$.$ui$.$gamewindow$.$onMouseDown$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("dragstart", $stendhal$$.$ui$.$gamewindow$.$onDragStart$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("mousemove", $stendhal$$.$ui$.$gamewindow$.$onMouseMove$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("touchmove", $stendhal$$.$ui$.$gamewindow$.$onMouseMove$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("dragover", $stendhal$$.$ui$.$gamewindow$.$onDragOver$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("drop", $stendhal$$.$ui$.$gamewindow$.$onDrop$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("contextmenu", $stendhal$$.$ui$.$gamewindow$.$onContentMenu$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$ = document.getElementById("minimap");
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("click", $stendhal$$.$ui$.$minimap$.$onClick$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("dblclick", $stendhal$$.$ui$.$minimap$.$onClick$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$ = document.getElementById("buddyList");
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("mouseup", $stendhal$$.$ui$.$buddyList$.$onMouseUp$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("contextmenu", $stendhal$$.$ui$.$gamewindow$.$onContentMenu$);
  document.getElementById("menubutton").addEventListener("click", $stendhal$$.$ui$.$menu$.$onOpenAppMenu$);
  document.getElementById("soundbutton").addEventListener("click", $stendhal$$.$main$.$toggleSound$);
  $stendhal$$.$main$.$onSoundToggled$();
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$ = document.getElementById("chatinput");
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("keydown", $stendhal$$.$ui$.$chatinput$.$onKeyDown$);
  $buddyList_chatinput$jscomp$1_gamewindow_minimap$$.addEventListener("keypress", $stendhal$$.$ui$.$chatinput$.$onKeyPress$);
}, $devWarning$:function() {
  console.log("%c ", "padding: 30px; background: url(" + window.location.protocol + "://" + window.location.host + "/images/buttons/devtools-warning.png) no-repeat; color: #AF0");
  console.log("%cIf someone told you, to copy and paste something here, it's a scam and will give them access to your account.", "color:#A00; background-color:#FFF");
  console.log("If you are a developer and curious about Stendhal, have a look at https://stendhalgame.org/development/introduction.html to get the source code. And perhaps, contribute a feature or a bugfix. ");
  console.log("");
  console.log("");
  window.eval = void 0;
}, $startup$:function() {
  $stendhal$$.$main$.$devWarning$();
  $stendhal$$.$ui$.$chatLog$.$addLine$("error", "This is an early stage of an experimental web-based client. Please use the official client at https://stendhalgame.org to play Stendhal.");
  $stendhal$$.$ui$.$chatLog$.$addLine$("client", "Client loaded. Connecting...");
  $stendhal$$.$main$.$registerMarauroaEventHandlers$();
  $stendhal$$.$main$.$registerBrowserEventHandlers$();
  $marauroa$$.$clientFramework$.connect(null, null);
  document.getElementById("gamewindow") && $stendhal$$.$ui$.$gamewindow$.$draw$.apply($stendhal$$.$ui$.$gamewindow$, arguments);
}, onerror:function($error$jscomp$10$$) {
  $stendhal$$.$main$.$errorCounter$++;
  if (5 < $stendhal$$.$main$.$errorCounter$) {
    console.log("Too many errors, stopped reporting");
  } else {
    var $text$jscomp$22$$ = $error$jscomp$10$$.message + "\r\n";
    $text$jscomp$22$$ += $error$jscomp$10$$.filename + ":" + $error$jscomp$10$$.lineno;
    $error$jscomp$10$$.colno && ($text$jscomp$22$$ += ":" + $error$jscomp$10$$.colno);
    $error$jscomp$10$$.error && ($text$jscomp$22$$ += "\r\n" + $error$jscomp$10$$.error.stack);
    $text$jscomp$22$$ += "\r\n" + window.navigator.userAgent;
    try {
      console.log($text$jscomp$22$$), $marauroa$$.$clientFramework$.$sendAction$({type:"report_error", text:$text$jscomp$22$$,});
    } catch ($e$10$$) {
    }
    return !0;
  }
}, $preventContextMenu$:function($event$jscomp$19$$) {
  $event$jscomp$19$$.preventDefault();
}};
document.addEventListener("DOMContentLoaded", $stendhal$$.$main$.$startup$);
window.addEventListener("error", $stendhal$$.$main$.onerror);


//# sourceMappingURL=stendhal-compiled.js.map
		