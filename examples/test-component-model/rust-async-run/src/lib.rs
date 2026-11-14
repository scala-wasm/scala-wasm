// Calls the `async func` exports of the Scala component (@WitAsync).
// Scala lifts them with the synchronous Canonical ABI, so this checks both
// spec pairings with a sync-ABI callee: an async-ABI caller and a sync-ABI
// caller.
wit_bindgen::generate!({
    world: "rust-async-run",
    path: "../wit",
    // Lower `bare-add-one` with the synchronous ABI. `add-one` keeps the
    // default for `async func`: the async ABI.
    sync: ["import:bare-add-one"],
});

use component::testing::async_api;

struct Component;

impl Guest for Component {
    async fn run_async_checks() -> i32 {
        assert_eq!(bare_add_one(41), 42);
        assert_eq!(async_api::add_one(41).await, 42);
        async_api::add_one(1).await + bare_add_one(2)
    }
}

export!(Component);
