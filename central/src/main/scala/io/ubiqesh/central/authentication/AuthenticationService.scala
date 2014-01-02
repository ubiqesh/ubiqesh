package io.ubiqesh.central.authentication

/**
 * Created by balu on 02.01.14.
 */
trait AuthenticationService {
  def authorized(username:String, password: String): Boolean
}

class AllowAllAuthenticationService extends AuthenticationService {
  override def authorized(username:String, password: String): Boolean = {
    true
  }
}