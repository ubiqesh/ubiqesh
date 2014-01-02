package io.ubiqesh.central.mqtt.commands

import org.scalatest._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class PathMatcherTest extends FlatSpec with Matchers {
  "topic /test" should "match path /test" in {
    val matcher = new PathMatcher()
    matcher.matchPath("/test", "/test") should be (true)
  }
  it should "not match topic /test/test" in {
    val matcher = new PathMatcher()
    matcher.matchPath("/test", "/test/test") should be (false)
  }

  "topic /test/+" should "match topic /test/test" in {
    val matcher = new PathMatcher()
    matcher.matchPath("/test/+", "/test/test") should be (true)
  }
  it should "match topic /test/test/test" in {
    val matcher = new PathMatcher()
    matcher.matchPath("/test/+", "/test/test/test") should be (false)
  }

  "topic /test/#" should "match topic /test/test" in {
    val matcher = new PathMatcher()
    matcher.matchPath("/test/#", "/test/test") should be (true)
  }
  it should "match topic /test/test/test" in {
    val matcher = new PathMatcher()
    matcher.matchPath("/test/#", "/test/test/test") should be (true)
  }
}