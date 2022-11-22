munitSuites += "MySuite" -> new FunSuite {

  test("This test fails") {
    assertEquals(true, false)
  }

}
