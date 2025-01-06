#[allow(dead_code)]
pub mod exports {
    #[allow(dead_code)]
    pub mod tanishiking {
        #[allow(dead_code)]
        pub mod test {
            #[allow(dead_code, clippy::all)]
            pub mod test {
                #[used]
                #[doc(hidden)]
                static __FORCE_SECTION_REF: fn() = super::super::super::super::__link_custom_section_describing_imports;
                use super::super::super::super::_rt;
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn _export_add_cabi<T: Guest>(arg0: i32, arg1: i32) -> i32 {
                    #[cfg(target_arch = "wasm32")] _rt::run_ctors_once();
                    let result0 = T::add(arg0, arg1);
                    _rt::as_i32(result0)
                }
                pub trait Guest {
                    fn add(a: i32, b: i32) -> i32;
                }
                #[doc(hidden)]
                macro_rules! __export_tanishiking_test_test_0_0_1_cabi {
                    ($ty:ident with_types_in $($path_to_types:tt)*) => {
                        const _ : () = { #[export_name =
                        "tanishiking:test/test@0.0.1#add"] unsafe extern "C" fn
                        export_add(arg0 : i32, arg1 : i32,) -> i32 { $($path_to_types)*::
                        _export_add_cabi::<$ty > (arg0, arg1) } };
                    };
                }
                #[doc(hidden)]
                pub(crate) use __export_tanishiking_test_test_0_0_1_cabi;
            }
        }
    }
}
mod _rt {
    #[cfg(target_arch = "wasm32")]
    pub fn run_ctors_once() {
        wit_bindgen_rt::run_ctors_once();
    }
    pub fn as_i32<T: AsI32>(t: T) -> i32 {
        t.as_i32()
    }
    pub trait AsI32 {
        fn as_i32(self) -> i32;
    }
    impl<'a, T: Copy + AsI32> AsI32 for &'a T {
        fn as_i32(self) -> i32 {
            (*self).as_i32()
        }
    }
    impl AsI32 for i32 {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
    impl AsI32 for u32 {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
    impl AsI32 for i16 {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
    impl AsI32 for u16 {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
    impl AsI32 for i8 {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
    impl AsI32 for u8 {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
    impl AsI32 for char {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
    impl AsI32 for usize {
        #[inline]
        fn as_i32(self) -> i32 {
            self as i32
        }
    }
}
/// Generates `#[no_mangle]` functions to export the specified type as the
/// root implementation of all generated traits.
///
/// For more information see the documentation of `wit_bindgen::generate!`.
///
/// ```rust
/// # macro_rules! export{ ($($t:tt)*) => (); }
/// # trait Guest {}
/// struct MyType;
///
/// impl Guest for MyType {
///     // ...
/// }
///
/// export!(MyType);
/// ```
#[allow(unused_macros)]
#[doc(hidden)]
macro_rules! __export_plug_impl {
    ($ty:ident) => {
        self::export!($ty with_types_in self);
    };
    ($ty:ident with_types_in $($path_to_types_root:tt)*) => {
        $($path_to_types_root)*::
        exports::tanishiking::test::test::__export_tanishiking_test_test_0_0_1_cabi!($ty
        with_types_in $($path_to_types_root)*:: exports::tanishiking::test::test);
    };
}
#[doc(inline)]
pub(crate) use __export_plug_impl as export;
#[cfg(target_arch = "wasm32")]
#[link_section = "component-type:wit-bindgen:0.31.0:tanishiking:test@0.0.1:plug:encoded world"]
#[doc(hidden)]
pub static __WIT_BINDGEN_COMPONENT_TYPE: [u8; 214] = *b"\
\0asm\x0d\0\x01\0\0\x19\x16wit-component-encoding\x04\0\x07\\\x01A\x02\x01A\x02\x01\
B\x02\x01@\x02\x01az\x01bz\0z\x04\0\x03add\x01\0\x04\x01\x1btanishiking:test/tes\
t@0.0.1\x05\0\x04\x01\x1btanishiking:test/plug@0.0.1\x04\0\x0b\x0a\x01\0\x04plug\
\x03\0\0\0G\x09producers\x01\x0cprocessed-by\x02\x0dwit-component\x070.216.0\x10\
wit-bindgen-rust\x060.31.0";
#[inline(never)]
#[doc(hidden)]
pub fn __link_custom_section_describing_imports() {
    wit_bindgen_rt::maybe_link_cabi_realloc();
}
