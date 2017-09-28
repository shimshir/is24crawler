package com.admir.is24crawler.util

import ch.qos.logback.core.AppenderBase

class InMemoryAppender[E] extends AppenderBase[E] {
  def append(event: E): Unit = {
    // ignore for now
  }
}
