package com.tedneward.scitter.test;

import org.junit.*;
import com.tedneward.scitter.*;

public class JavaScitterTests
{
  public static final String testUser = "";
  public static final String testPassword = "";
  
  @Test public void getFriendsStatuses()
  {
    Scitter scitter = new Scitter(testUser, testPassword);
    if (scitter.verifyCredentials())
    {
      scala.List statuses = scitter.friendsTimeline(new scala.collection.mutable.ListBuffer());
      Assert.assertTrue(statuses.length() > 0);
      
      for (int i=0; i<statuses.length(); i++)
      {
        Status stat = (Status)statuses.apply(i);
        System.out.println(stat.user().screenName() + " said " + stat.text());
      }
    }
    else
      Assert.assertTrue(false);
  }
  @Test public void getFriendsStatusesWithCount()
  {
    Scitter scitter = new Scitter(testUser, testPassword);
    if (scitter.verifyCredentials())
    {
      scala.collection.mutable.ListBuffer params =
        new scala.collection.mutable.ListBuffer();
      params.$plus$eq(new Count(5));
      
      scala.List statuses = scitter.friendsTimeline(params);

      Assert.assertTrue(statuses.length() > 0);
      Assert.assertTrue(statuses.length() == 5);
      
      for (int i=0; i<statuses.length(); i++)
      {
        Status stat = (Status)statuses.apply(i);
        System.out.println(stat.user().screenName() + " said " + stat.text());
      }
    }
    else
      Assert.assertTrue(false);
  }
}