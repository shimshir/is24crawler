package com.admir.is24crawler.models

case class Expose(
                   priceValue: Double,
                   priceStr: String,
                   pageLink: String,
                   imageLinks: Seq[String]
                 )
