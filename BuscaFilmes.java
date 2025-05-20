import java.io.*;
import java.net.*;

/**
 * A classe {@code BuscaFilmes} implementa um servidor HTTP simples que escuta
 * requisições da porta 80. O servidor disponibiliza uma interface web para
 * busca
 * de informações de filmes utilizando a API pública da OMDb (Open Movie
 * Database).
 *
 * O servidor interpreta reqquisições GET para a raiz ("/") exibindo um
 * formulário de busca,
 * e para o caminho "buscar?t=Titulo", retorna informações detalhadas sobre o
 * filme.
 * Podendo ainda escolher se vai usar o caminho com plot completo
 * ou simplificado.
 * Endpoint de busca:
 * /buscar?t=Titulo&amp;plot=full
 *
 * Requests e Response são manipulados manualmente utilizando sockets.
 * A resposta é construída em HTML, e o JSON é tratado como texto.
 * 
 * @author Emanuel Rawã Gurgel de Araújo Freitas
 * @author José Ivo Schwade Araújo
 * @version 1.0
 */
public class BuscaFilmes {
  /**
   * Porta onde o servidor irá executar as conexões.
   */
  private static final int PORT = 80;
  /**
   * Chave da API da OMDb (máximo de 1000 requisições por dia)
   */
  private static final String OMDB_API_KEY = "da70d648";

  /**
   * Método principal que inicializa o servidor e escuta conexões de clientes
   *
   * @param args não utilizado
   * @throws IOException se ocorrer erro ao abrir o socket do servidor
   */
  public static void main(String[] args) throws IOException {
    System.out.println("Servidor rodando na porta " + PORT + "...");
    ServerSocket serverSocket = new ServerSocket(PORT);

    while (true) {
      Socket client = serverSocket.accept();
      new Thread(() -> handleClient(client)).start();
    }
  }

  /**
   * Lida com a requisição de um cliente individual.
   * Interpreta os dados da requisição HTTP e envia a resposta correspondente.
   *
   * @param client o socket do cliente conectado.
   */
  private static void handleClient(Socket client) {
    try (
        BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        OutputStream out = client.getOutputStream();
        PrintWriter writer = new PrintWriter(out, true);) {
      String line = in.readLine();
      if (line == null || line.isEmpty()) {
        client.close();
        return;
      }

      String[] requestParts = line.split(" ");
      String method = requestParts[0];
      String path = requestParts[1];

      // Lê as demais linhas do header até linha vazia
      while (!(line = in.readLine()).isEmpty()) {
      }

      if (method.equals("GET") && path.equals("/")) {
        // Página inicial com formulário de busca
        sendHttpResponse(writer, "text/html", getHtmlForm());
      } else if (method.equals("GET") && path.startsWith("/buscar")) {
        String query = path.substring(path.indexOf('?') + 1);
        String titulo = null;
        boolean sinopseResumida = false;
        for (String param : query.split("&")) {
          String[] keyVal = param.split("=");
          if (keyVal.length == 2 && keyVal[0].equals("t")) {
            titulo = URLDecoder.decode(keyVal[1], "UTF-8");
          }
          if (keyVal[0].equals("plot")) {
            sinopseResumida = true;
          }
        }

        if (titulo == null || titulo.isEmpty()) {
          sendHttpResponse(writer, "text/html", "<h1>Parâmetro 't' (título) não informado</h1>");
          client.close();
          return;
        }

        String json = consultOMDBAPI(titulo, sinopseResumida);
        String html = HTMLResponseForm(json);
        sendHttpResponse(writer, "text/html; charset=UTF-8", html);
      } else {
        sendHttpResponse(writer, "text/html", "<h1>404 Not Found</h1>");
      }

      client.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Envia uma resposta HTTP simples com cabeçalhos e conteúdo
   *
   * @param writer      o {@link PrintWriter} para enviar dados do cliente.
   * @param contentType o tipo do conteúdo (ex.: "text/html").
   * @param content     o conteúdo da resposta.
   */
  private static void sendHttpResponse(PrintWriter writer, String contentType, String content) {
    writer.print("HTTP/1.1 200 OK\r\n");
    writer.print("Content-Type: " + contentType + "\r\n");
    writer.print("Connection: close\r\n");
    writer.print("\r\n");
    writer.print(content);
    writer.flush();
  }

  /**
   * Gera e retorna o HTML e CSS da página inicial com o formulário de busca.
   *
   * @return HTML e CSS em formato de {@code String}.
   */
  private static String getHtmlForm() {
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
   * Consulta a OMDb API com base no título do filme e se deve mostrar a sinópse
   * resumida ou não.
   *
   * @param titulo          o título do filme a ser buscado.
   * @param sinopseResumida o {@code Boolean} que verifica o tipo da sinópse.
   * @return resposta do JSON como {@code String}.
   * @throws IOException em caso de falha na comunicação com a API.
   */
  private static String consultOMDBAPI(String titulo, boolean sinopseResumida) throws IOException {
    Socket socket = new Socket("www.omdbapi.com", 80);
    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    String path;

    if (sinopseResumida) {
      path = "/?apikey=" + OMDB_API_KEY + "&t=" + URLEncoder.encode(titulo, "UTF-8");
    } else {
      path = "/?apikey=" + OMDB_API_KEY + "&t=" + URLEncoder.encode(titulo, "UTF-8") + "&plot=full";
    }

    out.write("GET " + path + " HTTP/1.1\r\n");
    out.write("Host: www.omdbapi.com\r\n");
    out.write("Connection: close\r\n");
    out.write("\r\n");
    out.flush();

    StringBuilder response = new StringBuilder();
    String line;
    boolean jsonStarted = false;
    while ((line = in.readLine()) != null) {
      if (jsonStarted) {
        response.append(line).append("\n");
      }
      if (line.isEmpty()) {
        jsonStarted = true;
      }
    }

    socket.close();
    return response.toString();
  }

  /**
   * Extrai valor de campo específico de um JSON em string.
   *
   * @param json  o JSON como {@code String}.
   * @param campo o nome do campo a ser extraído.
   * @return valor correspondente ou "N/A" se não encontrado.
   */
  private static String extractFieldJSON(String json, String campo) {
    String busca = "\"" + campo + "\":\"";
    int idx = json.indexOf(busca);
    if (idx == -1)
      return "N/A";
    int start = idx + busca.length();
    int end = json.indexOf("\"", start);
    if (end == -1)
      return "N/A";
    return json.substring(start, end);
  }

  /**
   * Monta o HTML e o CSS para exibir os dados do filme a partir do JSON response
   * da API.
   *
   * @param json o conteúdo do JSON com os dados dos filmes.
   * @return HTML e CSS formatado contendo informações como título, diretor,
   *         elenco, etc.
   */
  private static String HTMLResponseForm(String json) {
    String title = extractFieldJSON(json, "Title");
    String year = extractFieldJSON(json, "Year");
    String director = extractFieldJSON(json, "Director");
    String actors = extractFieldJSON(json, "Actors");
    String plot = extractFieldJSON(json, "Plot");
    String poster = extractFieldJSON(json, "Poster");

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
