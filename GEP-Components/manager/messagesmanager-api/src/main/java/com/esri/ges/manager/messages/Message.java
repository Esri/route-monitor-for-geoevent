package com.esri.ges.manager.messages;

import java.util.Date;

public class Message
{
  final public static String MESSAGE_ID_KEY   = "MESSAGE_ID";
  final public static String TYPE_KEY         = "TYPE";
  final public static String SUBJECT_KEY      = "SUBJECT";
  final public static String MESSAGE_TIME_KEY = "MESSAGE_TIME";
  final public static String MESSAGE_FROM_KEY = "MESSAGE_FROM";
  final public static String MESSAGE_TO_KEY   = "MESSAGE_TO";
  final public static String STATUS_KEY       = "STATUS";
  final public static String MESSAGE_BODY_KEY         = "MESSAGE_BODY";
  final public static String CALLBACK_KEY     = "CALLBACK";
  
  private String id;
  private String type;
  private String subject;
  private Date messageTime;
  private String messageFrom;
  private String messageTo;
  private String status;
  private String messageBody;
  private String callback;
  
  public Message()
  {
    id = java.util.UUID.randomUUID().toString();
    messageTime = new Date();
    status = MessageStatus.Open.toString();
    
    // init
    type="";
    subject="";
    messageFrom="";
    messageTo="";
    messageBody="";
    callback="";
  }

  public String getType()
  {
    return type;
  }

  public void setType(String type)
  {
    this.type = type;
  }

  public String getSubject()
  {
    return subject;
  }

  public void setSubject(String subject)
  {
    this.subject = subject;
  }

  public String getMessageFrom()
  {
    return messageFrom;
  }

  public void setMessageFrom(String messageFrom)
  {
    this.messageFrom = messageFrom;
  }

  public String getMessageTo()
  {
    return messageTo;
  }

  public void setMessageTo(String messageTo)
  {
    this.messageTo = messageTo;
  }

  public String getStatus()
  {
    return status;
  }

  public void setStatus(String status)
  {
    this.status = status;
  }

  public String getMessageBody()
  {
    return messageBody;
  }

  public void setMessageBody(String messageBody)
  {
    this.messageBody = messageBody;
  }

  public String getCallback()
  {
    return callback;
  }

  public void setCallback(String callback)
  {
    this.callback = callback;
  }
  
  public String getId()
  {
    return id;
  }

  public Date getMessageTime()
  {
    return messageTime;
  }

}
