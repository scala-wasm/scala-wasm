#[allow(warnings)]
mod bindings;

use crate::bindings::exports::component::testing::countable::{GuestCounter, Counter};
use crate::bindings::exports::component::testing::countable::Guest as Countable;
use crate::bindings::exports::component::testing::basics::Guest as Basics;
use crate::bindings::exports::component::testing::tests::Guest as Tests;
use crate::bindings::exports::component::testing::tests::*;

use std::cell::RefCell;

struct Component;

impl Basics for Component {
  fn roundtrip_u8(a: u8) -> u8 { a }
  fn roundtrip_s8(a: i8) -> i8 { a }
  fn roundtrip_u16(a: u16) -> u16 { a }
  fn roundtrip_s16(a: i16) -> i16 { a }
  fn roundtrip_u32(a: u32) -> u32 { a }
  fn roundtrip_s32(a: i32) -> i32 { a }
  fn roundtrip_u64(a: u64) -> u64 { a }
  fn roundtrip_s64(a: i64) -> i64 { a }
  fn roundtrip_f32(a: f32) -> f32 { a }
  fn roundtrip_f64(a: f64) -> f64 { a }
  fn roundtrip_char(a: char) -> char { a }
}

struct HostCounter {
  value: i32,
}
impl HostCounter {
  fn new(i: i32) -> Self {
    HostCounter { value: i }
  }
  fn up(&mut self) {
    self.value += 1;
  }
  fn down(&mut self) {
    self.value -= 1
  }
  fn value_of(&self) -> i32 {
      self.value
  }
}
struct GuestCounterImpl {
  inner: RefCell<HostCounter>,
}
impl GuestCounterImpl {
  fn create(i: i32) -> Self {
    let inner = HostCounter::new(i);
    let inner = RefCell::new(inner);
    GuestCounterImpl { inner }
  }
}

impl GuestCounter for GuestCounterImpl {
  fn new(i: i32) -> Self {
    return GuestCounterImpl::create(i);
  }
  fn sum(a: Counter, b: Counter) -> Counter {
    let s = a.get::<GuestCounterImpl>().value_of() + b.get::<GuestCounterImpl>().value_of();
    let c = GuestCounterImpl::create(s);
    return Counter::new(c);
  }
  fn up(&self) {
      self.inner.borrow_mut().up();
  }
  fn down(&self) {
      self.inner.borrow_mut().down();
  }
  fn value_of(&self) -> i32 {
      self.inner.borrow().value_of()
  }
}

impl Countable for Component {
  type Counter = GuestCounterImpl;
}

#[allow(unused_variables)]
impl Tests for Component {
  fn roundtrip_basics0(a: (u32, i32)) -> (u32, i32) { a }
  fn roundtrip_basics1(a: (u8, i8, u16, i16, u32, i32, f32, f64, char)) -> (u8, i8, u16, i16, u32, i32, f32, f64, char) { a }
  fn roundtrip_list_u16(a: Vec<u16>) -> Vec<u16> { a }
  fn roundtrip_list_point(a: Vec<Point>) -> Vec<Point> { a }
  fn roundtrip_list_variant(a: Vec<C1>) -> Vec<C1> { a }
  fn roundtrip_string(a: String) -> String { a }
  fn roundtrip_point(a: Point) -> Point { a }
  fn test_c1(a: C1) {}
  fn roundtrip_c1(a: C1) -> C1 { a }
  fn roundtrip_z1(a: Z1) -> Z1 { a }
  fn roundtrip_enum(a: E1) -> E1 { a }
  fn roundtrip_tuple(a: (C1, Z1)) -> (C1, Z1) { a }
  fn roundtrip_result(a: Result<(), ()>) -> Result<(), ()> { a }
  fn roundtrip_string_error(a: Result<f32, String>) -> Result<f32, String> { a }
  fn roundtrip_enum_error(a: Result<C1, E1>) -> Result<C1, E1> { a }
  fn roundtrip_option(a: Option<String>) -> Option<String> { a }
  fn roundtrip_double_option(a: Option<Option<String>>) -> Option<Option<String>> { a }

  fn roundtrip_f8(a: F1) -> F1 { a }
  fn roundtrip_f16(a: F2) -> F2 { a }
  fn roundtrip_f32(a: F3) -> F3 { a }
  fn roundtrip_flags(a: (F1, F1)) -> (F1, F1) { a }
}

bindings::export!(Component with_types_in bindings);
