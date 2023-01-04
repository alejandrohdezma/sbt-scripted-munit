munitSuites += "MySuite" -> new FunSuite {

  test("This test passes") {
    assertEquals(description.value, "simple")
  }

  test("This test is ignored".ignore) {
    assertEquals(true, true)
  }

}

munitSuites += "MyOtherSuite" -> new FunSuite {

  test("This assertion fails, but the test passes".fail) {
    assertEquals(true, false)
  }

}
