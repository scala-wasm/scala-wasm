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

      return Ok(());
    }
}

bindings::export!(Component with_types_in bindings);
