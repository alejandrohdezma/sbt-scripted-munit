@DESCRIPTION@

## Usage

Create a scripted test for your SBT plugin following the
[official guide](https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html).

Add the following line to the `plugins.sbt` file of your scripted test:

```sbt
addSbtPlugin("@ORGANIZATION@" % "@NAME@" % "@VERSION@")
```

Add some tests to `build.sbt`:

```scala
munitSuites += "MySuite" -> new FunSuite {

  test("The most important question") {
    assertEquals("The meaning of life, the universe and everything else", "42")
  }

}
```

Add this to your `test` file so MUnit suites are executed:

```
> munitScripted
```

## Contributors to this project 

@CONTRIBUTORS_TABLE@
