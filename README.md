# animeapplication
application for indexing stuff mainly anime, it can be serve by any server with this structure <br />
/info  ->[["name of the source","/tag_of_the_souce","url_of_the_source"],["name_of_2_source","/second_tag","2url"],....]<br />
/tag/latest ->[["url_download_latest_episode","Title","img_url"],......]<br />
/tag/q?q="searchterm" (without "")->[["url_series","title","img_url"],.......]<br />
/tag/dettagli?url="url_of_series"&chunk=chunk  (without ", a chunk is a group of 20 episode, if you do not want to implement it just put everything on chunk 0 and throw an error on other chunk)   -> [["url_download_episode","number of the episode"]]<br />
/tag/negi?url="url_of_serie" -> number of episode of the series (just an number ex: 13)<br />

