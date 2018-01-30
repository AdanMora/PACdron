package pac.pac.pacdron;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Iterator;

import static java.net.Proxy.Type.HTTP;

public class Consulta extends AppCompatActivity {

    Button btn_Consulta;
    TextView nombre;
    TextView provincia;
    TextView canton;
    TextView distrito;
    TextView centro_votacion;
    TextView direccion;
    TextView numMesa;
    TextView numElector;
    EditText inputCedula;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        btn_Consulta = (Button) findViewById(R.id.btn_Consultar);

        nombre = (TextView) findViewById(R.id.txt_nombre);
        provincia = (TextView) findViewById(R.id.txt_provincia);
        canton = (TextView) findViewById(R.id.txt_canton);
        distrito = (TextView) findViewById(R.id.txt_distrito);
        centro_votacion = (TextView) findViewById(R.id.txt_centro);
        direccion = (TextView) findViewById(R.id.txt_direccion);
        numMesa = (TextView) findViewById(R.id.txt_numMesa);
        numElector = (TextView) findViewById(R.id.txt_numElector);

        inputCedula = (EditText) findViewById(R.id.et_cedula);

        btn_Consulta.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                int numCed = Integer.parseInt(inputCedula.getText().toString());

                ConsultarCedula consulta = new ConsultarCedula();
                consulta.execute(String.valueOf(numCed));

                inputCedula.setText("");

            }
        });

    }


    private class ConsultarCedula extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... cedula) {

            HttpURLConnection connection = null;
            StringBuilder result = new StringBuilder();

            try {

                JSONObject ced = new JSONObject();
                ced.put("numeroCedula",cedula[0]);

                URL url = new URL("http://www.tse.go.cr/dondevotarp/prRemoto.aspx/ObtenerDondeVotar");

                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                OutputStream os = connection.getOutputStream();
                os.write(ced.toString().getBytes("UTF-8"));
                os.close();

                connection.connect();

                InputStream inputStream = connection.getInputStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }

                return new JSONObject(result.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                connection.disconnect();
            }
            return null;

        }

        @Override
        protected void onPostExecute(JSONObject s) {
            System.out.println(s.toString());
            JSONObject resultadoConsulta = null;
            Iterator keys = null;
            try {
                resultadoConsulta = s.getJSONObject("d").getJSONObject("lista");
                keys = resultadoConsulta.keys();

                if (!keys.hasNext()){
                    Toast toast = Toast.makeText(getApplicationContext(), "No se encontró la cédula digitada", Toast.LENGTH_SHORT);
                    toast.show();
                } else {

                    while (keys.hasNext()) {
                        //based on you key types
                        String keyStr = (String)keys.next();
                        System.out.println(keyStr);

                        if (keyStr.equals("nombreCompleto")){
                            nombre.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("descripcionProvincia")) {
                            provincia.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("descripcionCanton")) {
                            canton.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("descripcionDistrito")) {
                            distrito.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("nombreCentroVotacion")) {
                            centro_votacion.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("junta")) {
                            numMesa.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("numeroElector")) {
                            numElector.setText(resultadoConsulta.getString(keyStr));
                        } else if (keyStr.equals("direccionEscuela")) {
                            direccion.setText(resultadoConsulta.getString(keyStr));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
