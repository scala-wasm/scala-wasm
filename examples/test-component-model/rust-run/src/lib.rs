#[allow(warnings)]
mod bindings;

use crate::bindings::exports::wasi::cli::run::Guest as Run;
use crate::bindings::component::testing::tests::*;
use crate::bindings::component::testing::test_imports;

struct Component;

impl Run for Component {
    fn run() -> Result<(), ()> {
      test_imports::run();
      assert_eq!(
        roundtrip_string("aaa"),
        "aaa",
      );
      assert_eq!(
        roundtrip_string(""),
        "",
      );

      let p = Point { x: 0, y: 3 };
      assert_eq!(
        roundtrip_point(p), p
      );

      test_c1(C1::A(100));
      assert_eq!(roundtrip_c1(C1::A(2)), C1::A(2));
      assert_eq!(roundtrip_c1(C1::A(0)), C1::A(0));
      assert_eq!(roundtrip_c1(C1::B(100.0)), C1::B(100.0));
      assert_eq!(roundtrip_z1(Z1::A(140)), Z1::A(140));
      assert_eq!(roundtrip_z1(Z1::B), Z1::B);

      assert_eq!(roundtrip_enum(E1::A), E1::A);
      assert_eq!(roundtrip_enum(E1::B), E1::B);
      assert_eq!(roundtrip_enum(E1::C), E1::C);

      assert_eq!(
        roundtrip_tuple((C1::A(0), Z1::B)),
        (C1::A(0), Z1::B)
      );
      assert_eq!(
        roundtrip_tuple((C1::B(5521.53), Z1::A(64534))),
        (C1::B(5521.53), Z1::A(64534)),
      );

      return Ok(());
    }
}

bindings::export!(Component with_types_in bindings);
