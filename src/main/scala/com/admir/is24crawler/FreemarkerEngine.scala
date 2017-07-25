package com.admir.is24crawler

import java.io.StringWriter
import collection.JavaConverters._

import freemarker.cache.ClassTemplateLoader
import freemarker.template.{Configuration, Version}

class FreemarkerEngine {
  val cfg = new Configuration(new Version(2, 3, 23))
  cfg.setTemplateLoader(new ClassTemplateLoader(this.getClass, "/templates"))

  def renderUri(templateUri: String, model: Map[String, Any]): String = {
    val template = cfg.getTemplate(templateUri)
    val outputWriter = new StringWriter
    template.process(model.asJava, outputWriter)
    outputWriter.toString
  }
}
