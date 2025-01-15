#[allow(warnings)]
mod bindings;

use crate::bindings::exports::tanishiking::test::test::{Guest, GuestCounter, Counter};

use std::cell::RefCell;

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
        println!("{}", a + b);
        return a + b;
    }

    fn say(content: String) {
        println!("{}", content);
    }

    fn new_counter() -> Counter {
        let c = GuestCounterImpl::create();
        Counter::new(c)
    }

    type Counter = GuestCounterImpl;
}

bindings::export!(Component with_types_in bindings);
