#[allow(warnings)]
mod bindings;

use crate::bindings::exports::tanishiking::test::test::Guest;

struct Component;

impl Guest for Component {
    fn add(a: i32, b: i32) -> i32 {
        return a + b;
    }
}

bindings::export!(Component with_types_in bindings);
