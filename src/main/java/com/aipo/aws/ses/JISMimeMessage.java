/*
 * This file is part of the com.aipo.aws package.
 * Copyright (C) 2004-2011 Aimluck,Inc.
 * http://www.aipo.com
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.aipo.aws.ses;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.amazonaws.services.simpleemail.model.RawMessage;

/**
 *
 */
public class JISMimeMessage {

  protected MimeMessage delegate = null;

  public JISMimeMessage() {
    this(Session.getInstance(new Properties(), null));
  }

  /**
   * 
   * @param session
   */
  public JISMimeMessage(Session session) {
    delegate = new MimeMessage(session);
  }

  /**
   * 
   * @param from
   * @throws MessagingException
   */
  public void setFrom(String from) throws MessagingException {
    delegate.setFrom(new InternetAddress(from));
  }

  /**
   * 
   * @param name
   * @param from
   * @throws MessagingException
   */
  public void setFrom(String name, String from) throws MessagingException {
    String source = from;
    try {
      source = SES.encodeSource(name, from);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
    delegate.setFrom(new InternetAddress(source));
  }

  /**
   * 
   * @param recipientType
   * @param recipients
   * @throws MessagingException
   */
  public void setRecipients(RecipientType recipientType, String... recipients)
      throws MessagingException {
    if (recipients == null) {
      return;
    }
    int size = recipients.length;
    InternetAddress[] address = new InternetAddress[recipients.length];
    for (int i = 0; i < size; i++) {
      try {
        address[i] = getInternetAddress(recipients[i]);
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
    delegate.setRecipients(recipientType, address);
  }

  /**
   * 
   * @param subject
   * @throws MessagingException
   */
  public void setSubject(String subject) throws MessagingException {
    delegate.setSubject(subject, "ISO-2022-JP");
  }

  /**
   * 
   * @param text
   * @throws MessagingException
   */
  public void setTextContent(String text) throws MessagingException {
    delegate.setText(text, "ISO-2022-JP");
    delegate.setHeader("Content-Transfer-Encoding", "7bit");
  }

  /**
   * 
   * @return
   */
  public RawMessage getRawMessage() {
    RawMessage data = new RawMessage();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      delegate.writeTo(out);
      data.setData(ByteBuffer.wrap(out.toString().getBytes()));
    } catch (Throwable t) {
      throw new RuntimeException(t);
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (Throwable ignore) {
          // ignore
        }
      }
    }
    return data;
  }

  /**
   * 
   * @param addr
   * @return
   * @throws AddressException
   * @throws UnsupportedEncodingException
   */
  protected InternetAddress getInternetAddress(String addr)
      throws AddressException, UnsupportedEncodingException {
    InternetAddress address = null;
    StringTokenizer st = new StringTokenizer(addr, "<>");
    int count = st.countTokens();
    if (count <= 0) {
      return null;
    } else if (count == 1) {
      address = new InternetAddress(st.nextToken().trim());
    } else if (count == 2) {
      String name = st.nextToken().trim();
      String addressStr = st.nextToken().trim();
      address = new InternetAddress(addressStr, SES.encodeWordJIS(name));
    }
    return address;
  }
}
