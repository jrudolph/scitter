package com.tedneward.scitter
{
  import scala.xml._

  /**
   *
   */
  abstract class RateLimits
  {
    val remainingHits : Long
    val hourlyLimit : Long
    val resetTime : java.util.Date
    val resetTimeInSeconds : Long
  }
  /**
   *
   */
  object RateLimits
  {
    def fromXml(node : Node) : RateLimits =
    {
      new RateLimits {
        val remainingHits = (node \ "remaining-hits").text.toLong
        val hourlyLimit = (node \ "hourly-limit").text.toLong
        val resetTime =
          new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'").parse((node \ "reset-time").text)
        val resetTimeInSeconds = (node \ "reset-time-in-seconds").text.toLong
      }
    }
  }

  /**
   * Nested User type. This could be combined with the top-level User type,
   * if we decide later that it's OK for this to have a boatload of optional
   * elements, including the most-recently-posted status update (which is a
   * tad circular).
   */
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
  /**
   * Object wrapper for transforming (format) into User instances.
   */
  object User
  {
    /*
    def fromAtom(node : Node) : Status =
    {
    
    }
    */
    /*
    def fromRss(node : Node) : Status =
    {
    
    }
    */
    def fromXml(node : Node) : User =
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
  /**
   * Extended user information elements contain detailed user-specified
   * information about a user's profile. 
   */
  abstract class ExtUser extends User
  {
    //val id : Long
    //val name : String
    //val screenName : String
    //val location : String
    //val description : String
    //val profileImageUrl : String
    //val url : String
    //val protectedUpdates : Boolean
    //val followersCount : Int
    val profileBackgroundColor : String
    val profileTextColor : String
    val profileLinkColor : String
    val profileSidebarFillColor : String
    val profileSidebarBorderColor : String
    val friendsCount : Int
    val createdAt : String
    val favouritesCount : String
    val utcOffset : String
    val timeZone : String
    val profileBackgroundImageUrl : String
    val profileBackgroundTile : String
    val following : Int
    val notifications : Int
    val statusesCount : Int
  }
  /**
   * Object wrapper for transforming (format) into ExtUser instances.
   */
  object ExtUser
  {
    /*
    def fromAtom(node : Node) : ExtUser =
    {
    
    }
    */
    /*
    def fromRss(node : Node) : ExtUser =
    {
    
    }
    */
    def fromXml(node : Node) : ExtUser =
    {
      new ExtUser {
        val id = (node \ "id").text.toLong
        val name = (node \ "name").text
        val screenName = (node \ "screen_name").text
        val description = (node \ "description").text
        val location = (node \ "location").text
        val profileImageUrl = (node \ "profile_image_url").text
        val url = (node \ "url").text
        val protectedUpdates = (node \ "protected").text.toBoolean
        val followersCount = (node \ "followers_count").text.toInt
        val profileBackgroundColor = (node \ "profile_background_color").text
        val profileTextColor = (node \ "profile_text_color").text
        val profileLinkColor = (node \ "profile_link_color").text
        val profileSidebarFillColor = (node \ "profile_sidebar_fill_color").text
        val profileSidebarBorderColor = (node \ "profile_sidebar_border_color").text
        val friendsCount = (node \ "friends_count").text.toInt
        val createdAt = (node \ "created_at").text
        val favouritesCount = (node \ "favourites_count").text
        val utcOffset = (node \ "utc_offset").text
        val timeZone = (node \ "time_zone").text
        val profileBackgroundImageUrl = (node \ "profile_background_image_url").text
        val profileBackgroundTile = (node \ "profile_background_tile").text
        val following = (node \ "following").text.toInt
        val notifications = (node \ "notifications").text.toInt
        val statusesCount = (node \ "statuses_count").text.toInt
      }
    }
  }
  /**
   * Status message type. This will typically be the most common message type
   * sent back from Twitter (usually in some kind of collection form). Note
   * that all optional elements in the Status type are represented by the
   * Scala Option[T] type, since that's what it's there for.
   */
  abstract class Status
  {
    val createdAt : String
    val id : Long
    val text : String
    val source : String
    val truncated : Boolean
    val inReplyToStatusId : Option[Long]
    val inReplyToUserId : Option[Long]
    val inReplyToScreenName : Option[String]
    val favorited : Boolean
    val user : User
  }
  /**
   * Object wrapper for transforming (format) into Status instances.
   */
  object Status
  {
    /*
    def fromAtom(node : Node) : Status =
    {
    
    }
    */
    /*
    def fromRss(node : Node) : Status =
    {
    
    }
    */
    def fromXml(node : Node) : Status =
    {
      /* As of 9 March 2009, an XML status message looks like:
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
       */
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
        val inReplyToScreenName = 
          if ((node \ "in_reply_to_screen_name").text != "")
            Some((node \"in_reply_to_screen_name").text)
          else
            None
        val favorited = (node \ "favorited").text.toBoolean
        val user = User.fromXml((node \ "user")(0))
      }
    }
  }
}