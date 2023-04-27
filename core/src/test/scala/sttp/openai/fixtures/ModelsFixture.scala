package sttp.openai.fixtures

object ModelsFixture {
  val responseJson: String =
    """{
      |  "object":"list",
      |  "data":[
      |    {
      |      "id":"babbage",
      |      "object":"model",
      |      "created":1649358449,
      |      "owned_by":"openai",
      |      "permission":[
      |        {
      |          "id":"modelperm-49FUp5v084tBB49tC4z8LPH5",
      |          "object":"model_permission",
      |          "created":1669085501,
      |          "allow_create_engine":false,
      |          "allow_sampling":true,
      |          "allow_logprobs":true,
      |          "allow_search_indices":false,
      |          "allow_view":true,
      |          "allow_fine_tuning":false,
      |          "organization":"*",
      |          "group":null,
      |          "is_blocking":false
      |        }
      |      ],
      |      "root":"babbage",
      |      "parent":null
      |    },
      |    {
      |      "id":"davinci",
      |      "object":"model",
      |      "created":1649359874,
      |      "owned_by":"openai",
      |      "permission":[
      |        {
      |          "id":"modelperm-U6ZwlyAd0LyMk4rcMdz33Yc3",
      |          "object":"model_permission",
      |          "created":1669066355,
      |          "allow_create_engine":false,
      |          "allow_sampling":true,
      |          "allow_logprobs":true,
      |          "allow_search_indices":false,
      |          "allow_view":true,
      |          "allow_fine_tuning":false,
      |          "organization":"*",
      |          "group":null,
      |          "is_blocking":false
      |        }
      |      ],
      |      "root":"davinci",
      |      "parent":null
      |    }
      |  ]
      |}""".stripMargin
}
