import java.net.*;
import java.io.*;

public class Site {

  /**
   * Gera e retorna o HTML e CSS da página inicial com o formulário de busca.
   *
   * @return HTML e CSS em formato de {@code String}.
   */
  public static String getHtmlForm() {
    return "<!DOCTYPE html>\n" +
        "<html lang=\"pt-BR\">\n" +
        "<head>\n" +
        "    <meta charset=\"UTF-8\">\n" +
        "    <title>BuscaFilmes</title>\n" +
        "    <style>\n" +
        "        @keyframes slip-fade { 0% { opacity: 0; transform: translateY(20px); } 100% { opacity: 1; transform: translateY(0); } }\n"
        +
        "        body {\n" +
        "            margin: 0;\n" +
        "            height: 100vh;\n" +
        "            display: flex;\n" +
        "            flex-direction: column;\n" +
        "            align-items: center;\n" +
        "            justify-content: center;\n" +
        "            background: #202020;\n" +
        "            color: #f0f0f0;\n" +
        "            font-family: 'Montserrat', sans-serif;\n" +
        "            animation: slip-fade 1s ease-in-out;\n" +
        "            text-align: center;\n" +
        "        }\n" +
        "        input[type='text'] {\n" +
        "            width: 130px;\n" +
        "            transition: width 0.4s ease-in-out;\n" +
        "            padding: 5px;\n" +
        "            margin: 5px;\n" +
        "        }\n" +
        "        input[type='text']:focus { width: 200px; }\n" +
        "        input[type='checkbox'] { margin: 15px; }\n" +
        "        button[type='submit'] {\n" +
        "            background-color: #cd3b3b;\n" +
        "            color: #f0f0f0;\n" +
        "            padding: 7px 15px;\n" +
        "            border: none;\n" +
        "            border-radius: 15px;\n" +
        "            font-weight: bold;\n" +
        "            transition: background-color 0.3s;\n" +
        "        }\n" +
        "        button[type='submit']:hover { background-color: #a53131; }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <h1>BuscaFilmes</h1>\n" +
        "    <form action=\"/buscar\" method=\"GET\">\n" +
        "        <input type=\"text\" name=\"t\" placeholder=\"Nome do filme\" required><br>\n" +
        "        <input type=\"checkbox\" name=\"plot\">\n" +
        "        <label for=\"plot\">Sinopse Resumida?</label><br>\n" +
        "        <button type=\"submit\">Buscar</button>\n" +
        "    </form>\n" +
        "</body>\n" +
        "</html>";
  }

  /**
   * Monta o HTML e o CSS para exibir os dados do filme a partir do JSON response
   * da API.
   *
   * @param json o conteúdo do JSON com os dados dos filmes.
   * @return HTML e CSS formatado contendo informações como título, diretor,
   *         elenco, etc.
   */
  public static String HTMLResponseForm(String json) {
    String title = Cliente.extractFieldJSON(json, "Title");
    String year = Cliente.extractFieldJSON(json, "Year");
    String director = Cliente.extractFieldJSON(json, "Director");
    String actors = Cliente.extractFieldJSON(json, "Actors");
    String plot = Cliente.extractFieldJSON(json, "Plot");
    String poster = Cliente.extractFieldJSON(json, "Poster");

    StringBuilder sb = new StringBuilder();
    sb.append("<!DOCTYPE html>\n");
    sb.append("<html lang=\"pt-BR\">\n<head>\n<meta charset=\"UTF-8\">\n");
    sb.append("<title>").append(title).append("</title>\n");
    sb.append("<style>\n" +
        "@keyframes slip-fade{0%{bottom:20px; opacity: 0}100%{bottom:0px; opacity: 1}}" +
        "body{ position: relative; background: #202020; color:#f0f0f0; text-align: center; font-family:'Montserrat'; animation: slip-fade 1s ease-in-out;}"
        +
        "a{text-decoration: none; color: #f0f0f0; font-weight: bold;}");
    sb.append("</style>\n");
    sb.append("</head>\n<body>\n");
    sb.append("<h1>").append(title).append(" (").append(year).append(")</h1>\n");

    if (!poster.equals("N/A")) {
      sb.append("<img src=\"").append(poster).append("\" alt=\"Poster do filme\" style=\"max-width:300px;\"><br>\n");
    } else {
      sb.append("<p>Poster não disponível.</p>\n");
    }

    sb.append("<p><b>Diretor:</b> ").append(director).append("</p>\n");
    sb.append("<p><b>Elenco:</b> ").append(actors).append("</p>\n");
    sb.append("<p><b>Sinopse:</b> ").append(plot).append("</p>\n");
    sb.append("<p><a href=\"/\">Nova busca</a></p>\n");
    sb.append("</body>\n</html>");
    return sb.toString();
  }
}
