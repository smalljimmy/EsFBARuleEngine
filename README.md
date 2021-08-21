#What
Build up a rule engine to evaluate OIH economic decisions with Outlet Eligibility Check as first use case.

#Why
The reason for using Kotlin for Rule engine is trifold:

1. The rule engine itself is modeled as non-blocking, 100% asynchronous concurrent engine. The coroutine of Kotlin has helped reduce system resource need and it’s structured concurrency model alleviates many pain in exceptional thread handling. You can see my performance test result in the detail design link below.
2. The project was designed to use Drool & Actor model for concurrency handling. Inside Amazon, only Actor Scalar is in Brazil. There’re compatibility issue between drool rule and Scalar. Kotlin has Actor library as well, and it has no compatibility issue with Drools (thanks to designed Interoperability).
3. Code conciseness and readability (important for designing currency software for a Middleware)

#How
1. High level design - https://tiny.amazon.com/kwb7sqe4/wamazbinviewUserzengQuipOIH_

2. Detail design - https://tiny.amazon.com/1ekxqyqhv/tinyamazkwb7wama

#Test
1. Automatic tests with different inputs and decisions (RuleProcessorTest)
2. Performance tests for batch input (RuleProcessorTest)
3. Integration test from client (FBAFilterHandlerIntegTest)