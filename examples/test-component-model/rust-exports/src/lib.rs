#[allow(warnings)]
mod bindings;

use crate::bindings::exports::component::testing::basics::Guest as Basics;
use crate::bindings::exports::component::testing::tests::Guest as Tests;
use crate::bindings::exports::component::testing::tests::*;

struct Component;

impl Basics for Component {
  fn roundtrip_u8(a: u8) -> u8 { a }
  fn roundtrip_s8(a: i8) -> i8 { a }
  fn roundtrip_u16(a: u16) -> u16 { a }
  fn roundtrip_s16(a: i16) -> i16 { a }
  fn roundtrip_u32(a: u32) -> u32 { a }
  fn roundtrip_s32(a: i32) -> i32 { a }
  // fn roundtrip_u64(a: u64) -> u64 { a }
  // fn roundtrip_s64(a: i64) -> i64 { a }
  fn roundtrip_f32(a: f32) -> f32 { a }
  fn roundtrip_f64(a: f64) -> f64 { a }
  fn roundtrip_char(a: char) -> char { a }
}

#[allow(unused_variables)]
impl Tests for Component {
  fn roundtrip_string(a: String) -> String { a }
  fn roundtrip_point(a: Point) -> Point { a }
  fn test_c1(a: C1) {}
  fn roundtrip_c1(a: C1) -> C1 { a }
  fn roundtrip_z1(a: Z1) -> Z1 { a }
  fn roundtrip_enum(a: E1) -> E1 { a }
  fn roundtrip_tuple(a: (C1, Z1)) -> (C1, Z1) { a }
}

bindings::export!(Component with_types_in bindings);
