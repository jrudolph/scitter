package com.tedneward.scitter.test
{
  class ScitterTests
  {
    import org.junit._, Assert._
    import com.tedneward.scitter._

    def testUser = ""
    def testPassword = ""
    
    @Test def scitterTest =
    {
      val result = Scitter.test()
      assertTrue(result)
    }
    @Test def ipRateLimits =
    {
      val result = Scitter.rateLimitStatus()
      assertTrue(result.isDefined)
    }
    @Test def verifyCreds =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.verifyCredentials()
      assertTrue(result)
    }
    @Test def endSession =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.verifyCredentials()
      assertTrue(result)
      val result2 = scitter.endSession()
      assertTrue(result2)
    }
    @Test def userRateLimits =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.rateLimitStatus()
      assertTrue(result.isDefined)
    }
    @Test def scitterPublicTimeline =
    {
      val result = Scitter.publicTimeline()
      assertTrue(result.length > 0)
    }
    @Test def friendsTimeline =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.friendsTimeline()
      assertTrue(result.length > 0)
    }
    /* This only works if the test account has lots of friends
    @Test def friendsTimelineWithCount =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.friendsTimeline(Count(5))
      assertTrue(result.length == 5)
    }
    */
    @Test def userTimeline =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.userTimeline(Count(5))
      assertTrue(result.length == 5)
    }
    @Test def someOtherUserTimeline =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.userTimeline(Id("glaforge"), Count(5))
      assertTrue(result.length == 5)
    }
    @Test def friends =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.friends()
      assertTrue(result.length > 0)
    }
    /* This only works if the test account has lots of friends
    @Test def friendsWithPage =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.friends(Page(2))
      assertTrue(result.length > 0)
    }
    */
    /* This only works if the test account has followers
    @Test def followers =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.followers()
      assertTrue(result.length > 0)
    }
    */
    /* This only works if the test account has lots of followers
    @Test def followersWithPage =
    {
      val scitter = new Scitter(testUser, testPassword)
      val result = scitter.followers(Page(2))
      assertTrue(result.length > 0)
    }
    */
    @Test def update =
    {
      val scitter = new Scitter(testUser, testPassword)
      val message =
        "This is a post sent from the Scitter library on " +
        new java.util.Date().toString() +
        ". Expect it to be deleted #scittertests"
      val result = scitter.update(message)

      assertTrue(result.isDefined)
      if (result.isDefined)
        assertTrue(result.get.text == message)
    }
    @Test def updateWithReply =
    {
      val scitter = new Scitter(testUser, testPassword)
      val message =
        "This is a post sent from the Scitter library on " +
        new java.util.Date().toString() +
        ". Expect it to be deleted #scittertests"
      val result = scitter.update(message)

      assertTrue(result.isDefined)
      if (result.isDefined)
      {
        assertTrue(result.get.text == message)
        
        val message2 =
          "This is a reply sent to @" + testUser + " in response to message " +
          result.get.id + ". " +
          "Expect it to be deleted. #scittertests"
        val result2 = scitter.update(message2, InReplyToStatusId(result.get.id))
        assertTrue(result2.isDefined)
        if (result2.isDefined)
        {
          assertTrue(result2.get.text == message2)
        }
      }
    }
    @Test def updateAndDestroy =
    {
      val scitter = new Scitter(testUser, testPassword)
      val message =
        "This is a post sent from the Scitter library on " +
        new java.util.Date().toString() +
        ". Expect it to be deleted #scittertests"
      val result = scitter.update(message)

      assertTrue(result.isDefined)
      if (result.isDefined)
      {
        System.out.println("Updated status = " + result.get.id)
        
        assertTrue(result.get.text == message)
        
        val id = result.get.id
        
        System.out.println("Attempting to find status " + id)
        val findResult = scitter.show(id)
        assertTrue(findResult.isDefined)
        if (findResult.isDefined)
        {
          System.out.println("Attempting to destroy id " + id)
          val result2 = scitter.destroy(id)
          // As of this writing, Twitter's destroy API is broken and fails
          // this test miserably. Uncomment when we can determine that the
          // Twitter API isn't broken any more (sigh)
          //
          //assertTrue(result2.isDefined)
          //assertTrue(result2.get.text == message)
          ()
        }
      }
    }
  }

  class ExplorationTests
  {
    import org.junit._, Assert._
    
    @Test def simpleDateParse =
    {
      val dateString = "2009-09-02T08:22:59+00:00"
      
      val date =
        new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'").parse(dateString)
      assertTrue(date.getYear() == 109)
    }
    
    @Test def simpleAtomParse =
    {
      val atom =
        <feed xml:lang="en-US" xmlns="http://www.w3.org/2005/Atom">
          <title>Twitter / tedneward</title>
          <id>tag:twitter.com,2007:Status</id>
          <link type="text/html" rel="alternate" href="http://twitter.com/tedneward"/>
          <updated>2009-03-07T13:48:31+00:00</updated>
          <subtitle>Twitter updates from Ted Neward / tedneward.</subtitle>
          <entry>
            <title>tedneward: @kdellison Happens to the best of us...</title>
            <content type="html">tedneward: @kdellison Happens to the best of us...</content>
            <id>tag:twitter.com,2007:http://twitter.com/tedneward/statuses/1292396349</id>
            <published>2009-03-07T11:07:18+00:00</published>
            <updated>2009-03-07T11:07:18+00:00</updated>
            <link type="text/html" rel="alternate" href="http://twitter.com/tedneward/statuses/1292396349"/>
            <link type="image/png" rel="image" href="http://s3.amazonaws.com/twitter_production/profile_images/55857457/javapolis_normal.png"/>
            <author>
              <name>Ted Neward</name>
              <uri>http://www.tedneward.com</uri>
            </author>
          </entry>
        </feed>
    
      assertEquals(atom \\ "entry" \ "author" \ "name", "Ted Neward")
    }

    import org.apache.commons.httpclient._, methods._, params._, cookie._, auth._
    
    def testUser = "TedNeward"
	def testPassword = "s5z1nn2"
    
    @Test def tuplesToNVP =
    {
      val map = Map("one" -> "1", "two" -> "2")

      val array = new Array[NameValuePair](map.size)
      var pos = 0
      map.elements.foreach { (pr) =>
        pr match {
          case (k, v) => array(pos) = new NameValuePair(k, v)
        }
        pos = pos + 1
      }

      assertEquals(array.size, map.size)
      assertEquals(array(0).getName(), "one")
      assertEquals(array(0).getValue(), "1")
      assertEquals(array(1).getName(), "two")
      assertEquals(array(1).getValue(), "2")
    }
    
    @Test def callTwitterTest =
    {
      val testURL = "http://twitter.com/help/test.xml"
      
      // HttpClient API 101
      val client = new HttpClient()
      val method = new GetMethod(testURL)

      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        new DefaultHttpMethodRetryHandler(3, false))

      client.executeMethod(method)
      
      val statusLine = method.getStatusLine()
      
      assertEquals(200, statusLine.getStatusCode())
      assertEquals("OK", statusLine.getReasonPhrase())
    }
    /*
    @Test def verifyCreds =
    {
      val client = new HttpClient()

      val verifyCredsURL = "http://twitter.com/account/verify_credentials.xml"
      val method = new GetMethod(verifyCredsURL)

      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        new DefaultHttpMethodRetryHandler(3, false))
      client.getParams().setAuthenticationPreemptive(true)
      val defaultcreds = new UsernamePasswordCredentials(testUser, testPassword)
      client.getState().setCredentials(
        new AuthScope("twitter.com", 80, AuthScope.ANY_REALM), defaultcreds)
      
      client.executeMethod(method)
      
      val statusLine = method.getStatusLine()
      
      assertEquals(200, statusLine.getStatusCode())
      assertEquals("OK", statusLine.getReasonPhrase())
    }
    */

    /*
    @Test def callTwitterPublicTimeline =
    {
      val publicFeedURL = "http://twitter.com/statuses/public_timeline.xml"
      
      // HttpClient API 101
      val client = new HttpClient()
      val method = new GetMethod(publicFeedURL)
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        new DefaultHttpMethodRetryHandler(3, false))
      
      client.executeMethod(method)
      
      val statusLine = method.getStatusLine()
      assertEquals(statusLine.getStatusCode(), 200)
      assertEquals(statusLine.getReasonPhrase(), "OK")
      
      val responseBody = method.getResponseBodyAsString()
      System.out.println("callTwitterPublicTimeline got... ")
      System.out.println(responseBody)
    }
    // */
    /*
    @Test def simplePublicFeedPullAndParse =
    {
      val publicFeedURL = "http://twitter.com/statuses/public_timeline.xml"
      
      // HttpClient API 101
      val client = new HttpClient()
      val method = new GetMethod(publicFeedURL)
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        new DefaultHttpMethodRetryHandler(3, false))
      val statusCode = client.executeMethod(method)
      val responseBody = new String(method.getResponseBody())
      
      val responseXML = scala.xml.XML.loadString(responseBody)
      val statuses = responseXML \\ "status"

      for (n <- statuses.elements)
      {
        n match
        {
          case <status>{ contents @ _*}</status> =>
          {
            System.out.println("Status: ")
            contents.foreach((c) =>
              c match
              {
                case <text>{ t @ _*}</text> =>
                  System.out.println("\tText: " + t.text.trim)
                case <user>{ contents2 @ _* }</user> =>
                {
                  contents2.foreach((c2) =>
                    c2 match
                    {
                      case <screen_name>{ u }</screen_name> =>
                        System.out.println("\tUser: " + u.text.trim)
                      case _ => ()
                    }
                  )
                }
                case _ => ()
              }
            )
          }
          case _ =>
            System.out.println("Unrecognized element!")
        }
      }
    }
    // */
    /*
    @Test def simplePublicFeedPullAndDeserialize =
    {
      val publicFeedURL = "http://twitter.com/statuses/public_timeline.xml"
      
      // HttpClient API 101
      val client = new HttpClient()
      val method = new GetMethod(publicFeedURL)
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        new DefaultHttpMethodRetryHandler(3, false))
      val statusCode = client.executeMethod(method)
      val responseBody = new String(method.getResponseBody())
      
      val responseXML = scala.xml.XML.loadString(responseBody)
      val statuses = responseXML \\ "status"

      for (n <- statuses.elements)
      {
        val s = Status.fromXml(n)
        System.out.println("\t'@" + s.user.screenName + "' wrote " + s.text)
      }
    }
    // */
    //*
    @Test def userFollowersAndDeserialize =
    {
      val followerFeedURL = "http://twitter.com/statuses/friends_timeline.xml"
      
      val client = new HttpClient()
      val method = new GetMethod(followerFeedURL)
      
      method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, 
        new DefaultHttpMethodRetryHandler(3, false))

      client.getParams().setAuthenticationPreemptive(true)
      val defaultcreds = new UsernamePasswordCredentials(testUser, testPassword)
      client.getState().setCredentials(
        new AuthScope("twitter.com", 80, AuthScope.ANY_REALM), defaultcreds)

      val statusCode = client.executeMethod(method)
      val responseBody = new String(method.getResponseBody())
      
      val responseXML = scala.xml.XML.loadString(responseBody)

      val statuses = responseXML \ "status"
      for (n <- statuses.elements)
      {
        n match
        {
          case <status>{ contents @ _*}</status> =>
          {
            contents.foreach((c) =>
              c match
              {
                case <text>{ t @ _*}</text> =>
                {
                  System.out.println("\tText: " + t.text.trim)
                }
                case <user>{ contents2 @ _* }</user> =>
                {
                  contents2.foreach((c2) =>
                    c2 match
                    {
                      case <screen_name>{ u }</screen_name> =>
                        System.out.println("\tUser: " + u.text.trim)
                      case _ => ()
                    }
                  )
                }
                case _ => ()
              }
            )
          }
          case _ =>
            System.out.println("Unrecognized element!")
        }
      }
    }







    
    import scala.xml._
    
    /*
    @Test def xmlMatch =
    {
      val xmlSample =
        <phonebook>
          <descr>
            This is the <b>phonebook</b> of the
            <a href="http://acme.org">ACME</a> corporation.
          </descr>
          <entry>
            <name>Burak</name>
            <phone where="work">  +41 21 693 68 67</phone>
            <phone where="mobile">+41 79 602 23 23</phone>
          </entry>
        </phonebook>;
        
      def parse(seq : NodeSeq) =
      {
        for (n <- seq.elements)
        {
          n match
          {
            case <descr>{ contents @ _*}</descr> =>
            {
              System.out.println("Description: " + contents.text)
            }
            case <entry>{ contents @ _*}</entry> =>
            {
              System.out.println("Entry: ")
              contents.foreach((c) =>
                c match
                {
                  case <name>{ t @ _*}</name> => System.out.println("\tName: " + t.text)
                  case <phone>{ p }</phone> => System.out.println("\tPhone: " + p.text.trim)
                  case _ => ()
                }
              )
            }
            case _ => System.out.println("???? => " + n)
          }
        }
      }
      
      parse(xmlSample \ "_")
    }
    */


    abstract class Status
    {
      abstract class User
      {
        val id : Long
        val name : String
        val screenName : String
        val description : String
        val location : String
        val profileImageUrl : String
        val url : String
        val protectedUpdates : Boolean
        val followersCount : Int
      }
      object User
      {
        def fromXml(node : scala.xml.Node) : User =
        {
          new User {
            val id = (node \ "id").text.toLong
            val name = (node \ "name").text
            val screenName = (node \ "screen_name").text
            val description = (node \ "description").text
            val location = (node \ "location").text
            val profileImageUrl = (node \ "profile_image_url").text
            val url = (node \ "url").text
            val protectedUpdates = (node \ "protected").text.toBoolean
            val followersCount = (node \ "followers_count").text.toInt
          }
        }
      }
    
      val createdAt : String
      val id : Long
      val text : String
      val source : String
      val truncated : Boolean
      val inReplyToStatusId : Option[Long]
      val inReplyToUserId : Option[Long]
      val favorited : Boolean
      val user : User
    }
    object Status
    {
      def fromXml(node : scala.xml.Node) : Status =
      {
        new Status {
          val createdAt = (node \ "created_at").text
          val id = (node \ "id").text.toLong
          val text = (node \ "text").text
          val source = (node \ "source").text
          val truncated = (node \ "truncated").text.toBoolean
          val inReplyToStatusId =
            if ((node \ "in_reply_to_status_id").text != "")
              Some((node \"in_reply_to_status_id").text.toLong)
            else
              None
          val inReplyToUserId = 
            if ((node \ "in_reply_to_user_id").text != "")
              Some((node \"in_reply_to_user_id").text.toLong)
            else
              None
          val favorited = (node \ "favorited").text.toBoolean
          val user = User.fromXml((node \ "user")(0))
        }
      }
    }
    
    @Test def parseStatuses =
    {
      val sourceFeed =
        <statuses type="array">
          <status>
            <created_at>Tue Mar 10 03:14:54 +0000 2009</created_at>
            <id>1303777336</id>
            <text>She really is. http://tinyurl.com/d65hmj</text>
            <source>&lt;a href=&quot;http://iconfactory.com/software/twitterrific&quot;&gt;twitterrific&lt;/a&gt;</source>
            <truncated>false</truncated>
            <in_reply_to_status_id></in_reply_to_status_id>
            <in_reply_to_user_id></in_reply_to_user_id>
            <favorited>false</favorited>
            <user>
              <id>18729101</id>
              <name>Brittanie</name>
              <screen_name>brittaniemarie</screen_name>
              <description>I'm a bright character. I suppose.</description>
              <location>Atlanta or Philly.</location>
              <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/81636505/goodish_normal.jpg</profile_image_url>
              <url>http://writeitdowntakeapicture.blogspot.com</url>
              <protected>false</protected>
              <followers_count>61</followers_count>
            </user>
          </status>
          <status>
            <created_at>Tue Mar 10 03:14:57 +0000 2009</created_at>
            <id>1303777334</id>
            <text>Number 2 of my four life principles.     &quot;Life is fun and rewarding&quot;</text>
            <source>web</source>
            <truncated>false</truncated>
            <in_reply_to_status_id></in_reply_to_status_id>
            <in_reply_to_user_id></in_reply_to_user_id>
            <favorited>false</favorited>
            <user>
              <id>21465465</id>
              <name>Dale Greenwood</name>
              <screen_name>Greeendale</screen_name>
              <description>Vegetarian. Eat and use only organics. Love helping people become prosperous</description>
              <location>Melbourne Australia</location>
              <profile_image_url>http://s3.amazonaws.com/twitter_production/profile_images/90659576/Dock_normal.jpg</profile_image_url>
              <url>http://www.4abundance.mionegroup.com</url>
              <protected>false</protected>
              <followers_count>15</followers_count>
            </user>
          </status>
        </statuses>;
        
      sourceFeed match
      {
        case <statuses>{ statuses @ _*}</statuses> =>
          for (stat @ <status>{_*}</status> <- statuses)
          {
            val s = Status.fromXml(stat)
            System.out.println("Received: " + s.text + " from " + s.user.screenName)
          }
      }
      
      ()
    }
  }
}
