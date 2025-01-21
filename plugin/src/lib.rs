#[allow(warnings)]
mod bindings;

use crate::bindings::exports::tanishiking::test::test::{Guest, GuestCounter, Counter, Tree};

use std::cell::RefCell;
use ferris_says::say;

struct HostCounter {
    value: i32,
}

impl HostCounter {
    fn new() -> Self {
        let value = 0;
        HostCounter { value }
    }

    fn up(&mut self) {
        self.value += 1;
    }

    fn down(&mut self) {
        if self.value > 0 {
            self.value -= 1
        }
    }

    fn value_of(&self) -> i32 {
        self.value
    }
}

struct GuestCounterImpl {
    inner: RefCell<HostCounter>,
}

impl GuestCounterImpl {
    fn create() -> Self {
        let inner = HostCounter::new();
        let inner = RefCell::new(inner);
        GuestCounterImpl { inner }
    }
}

impl GuestCounter for GuestCounterImpl {
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

struct Component;

impl Guest for Component {
    fn add(a: i32, b: i32) -> i32 {
        return a + b;
    }

    fn say(content: String) {
        let width = 80;
        let mut writer = std::io::stdout();

        if let Err(e) = say(content.as_str(), width, &mut writer) {
            println!("{e}");
        }
    }

    fn print_number(x: i32) {
        println!("{}", x);
    }

    fn parse(i: i32) -> Tree {
      if i == 0 {
        return Tree::StrValue(String::from("hello!"));
      } else {
        return Tree::NumValue(i);
      }
    }

    fn new_counter() -> Counter {
        let c = GuestCounterImpl::create();
        Counter::new(c)
    }

    type Counter = GuestCounterImpl;
}

bindings::export!(Component with_types_in bindings);
