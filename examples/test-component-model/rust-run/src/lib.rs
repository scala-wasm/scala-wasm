#[allow(warnings)]
mod bindings;

use crate::bindings::exports::wasi::cli::run::Guest as Run;
use crate::bindings::component::testing::tests;
use crate::bindings::component::testing::test_imports;

struct Component;

impl Run for Component {
    fn run() -> Result<(), ()> {
      test_imports::run();
      assert_eq!(
        tests::roundtrip_string("aaa"),
        "aaa",
      );
      assert_eq!(
        tests::roundtrip_string(""),
        "",
      );

      let p = tests::Point { x: 0, y: 3 };
      assert_eq!(
        tests::roundtrip_point(p), p
      );

      tests::test_c1(tests::C1::A(100));
      assert_eq!(tests::roundtrip_c1(tests::C1::A(2)), tests::C1::A(2));
      assert_eq!(tests::roundtrip_c1(tests::C1::A(0)), tests::C1::A(0));
      assert_eq!(tests::roundtrip_c1(tests::C1::B(100.0)), tests::C1::B(100.0));
      assert_eq!(tests::roundtrip_z1(tests::Z1::A(140)), tests::Z1::A(140));
      assert_eq!(tests::roundtrip_z1(tests::Z1::B), tests::Z1::B);

      return Ok(());
    }
}

bindings::export!(Component with_types_in bindings);
