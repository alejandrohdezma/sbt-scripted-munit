SBT plugin to enable using MUnit to test your SBT plugins

## Usage

Create a scripted test for your SBT plugin following the
[official guide](https://www.scala-sbt.org/1.x/docs/Testing-sbt-plugins.html).

Add the following line to the `plugins.sbt` file of your scripted test:

```sbt
addSbtPlugin("com.alejandrohdezma" % "sbt-scripted-munit" % "0.1.0")
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

| <a href="https://github.com/alejandrohdezma"><img alt="alejandrohdezma" src="https://avatars.githubusercontent.com/u/9027541?v=4&s=120" width="120px" /></a> |
| :--: |
| <a href="https://github.com/alejandrohdezma"><sub><b>alejandrohdezma</b></sub></a> |
