#[allow(warnings)]
mod bindings;
use crate::bindings::exports::scala_wasm::helloworld::greeter::Guest;

struct Component;
impl Guest for Component {
    fn greet(content: String) -> String {
        let mut buf = Vec::new();
        let message = format!("Hello {}!", content.as_str());
        ferris_says::say(&message, 80, &mut buf).unwrap();
        return String::from_utf8(buf).unwrap();
    }
}
bindings::export!(Component with_types_in bindings);
