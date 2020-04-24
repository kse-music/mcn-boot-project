<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta http-equiv="x-ua-compatible" content="IE=edge">
  <title>Swagger UI</title>
  <link rel="icon" type="image/png" href="${cdn}images/favicon-32x32.png" sizes="32x32" />
  <link rel="icon" type="image/png" href="${cdn}images/favicon-16x16.png" sizes="16x16" />
  <link href='${cdn}css/typography.css' media='screen' rel='stylesheet' type='text/css'/>
  <link href='${cdn}css/reset.css' media='screen' rel='stylesheet' type='text/css'/>
  <link href='${cdn}css/screen.css' media='screen' rel='stylesheet' type='text/css'/>
  <link href='${cdn}css/reset.css' media='print' rel='stylesheet' type='text/css'/>
  <link href='${cdn}css/print.css' media='print' rel='stylesheet' type='text/css'/>

  <script src='${cdn}lib/object-assign-pollyfill.js' type='text/javascript'></script>
  <script src='${cdn}lib/jquery-1.8.0.min.js' type='text/javascript'></script>
  <script src='${cdn}lib/jquery.slideto.min.js' type='text/javascript'></script>
  <script src='${cdn}lib/jquery.wiggle.min.js' type='text/javascript'></script>
  <script src='${cdn}lib/jquery.ba-bbq.min.js' type='text/javascript'></script>
  <script src='${cdn}lib/handlebars-4.0.5.js' type='text/javascript'></script>
  <script src='${cdn}lib/lodash.min.js' type='text/javascript'></script>
  <script src='${cdn}lib/backbone-min.js' type='text/javascript'></script>
  <script src='${cdn}js/swagger-ui.js' type='text/javascript'></script>
  <script src='${cdn}lib/highlight.9.1.0.pack.js' type='text/javascript'></script>
  <script src='${cdn}lib/highlight.9.1.0.pack_extended.js' type='text/javascript'></script>
  <script src='${cdn}lib/jsoneditor.min.js' type='text/javascript'></script>
  <script src='${cdn}lib/marked.js' type='text/javascript'></script>
  <script src='${cdn}lib/swagger-oauth.js' type='text/javascript'></script>

  <!-- Some basic translations -->
  <script src='${cdn}lang/translator.js' type='text/javascript'></script>
  <!-- <script src='lang/ru.js' type='text/javascript'></script> -->
  <script src='${cdn}lang/zh-cn.js' type='text/javascript'></script>

  <script type="text/javascript">
    $(function () {
      var url = window.location.search.match(/url=([^&]+)/);
      if (url && url.length > 1) {
        url = decodeURIComponent(url[1]);
      } else {
        url = window.location.protocol+"//${host}${path}/swagger.json";
      }

      hljs.configure({
        highlightSizeThreshold: 5000
      });

      // Pre load translate...
      if(window.SwaggerTranslator) {
        window.SwaggerTranslator.translate();
      }
      window.swaggerUi = new SwaggerUi({
        url: url,
        dom_id: "swagger-ui-container",
        supportedSubmitMethods: ['get', 'post', 'put', 'delete', 'patch'],
        onComplete: function(swaggerApi, swaggerUi){
          if(typeof initOAuth == "function") {
            initOAuth({
              clientId: "your-client-id",
              clientSecret: "your-client-secret-if-required",
              realm: "your-realms",
              appName: "your-app-name",
              scopeSeparator: " ",
              additionalQueryStringParams: {}
            });
          }

          if(window.SwaggerTranslator) {
            window.SwaggerTranslator.translate();
          }
        },
        onFailure: function(data) {
          log("Unable to Load SwaggerUI");
        },
        docExpansion: "none",
        jsonEditor: false,
        defaultModelRendering: 'schema',
        showRequestHeaders: false,
        showOperationIds: false
      });

      window.swaggerUi.load();

      function log() {
        if ('console' in window) {
          console.log.apply(console, arguments);
        }
      }
  });
  </script>
   <style type="text/css">
    #validator,#input_baseUrl,#explore{
      display: none!important;
    }
  </style>
</head>

<body class="swagger-section">
<div id='header'>
  <div class="swagger-ui-wrap">
    <a id="logo" href="https://github.com/kse-music"><img class="logo__img" alt="swagger" height="30" width="30" src="${cdn}images/logo_small.png" /><span class="logo__title">Swagger</span></a>
    <form id='api_selector'>
      <div class='input'><input placeholder="http://example.com/api" id="input_baseUrl" name="baseUrl" type="text"/></div>
      <div id='auth_container'></div>
      <div class='input'><a id="explore" class="header__btn" href="#" data-sw-translate>Explore</a></div>
    </form>
  </div>
</div>

<div id="message-bar" class="swagger-ui-wrap" data-sw-translate>&nbsp;</div>
<div id="swagger-ui-container" class="swagger-ui-wrap"></div>
</body>
</html>
