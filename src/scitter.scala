package com.tedneward.scitter
{
  import org.apache.commons.httpclient._, auth._, methods._, params._
  import scala.collection.mutable.ListBuffer
  import scala.xml._

  /**
   * Types for optional parameters to Twitter API calls
   */
  abstract class OptionalParam
  case class Id(id : String) extends OptionalParam
  case class UserId(user_id : Long) extends OptionalParam
  case class ScreenName(screen_name : String) extends OptionalParam
  case class Since(since_id : Long) extends OptionalParam
  case class Max(max_id : Long) extends OptionalParam
  case class Count(count : Int) extends OptionalParam
  case class Page(page : Int) extends OptionalParam
  case class InReplyToStatusId(status_id : Long) extends OptionalParam

  /**
   * Object for consuming "non-specific" Twitter feeds, such as the public timeline.
   * Use this to do non-authenticated requests of Twitter feeds.
   */
  object Scitter
  {
    /**
     * Ping the server to see if it's up and running.
     *
     * Twitter docs say:
     * test
     * Returns the string "ok" in the requested format with a 200 OK HTTP status code.
     * URL: http://twitter.com/help/test.format
     * Formats: xml, json
     * Method(s): GET
     */
    def test : Boolean =
    {
      val (statusCode, statusBody) = execute("http://twitter.com/statuses/public_timeline.xml")
      statusCode == 200
    }

    /**
     *
     */
    def rateLimitStatus : Option[RateLimits] =
    {
      val url = "http://twitter.com/account/rate_limit_status.xml"
      val (statusCode, statusBody) =
        Scitter.execute(url)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(statusBody)
        
        Some(RateLimits.fromXml(responseXML))
      }
      else
      {
        None
      }
    }

    /**
     * Query the public timeline for the most recent statuses.
     *
     * Twitter docs say:
     * public_timeline
     * Returns the 20 most recent statuses from non-protected users who have set
     * a custom user icon.  Does not require authentication.  Note that the
     * public timeline is cached for 60 seconds so requesting it more often than
     * that is a waste of resources.
     * URL: http://twitter.com/statuses/public_timeline.format
     * Formats: xml, json, rss, atom
     * Method(s): GET
     * API limit: Not applicable
     * Returns: list of status elements     
     */
    def publicTimeline : List[Status] =
    {
      val (statusCode, statusBody) = execute("http://twitter.com/statuses/public_timeline.xml")

      if (statusCode == 200)
      {
        val responseXML = XML.loadString(statusBody)

        val statusListBuffer = new ListBuffer[Status]

        for (n <- (responseXML \\ "status").elements)
          statusListBuffer += (Status.fromXml(n))
        
        statusListBuffer.toList
      }
      else
      {
        Nil
      }
    }

    /**
     *
     */
    def show(id : Long) : Option[Status] =
    {
      val (statusCode, body) =
        Scitter.execute("http://twitter.com/statuses/show/" + id + ".xml")
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(body)
        
        Some(Status.fromXml(responseXML))
      }
      else
      {
        None
      }
    }
    
    private[scitter] def execute(url : String) : (Int, String) =
      execute(url, Map(), "", "")
    private[scitter] def execute(url : String, username : String, password : String) : (Int, String) =
      execute(url, Map(), username, password)
    private[scitter] def execute(url : String, dataMap : Map[String, String]) : (Int, String) =
      execute(url, dataMap, "", "")
    private[scitter] def execute(url : String, dataMap : Map[String, String],
                                 username : String, password : String) =
    {
      val client = new HttpClient()
      val method = 
        if (dataMap.size == 0)
        {
          new GetMethod(url)
        }
        else
        {
          var m = new PostMethod(url)
          
          /*
          val array = new Array[NameValuePair](dataMap.size)
          var pos = 0
          dataMap.elements.foreach { (pr) =>
            pr match {
              case (k, v) => array(pos) = new NameValuePair(k, v)
            }
            pos += 1
          }
          m.setRequestBody(array)
          */
          dataMap.elements.foreach { (pr) =>
            pr match {
              case (k, v) => m.addParameter(k, v)
            }
          }
          
          m
        }

      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        new DefaultHttpMethodRetryHandler(3, false))
        
      if ((username != "") && (password != ""))
      {
        client.getParams().setAuthenticationPreemptive(true)
        client.getState().setCredentials(
          new AuthScope("twitter.com", 80, AuthScope.ANY_REALM),
            new UsernamePasswordCredentials(username, password))
      }
      
      client.executeMethod(method)
      
      (method.getStatusLine().getStatusCode(), method.getResponseBodyAsString())
    }
  }
  /**
   * Class for consuming "authenticated user" Twitter APIs. Each instance is
   * thus "tied" to a particular authenticated user on Twitter, and will
   * behave accordingly (according to the Twitter API documentation).
   */
  class Scitter(username : String, password : String)
  {
    /**
     * Verify the user credentials against Twitter.
     *
     * Twitter docs say:
     *
     * verify_credentials
     * Returns an HTTP 200 OK response code and a representation of the
     * requesting user if authentication was successful; returns a 401 status
     * code and an error message if not.  Use this method to test if supplied
     * user credentials are valid.
     * URL: http://twitter.com/account/verify_credentials.format
     * Formats: xml, json
     * Method(s): GET
     */
    def verifyCredentials : Boolean =
    {
      val (statusCode, statusBody) =
        Scitter.execute("http://twitter.com/help/test.xml", username, password)

      statusCode == 200
    }

    /**
     *
     */
    def endSession : Boolean =
    {
      val (statusCode, statusBody) =
        Scitter.execute("http://twitter.com/account/end_session.xml",
          Map("" -> ""), username, password)

      statusCode == 200
    }
    
    /**
     *
     */
    def rateLimitStatus : Option[RateLimits] =
    {
      val url = "http://twitter.com/account/rate_limit_status.xml"
      val (statusCode, statusBody) =
        Scitter.execute(url, username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(statusBody)
        
        Some(RateLimits.fromXml(responseXML))
      }
      else
      {
        None
      }
    }

    /**
     * Get the authenticated user's statuses.
     *
     * Twitter docs say:
     *
     * Returns the 20 most recent statuses posted from the authenticating user.
     * It's also possible to request another user's timeline via the id
     * parameter below. This is the equivalent of the Web /archive page for
     * your own user, or the profile page for a third party.
     * URL: http://twitter.com/statuses/user_timeline.format
     * Formats: xml, json, rss, atom
     * Method(s): GET
     * Parameters:
     * * id.  Optional.  Specifies the ID or screen name of the user for whom
     * to return the user_timeline.
     * Ex: http://twitter.com/statuses/user_timeline/12345.xml or
     * http://twitter.com/statuses/user_timeline/bob.json.
     * * user_id.  Optional.  Specfies the ID of the user for whom to return the
     * user_timeline. Helpful for disambiguating when a valid user ID is also a
     * valid screen name.
     * Ex: http://twitter.com/statuses/user_timeline.xml?user_id=1401881
     * * screen_name.  Optional.  Specfies the screen name of the user for whom
     * to return the user_timeline. Helpful for disambiguating when a valid
     * screen name is also a user ID.
     * Ex: http://twitter.com/statuses/user_timeline.xml?screen_name=101010
     * * since_id.  Optional.  Returns only statuses with an ID greater than
     * (that is, more recent than) the specified ID.
     * Ex: http://twitter.com/statuses/user_timeline.xml?since_id=12345
     * * max_id. Optional.  Returns only statuses with an ID less than (that is,
     * older than) the specified ID.
     * Ex: http://twitter.com/statuses/user_timeline.xml?max_id=54321
     * * page. Optional. Ex: http://twitter.com/statuses/user_timeline.rss?page=3
     * Returns: list of status elements
     */
    def userTimeline(options : OptionalParam*) : List[Status] =
    {
      val url = "http://twitter.com/statuses/user_timeline"
      var urlId = ".xml"
      val optionsStr = new StringBuffer("?")
      for (option <- options)
      {
        option match
        {
          case Id(id) =>
            urlId = "/" + id.toString() + ".xml"
          case UserId(user_id) =>
            optionsStr.append("user_id=" + user_id.toString() + "&")
          case Since(since_id) =>
            optionsStr.append("since_id=" + since_id.toString() + "&")
          case Max(max_id) =>
            optionsStr.append("max_id=" + max_id.toString() + "&")
          case Count(count) =>
            optionsStr.append("count=" + count.toString() + "&")
          case Page(page) =>
            optionsStr.append("page=" + page.toString() + "&")
        }
      }
      
      val (statusCode, statusBody) =
        Scitter.execute(url + urlId + optionsStr.toString(), username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(statusBody)

        val statusListBuffer = new ListBuffer[Status]

        for (n <- (responseXML \\ "status").elements)
          statusListBuffer += (Status.fromXml(n))
        
        statusListBuffer.toList
      }
      else
      {
        Nil
      }
    }

    /**
     * Get the authenticated user's followers' statuses.
     *
     * Twitter docs say:
     *
     * statuses/friends_timeline.xml
     * Returns the 20 most recent statuses posted by the authenticating user
     * and that user's friends. This is the equivalent of /home on the Web.
     * URL: http://twitter.com/statuses/friends_timeline.format
     * Formats: xml, json, rss, atom
     * Method(s): GET
     * API Limit: 1 per request
     * Parameters:
     *   * since_id.  Optional.  Returns only statuses with an ID greater than
     * (that is, more recent than) the specified ID.
     * Ex: http://twitter.com/statuses/friends_timeline.xml?since_id=12345
     *   * max_id. Optional.  Returns only statuses with an ID less than (that
     * is, older than) the specified ID.
     * Ex: http://twitter.com/statuses/friends_timeline.xml?max_id=54321
     *   * count.  Optional.  Specifies the number of statuses to retrieve.
     * May not be greater than 200.
     * Ex: http://twitter.com/statuses/friends_timeline.xml?count=5
     *   * page. Optional.
     * Ex: http://twitter.com/statuses/friends_timeline.rss?page=3
     * Returns: list of status elements
     */
    def friendsTimeline(options : OptionalParam*) : List[Status] =
    {
      val optionsStr =
        new StringBuffer("http://twitter.com/statuses/friends_timeline.xml?")
      for (option <- options)
      {
        option match
        {
          case Since(since_id) =>
            optionsStr.append("since_id=" + since_id.toString() + "&")
          case Max(max_id) =>
            optionsStr.append("max_id=" + max_id.toString() + "&")
          case Count(count) =>
            optionsStr.append("count=" + count.toString() + "&")
          case Page(page) =>
            optionsStr.append("page=" + page.toString() + "&")
        }
      }
      
      val (statusCode, statusBody) =
        Scitter.execute(optionsStr.toString(), username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(statusBody)

        val statusListBuffer = new ListBuffer[Status]

        for (n <- (responseXML \\ "status").elements)
          statusListBuffer += (Status.fromXml(n))
        
        statusListBuffer.toList
      }
      else
      {
        Nil
      }
    }

    /**
     * Twitter docs say:
     *
     * Returns the authenticating user's friends, each with current status
     * inline. They are ordered by the order in which they were added as
     * friends. It's also possible to request another user's recent friends
     * list via the id parameter below.
     * URL: http://twitter.com/statuses/friends.format
     * Formats: xml, json
     * Method(s): GET
     * Parameters:
     * * id.  Optional.  The ID or screen name of the user for whom to request
     * a list of friends.
     * Ex: http://twitter.com/statuses/friends/12345.json or
     * http://twitter.com/statuses/friends/bob.xml
     * * user_id.  Optional.  Specfies the ID of the user for whom to return the
     * list of friends. Helpful for disambiguating when a valid user ID is also
     * a valid screen name.
     * Ex: http://twitter.com/statuses/friends.xml?user_id=1401881
     * * screen_name.  Optional.  Specfies the screen name of the user for whom
     * to return the list of friends. Helpful for disambiguating when a valid
     * screen name is also a user ID.
     * Ex: http://twitter.com/statuses/friends.xml?screen_name=101010
     * * page.  Optional. Retrieves the next 100 friends.
     * Ex: http://twitter.com/statuses/friends.xml?page=2
     * Returns: list of basic user information elements
     */
    def friends(options : OptionalParam*) : List[User] =
    {
      val optionsStr =
        new StringBuffer("http://twitter.com/statuses/friends.xml?")
      for (option <- options)
      {
        option match
        {
          case Id(id) =>
            optionsStr.append("id=" + id.toString() + "&")
          case UserId(user_id) =>
            optionsStr.append("user_id=" + user_id.toString() + "&")
          case ScreenName(screen_name) =>
            optionsStr.append("screen_name=" + screen_name.toString() + "&")
          case Page(page) =>
            optionsStr.append("page=" + page.toString() + "&")
          case _ =>
            // Do nothing
        }
      }
      
      val (statusCode, body) =
        Scitter.execute(optionsStr.toString(), username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(body)

        val userListBuffer = new ListBuffer[User]

        for (n <- (responseXML \\ "user").elements)
          userListBuffer += (User.fromXml(n))
        
        userListBuffer.toList
      }
      else
      {
        Nil
      }
    }

    /**
     * Twitter docs say:
     *
     * Returns the authenticating user's followers, each with current status
     * inline.  They are ordered by the order in which they joined Twitter
     * (this is going to be changed).
     * URL: http://twitter.com/statuses/followers.format
     * Formats: xml, json
     * Method(s): GET
     * Parameters:
     * * id.  Optional.  The ID or screen name of the user for whom to request
     * a list of friends.
     * Ex: http://twitter.com/statuses/followers/12345.json or
     * http://twitter.com/statuses/followers/bob.xml
     * * user_id.  Optional.  Specfies the ID of the user for whom to return the
     * list of friends. Helpful for disambiguating when a valid user ID is also
     * a valid screen name.
     * Ex: http://twitter.com/statuses/followers.xml?user_id=1401881
     * * screen_name.  Optional.  Specfies the screen name of the user for whom
     * to return the list of friends. Helpful for disambiguating when a valid
     * screen name is also a user ID.
     * Ex: http://twitter.com/statuses/followers.xml?screen_name=101010
     * * page.  Optional. Retrieves the next 100 friends.
     * Ex: http://twitter.com/statuses/followers.xml?page=2
     * Returns: list of basic user information elements
     */
    def followers(options : OptionalParam*) : List[User] =
    {
      val optionsStr =
        new StringBuffer("http://twitter.com/statuses/followers.xml?")
      for (option <- options)
      {
        option match
        {
          case Id(id) =>
            optionsStr.append("id=" + id.toString() + "&")
          case UserId(user_id) =>
            optionsStr.append("user_id=" + user_id.toString() + "&")
          case ScreenName(screen_name) =>
            optionsStr.append("screen_name=" + screen_name.toString() + "&")
          case Page(page) =>
            optionsStr.append("page=" + page.toString() + "&")
          case _ =>
            // Do nothing
        }
      }
      
      val (statusCode, body) =
        Scitter.execute(optionsStr.toString(), username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(body)

        val userListBuffer = new ListBuffer[User]

        for (n <- (responseXML \\ "user").elements)
          userListBuffer += (User.fromXml(n))
        
        userListBuffer.toList
      }
      else
      {
        Nil
      }
    }
    
    def show(id : Long) : Option[Status] =
    {
      val (statusCode, body) =
        Scitter.execute("http://twitter.com/statuses/show/" + id + ".xml", username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(body)
        
        Some(Status.fromXml(responseXML))
      }
      else
      {
        None
      }
    }

    /**
     *
     */
    def update(message : String, options : OptionalParam*) : Option[Status] =
    {
      def optionsToMap(options : List[OptionalParam]) : Map[String, String]=
      {
        options match
        {
          case hd :: tl =>
            hd match {
              case InReplyToStatusId(id) =>
                Map("in_reply_to_status_id" -> id.toString) ++ optionsToMap(tl)
              case _ =>
                optionsToMap(tl)
            }
          case List() => Map()
        }
      }
      
      val paramsMap = Map("status" -> message) ++ optionsToMap(options.toList)
    
      val (statusCode, body) =
        Scitter.execute("http://twitter.com/statuses/update.xml", paramsMap, username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(body)
        
        Some(Status.fromXml(responseXML))
      }
      else
      {
        None
      }
    }

    /**
     *
     */
    def destroy(id : Long) : Option[Status] =
    {
      val paramsMap = Map("id" -> id.toString())
    
      val (statusCode, body) =
        Scitter.execute("http://twitter.com/statuses/destroy/" + id.toString() + ".xml",
          paramsMap, username, password)
      if (statusCode == 200)
      {
        val responseXML = XML.loadString(body)
        
        Some(Status.fromXml(responseXML))
      }
      else if (statusCode == 400)
      {
        val responseXML = XML.loadString(body)
        
        /*
        Current testing indicates Twitter returns this:
        
<hash>
  <request>/statuses/destroy/3707808368.xml</request>
  <error>We could not delete that status for some reason.</error>
</hash>

        ...in the event that it can't delete a status.
         */
         
        val errorRequest = responseXML \ "hash" \ "request"
        val errorMessage = responseXML \ "hash" \ "error"
        
        // What to do with this?
        
        None
      }
      else
      {
        None
      }
    }
    def destroy(id : Id) : Option[Status] =
      destroy(id.id.toLong)

    /**
     * Twitter docs say:
     *
     * Returns extended information of a given user, specified by ID or screen
     * name as per the required id parameter below.  This information includes
     * design settings, so third party developers can theme their widgets
     * according to a given user's preferences.
     * URL: http://twitter.com/users/show/id.format
     * Formats: xml, json
     * Method(s): GET
     * Parameters:
     * One of the following is required:
     * * id.  The ID or screen name of a user.
     * Ex: http://twitter.com/users/show/12345.json or
     * http://twitter.com/users/show/bob.xml
     * * user_id.  Optional.  Specfies the ID of the user to return. Helpful
     * for disambiguating when a valid user ID is also a valid screen name.
     * Ex: http://twitter.com/users/show.xml?user_id=1401881
     * * screen_name.  Optional.  Specfies the screen name of the user to
     * return. Helpful for disambiguating when a valid screen name is also a
     * user ID.
     * Ex: http://twitter.com/users/show.xml?screen_name=101010
     * Returns: extended user information element
     */
    def userExtInfo(id : Id, options : OptionalParam*) : Option[ExtUser] =
    {
      None
    }
  }
}
