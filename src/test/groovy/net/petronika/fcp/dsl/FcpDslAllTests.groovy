package net.petronika.fcp.dsl

import org.junit.runner.RunWith
import org.junit.runners.Suite

@RunWith(Suite.class)
@Suite.SuiteClasses([
	FcpBuilderConnectionTest.class,
	FcpBuilderOperationsTest.class
])
class FcpDslAllTests {
}