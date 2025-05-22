import java.io.*;
import java.net.*;

public class Cliente {
  /**
   * Chave da API da OMDb (máximo de 1000 requisições por dia)
   */
  private static final String OMDB_API_KEY = "da70d648";

  /**
   * Envia uma resposta HTTP simples com cabeçalhos e conteúdo
   *
   * @param writer      o {@link PrintWriter} para enviar dados do cliente.
   * @param contentType o tipo do conteúdo (ex.: "text/html").
   * @param content     o conteúdo da resposta.
   */
  public static void sendHttpResponse(PrintWriter writer, String contentType, String content) {
    writer.print("HTTP/1.1 200 OK\r\n");
    writer.print("Content-Type: " + contentType + "\r\n");
    writer.print("Connection: close\r\n");
    writer.print("\r\n");
    writer.print(content);
    writer.flush();
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
  public static String consultOMDBAPI(String titulo, boolean sinopseResumida) throws IOException {
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
  public static String extractFieldJSON(String json, String campo) {
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
   * Lida com a requisição de um cliente individual.
   * Interpreta os dados da requisição HTTP e envia a resposta correspondente.
   *
   * @param client o socket do cliente conectado.
   */
  public static void handleClient(Socket client) {
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
        sendHttpResponse(writer, "text/html", Site.getHtmlForm());
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
        String html = Site.HTMLResponseForm(json);
        sendHttpResponse(writer, "text/html; charset=UTF-8", html);
      } else {
        sendHttpResponse(writer, "text/html", "<h1>404 Not Found</h1>");
      }

      client.close();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
