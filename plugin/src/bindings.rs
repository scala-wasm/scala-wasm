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
                #[derive(Debug)]
                #[repr(transparent)]
                pub struct Counter {
                    handle: _rt::Resource<Counter>,
                }
                type _CounterRep<T> = Option<T>;
                impl Counter {
                    /// Creates a new resource from the specified representation.
                    ///
                    /// This function will create a new resource handle by moving `val` onto
                    /// the heap and then passing that heap pointer to the component model to
                    /// create a handle. The owned handle is then returned as `Counter`.
                    pub fn new<T: GuestCounter>(val: T) -> Self {
                        Self::type_guard::<T>();
                        let val: _CounterRep<T> = Some(val);
                        let ptr: *mut _CounterRep<T> = _rt::Box::into_raw(
                            _rt::Box::new(val),
                        );
                        unsafe { Self::from_handle(T::_resource_new(ptr.cast())) }
                    }
                    /// Gets access to the underlying `T` which represents this resource.
                    pub fn get<T: GuestCounter>(&self) -> &T {
                        let ptr = unsafe { &*self.as_ptr::<T>() };
                        ptr.as_ref().unwrap()
                    }
                    /// Gets mutable access to the underlying `T` which represents this
                    /// resource.
                    pub fn get_mut<T: GuestCounter>(&mut self) -> &mut T {
                        let ptr = unsafe { &mut *self.as_ptr::<T>() };
                        ptr.as_mut().unwrap()
                    }
                    /// Consumes this resource and returns the underlying `T`.
                    pub fn into_inner<T: GuestCounter>(self) -> T {
                        let ptr = unsafe { &mut *self.as_ptr::<T>() };
                        ptr.take().unwrap()
                    }
                    #[doc(hidden)]
                    pub unsafe fn from_handle(handle: u32) -> Self {
                        Self {
                            handle: _rt::Resource::from_handle(handle),
                        }
                    }
                    #[doc(hidden)]
                    pub fn take_handle(&self) -> u32 {
                        _rt::Resource::take_handle(&self.handle)
                    }
                    #[doc(hidden)]
                    pub fn handle(&self) -> u32 {
                        _rt::Resource::handle(&self.handle)
                    }
                    #[doc(hidden)]
                    fn type_guard<T: 'static>() {
                        use core::any::TypeId;
                        static mut LAST_TYPE: Option<TypeId> = None;
                        unsafe {
                            assert!(! cfg!(target_feature = "atomics"));
                            let id = TypeId::of::<T>();
                            match LAST_TYPE {
                                Some(ty) => {
                                    assert!(
                                        ty == id, "cannot use two types with this resource type"
                                    )
                                }
                                None => LAST_TYPE = Some(id),
                            }
                        }
                    }
                    #[doc(hidden)]
                    pub unsafe fn dtor<T: 'static>(handle: *mut u8) {
                        Self::type_guard::<T>();
                        let _ = _rt::Box::from_raw(handle as *mut _CounterRep<T>);
                    }
                    fn as_ptr<T: GuestCounter>(&self) -> *mut _CounterRep<T> {
                        Counter::type_guard::<T>();
                        T::_resource_rep(self.handle()).cast()
                    }
                }
                /// A borrowed version of [`Counter`] which represents a borrowed value
                /// with the lifetime `'a`.
                #[derive(Debug)]
                #[repr(transparent)]
                pub struct CounterBorrow<'a> {
                    rep: *mut u8,
                    _marker: core::marker::PhantomData<&'a Counter>,
                }
                impl<'a> CounterBorrow<'a> {
                    #[doc(hidden)]
                    pub unsafe fn lift(rep: usize) -> Self {
                        Self {
                            rep: rep as *mut u8,
                            _marker: core::marker::PhantomData,
                        }
                    }
                    /// Gets access to the underlying `T` in this resource.
                    pub fn get<T: GuestCounter>(&self) -> &T {
                        let ptr = unsafe { &mut *self.as_ptr::<T>() };
                        ptr.as_ref().unwrap()
                    }
                    fn as_ptr<T: 'static>(&self) -> *mut _CounterRep<T> {
                        Counter::type_guard::<T>();
                        self.rep.cast()
                    }
                }
                unsafe impl _rt::WasmResource for Counter {
                    #[inline]
                    unsafe fn drop(_handle: u32) {
                        #[cfg(not(target_arch = "wasm32"))]
                        unreachable!();
                        #[cfg(target_arch = "wasm32")]
                        {
                            #[link(
                                wasm_import_module = "[export]tanishiking:test/test@0.0.1"
                            )]
                            extern "C" {
                                #[link_name = "[resource-drop]counter"]
                                fn drop(_: u32);
                            }
                            drop(_handle);
                        }
                    }
                }
                #[repr(C)]
                #[derive(Clone, Copy)]
                pub struct Point {
                    pub x: u32,
                    pub y: u32,
                }
                impl ::core::fmt::Debug for Point {
                    fn fmt(
                        &self,
                        f: &mut ::core::fmt::Formatter<'_>,
                    ) -> ::core::fmt::Result {
                        f.debug_struct("Point")
                            .field("x", &self.x)
                            .field("y", &self.y)
                            .finish()
                    }
                }
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn _export_ferris_say_cabi<T: Guest>(
                    arg0: *mut u8,
                    arg1: usize,
                    arg2: i32,
                ) -> *mut u8 {
                    #[cfg(target_arch = "wasm32")] _rt::run_ctors_once();
                    let len0 = arg1;
                    let bytes0 = _rt::Vec::from_raw_parts(arg0.cast(), len0, len0);
                    let result1 = T::ferris_say(_rt::string_lift(bytes0), arg2 as u32);
                    let ptr2 = _RET_AREA.0.as_mut_ptr().cast::<u8>();
                    let vec3 = (result1.into_bytes()).into_boxed_slice();
                    let ptr3 = vec3.as_ptr().cast::<u8>();
                    let len3 = vec3.len();
                    ::core::mem::forget(vec3);
                    *ptr2.add(4).cast::<usize>() = len3;
                    *ptr2.add(0).cast::<*mut u8>() = ptr3.cast_mut();
                    ptr2
                }
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn __post_return_ferris_say<T: Guest>(arg0: *mut u8) {
                    let l0 = *arg0.add(0).cast::<*mut u8>();
                    let l1 = *arg0.add(4).cast::<usize>();
                    _rt::cabi_dealloc(l0, l1, 1);
                }
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn _export_get_origin_cabi<T: Guest>() -> *mut u8 {
                    #[cfg(target_arch = "wasm32")] _rt::run_ctors_once();
                    let result0 = T::get_origin();
                    let ptr1 = _RET_AREA.0.as_mut_ptr().cast::<u8>();
                    let Point { x: x2, y: y2 } = result0;
                    *ptr1.add(0).cast::<i32>() = _rt::as_i32(x2);
                    *ptr1.add(4).cast::<i32>() = _rt::as_i32(y2);
                    ptr1
                }
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn _export_new_counter_cabi<T: Guest>() -> i32 {
                    #[cfg(target_arch = "wasm32")] _rt::run_ctors_once();
                    let result0 = T::new_counter();
                    (result0).take_handle() as i32
                }
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn _export_method_counter_up_cabi<T: GuestCounter>(
                    arg0: *mut u8,
                ) {
                    #[cfg(target_arch = "wasm32")] _rt::run_ctors_once();
                    T::up(CounterBorrow::lift(arg0 as u32 as usize).get());
                }
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn _export_method_counter_down_cabi<T: GuestCounter>(
                    arg0: *mut u8,
                ) {
                    #[cfg(target_arch = "wasm32")] _rt::run_ctors_once();
                    T::down(CounterBorrow::lift(arg0 as u32 as usize).get());
                }
                #[doc(hidden)]
                #[allow(non_snake_case)]
                pub unsafe fn _export_method_counter_value_of_cabi<T: GuestCounter>(
                    arg0: *mut u8,
                ) -> i32 {
                    #[cfg(target_arch = "wasm32")] _rt::run_ctors_once();
                    let result0 = T::value_of(
                        CounterBorrow::lift(arg0 as u32 as usize).get(),
                    );
                    _rt::as_i32(result0)
                }
                pub trait Guest {
                    type Counter: GuestCounter;
                    fn ferris_say(content: _rt::String, width: u32) -> _rt::String;
                    fn get_origin() -> Point;
                    fn new_counter() -> Counter;
                }
                pub trait GuestCounter: 'static {
                    #[doc(hidden)]
                    unsafe fn _resource_new(val: *mut u8) -> u32
                    where
                        Self: Sized,
                    {
                        #[cfg(not(target_arch = "wasm32"))]
                        {
                            let _ = val;
                            unreachable!();
                        }
                        #[cfg(target_arch = "wasm32")]
                        {
                            #[link(
                                wasm_import_module = "[export]tanishiking:test/test@0.0.1"
                            )]
                            extern "C" {
                                #[link_name = "[resource-new]counter"]
                                fn new(_: *mut u8) -> u32;
                            }
                            new(val)
                        }
                    }
                    #[doc(hidden)]
                    fn _resource_rep(handle: u32) -> *mut u8
                    where
                        Self: Sized,
                    {
                        #[cfg(not(target_arch = "wasm32"))]
                        {
                            let _ = handle;
                            unreachable!();
                        }
                        #[cfg(target_arch = "wasm32")]
                        {
                            #[link(
                                wasm_import_module = "[export]tanishiking:test/test@0.0.1"
                            )]
                            extern "C" {
                                #[link_name = "[resource-rep]counter"]
                                fn rep(_: u32) -> *mut u8;
                            }
                            unsafe { rep(handle) }
                        }
                    }
                    /// new: static func() -> counter; TODO: support static method
                    fn up(&self);
                    fn down(&self);
                    fn value_of(&self) -> i32;
                }
                #[doc(hidden)]
                macro_rules! __export_tanishiking_test_test_0_0_1_cabi {
                    ($ty:ident with_types_in $($path_to_types:tt)*) => {
                        const _ : () = { #[export_name =
                        "tanishiking:test/test@0.0.1#ferris-say"] unsafe extern "C" fn
                        export_ferris_say(arg0 : * mut u8, arg1 : usize, arg2 : i32,) ->
                        * mut u8 { $($path_to_types)*:: _export_ferris_say_cabi::<$ty >
                        (arg0, arg1, arg2) } #[export_name =
                        "cabi_post_tanishiking:test/test@0.0.1#ferris-say"] unsafe extern
                        "C" fn _post_return_ferris_say(arg0 : * mut u8,) {
                        $($path_to_types)*:: __post_return_ferris_say::<$ty > (arg0) }
                        #[export_name = "tanishiking:test/test@0.0.1#get-origin"] unsafe
                        extern "C" fn export_get_origin() -> * mut u8 {
                        $($path_to_types)*:: _export_get_origin_cabi::<$ty > () }
                        #[export_name = "tanishiking:test/test@0.0.1#new-counter"] unsafe
                        extern "C" fn export_new_counter() -> i32 { $($path_to_types)*::
                        _export_new_counter_cabi::<$ty > () } #[export_name =
                        "tanishiking:test/test@0.0.1#[method]counter.up"] unsafe extern
                        "C" fn export_method_counter_up(arg0 : * mut u8,) {
                        $($path_to_types)*:: _export_method_counter_up_cabi::<<$ty as
                        $($path_to_types)*:: Guest >::Counter > (arg0) } #[export_name =
                        "tanishiking:test/test@0.0.1#[method]counter.down"] unsafe extern
                        "C" fn export_method_counter_down(arg0 : * mut u8,) {
                        $($path_to_types)*:: _export_method_counter_down_cabi::<<$ty as
                        $($path_to_types)*:: Guest >::Counter > (arg0) } #[export_name =
                        "tanishiking:test/test@0.0.1#[method]counter.value-of"] unsafe
                        extern "C" fn export_method_counter_value_of(arg0 : * mut u8,) ->
                        i32 { $($path_to_types)*::
                        _export_method_counter_value_of_cabi::<<$ty as
                        $($path_to_types)*:: Guest >::Counter > (arg0) } const _ : () = {
                        #[doc(hidden)] #[export_name =
                        "tanishiking:test/test@0.0.1#[dtor]counter"]
                        #[allow(non_snake_case)] unsafe extern "C" fn dtor(rep : * mut
                        u8) { $($path_to_types)*:: Counter::dtor::< <$ty as
                        $($path_to_types)*:: Guest >::Counter > (rep) } }; };
                    };
                }
                #[doc(hidden)]
                pub(crate) use __export_tanishiking_test_test_0_0_1_cabi;
                #[repr(align(4))]
                struct _RetArea([::core::mem::MaybeUninit<u8>; 8]);
                static mut _RET_AREA: _RetArea = _RetArea(
                    [::core::mem::MaybeUninit::uninit(); 8],
                );
            }
        }
    }
}
mod _rt {
    use core::fmt;
    use core::marker;
    use core::sync::atomic::{AtomicU32, Ordering::Relaxed};
    /// A type which represents a component model resource, either imported or
    /// exported into this component.
    ///
    /// This is a low-level wrapper which handles the lifetime of the resource
    /// (namely this has a destructor). The `T` provided defines the component model
    /// intrinsics that this wrapper uses.
    ///
    /// One of the chief purposes of this type is to provide `Deref` implementations
    /// to access the underlying data when it is owned.
    ///
    /// This type is primarily used in generated code for exported and imported
    /// resources.
    #[repr(transparent)]
    pub struct Resource<T: WasmResource> {
        handle: AtomicU32,
        _marker: marker::PhantomData<T>,
    }
    /// A trait which all wasm resources implement, namely providing the ability to
    /// drop a resource.
    ///
    /// This generally is implemented by generated code, not user-facing code.
    #[allow(clippy::missing_safety_doc)]
    pub unsafe trait WasmResource {
        /// Invokes the `[resource-drop]...` intrinsic.
        unsafe fn drop(handle: u32);
    }
    impl<T: WasmResource> Resource<T> {
        #[doc(hidden)]
        pub unsafe fn from_handle(handle: u32) -> Self {
            debug_assert!(handle != u32::MAX);
            Self {
                handle: AtomicU32::new(handle),
                _marker: marker::PhantomData,
            }
        }
        /// Takes ownership of the handle owned by `resource`.
        ///
        /// Note that this ideally would be `into_handle` taking `Resource<T>` by
        /// ownership. The code generator does not enable that in all situations,
        /// unfortunately, so this is provided instead.
        ///
        /// Also note that `take_handle` is in theory only ever called on values
        /// owned by a generated function. For example a generated function might
        /// take `Resource<T>` as an argument but then call `take_handle` on a
        /// reference to that argument. In that sense the dynamic nature of
        /// `take_handle` should only be exposed internally to generated code, not
        /// to user code.
        #[doc(hidden)]
        pub fn take_handle(resource: &Resource<T>) -> u32 {
            resource.handle.swap(u32::MAX, Relaxed)
        }
        #[doc(hidden)]
        pub fn handle(resource: &Resource<T>) -> u32 {
            resource.handle.load(Relaxed)
        }
    }
    impl<T: WasmResource> fmt::Debug for Resource<T> {
        fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
            f.debug_struct("Resource").field("handle", &self.handle).finish()
        }
    }
    impl<T: WasmResource> Drop for Resource<T> {
        fn drop(&mut self) {
            unsafe {
                match self.handle.load(Relaxed) {
                    u32::MAX => {}
                    other => T::drop(other),
                }
            }
        }
    }
    pub use alloc_crate::boxed::Box;
    #[cfg(target_arch = "wasm32")]
    pub fn run_ctors_once() {
        wit_bindgen_rt::run_ctors_once();
    }
    pub use alloc_crate::vec::Vec;
    pub unsafe fn string_lift(bytes: Vec<u8>) -> String {
        if cfg!(debug_assertions) {
            String::from_utf8(bytes).unwrap()
        } else {
            String::from_utf8_unchecked(bytes)
        }
    }
    pub unsafe fn cabi_dealloc(ptr: *mut u8, size: usize, align: usize) {
        if size == 0 {
            return;
        }
        let layout = alloc::Layout::from_size_align_unchecked(size, align);
        alloc::dealloc(ptr, layout);
    }
    pub use alloc_crate::string::String;
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
    extern crate alloc as alloc_crate;
    pub use alloc_crate::alloc;
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
pub static __WIT_BINDGEN_COMPONENT_TYPE: [u8; 410] = *b"\
\0asm\x0d\0\x01\0\0\x19\x16wit-component-encoding\x04\0\x07\x9f\x02\x01A\x02\x01\
A\x02\x01B\x10\x04\0\x07counter\x03\x01\x01r\x02\x01xy\x01yy\x04\0\x05point\x03\0\
\x01\x01h\0\x01@\x01\x04self\x03\x01\0\x04\0\x12[method]counter.up\x01\x04\x04\0\
\x14[method]counter.down\x01\x04\x01@\x01\x04self\x03\0z\x04\0\x18[method]counte\
r.value-of\x01\x05\x01@\x02\x07contents\x05widthy\0s\x04\0\x0aferris-say\x01\x06\
\x01@\0\0\x02\x04\0\x0aget-origin\x01\x07\x01i\0\x01@\0\0\x08\x04\0\x0bnew-count\
er\x01\x09\x04\x01\x1btanishiking:test/test@0.0.1\x05\0\x04\x01\x1btanishiking:t\
est/plug@0.0.1\x04\0\x0b\x0a\x01\0\x04plug\x03\0\0\0G\x09producers\x01\x0cproces\
sed-by\x02\x0dwit-component\x070.216.0\x10wit-bindgen-rust\x060.31.0";
#[inline(never)]
#[doc(hidden)]
pub fn __link_custom_section_describing_imports() {
    wit_bindgen_rt::maybe_link_cabi_realloc();
}
