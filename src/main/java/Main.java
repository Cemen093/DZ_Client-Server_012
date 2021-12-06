//Написать утилиту для слежения о новых товарах в категории «iPhone 11 pro» на сайте олх .
//Утилита должна создавать структуру директорий (дата добавления устройства - имя папки).
//Документ внутри - описание смартфона , цена, ссылка на главное фото и ссылка на сам товар.
//Утилита должна отслеживать добавление нового товара и синхронизировать с базой.

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        while (true){
            function();
            TimeUnit.MINUTES.sleep(600);
        }
    }
    public static void function(){

        // даты "29 нояб.", "Сегодня 20:23", "Вчера 10:41", круто! я так понял это надо к нормальному виду приводить, да?
        HashMap<String, Integer> _MOUNT = new HashMap();
        _MOUNT.put("янв.", 1);
        _MOUNT.put("фев.", 2);
        _MOUNT.put("мар.", 3);
        _MOUNT.put("апр.", 4);
        _MOUNT.put("май", 5);
        _MOUNT.put("июн.", 6);
        _MOUNT.put("июл.", 7);
        _MOUNT.put("авг.", 8);
        _MOUNT.put("сен.", 9);
        _MOUNT.put("окт.", 10);
        _MOUNT.put("нояб.", 11);
        _MOUNT.put("дек.", 12);
        try {
            String url = "https://www.olx.ua/elektronika/telefony-i-aksesuary/mobilnye-telefony-smartfony/q-iphone-11-pro/";
            Document doc;
            Elements elements = new Elements();
            Elements aNextPage = null;

            while(true) {
                doc = Jsoup.connect(url)
                        .userAgent("Chrome/4.0.249.0 Safari/532.5")
                        .referrer("http://www.google.com")
                        .get();
                elements.addAll(doc.select("tr.wrap"));
                aNextPage = doc.select("span.fbold.next.abs.large a.link.pageNextPrev");
                if (aNextPage.isEmpty()){
                    break;
                }
                url = aNextPage.attr("href");
            }
            for (Element element : elements) {
                LocalDateTime dateNow = LocalDateTime.now();
                LocalDateTime date;
                String _date = element.select("td.bottom-cell small.breadcrumb.x-normal").get(1).text();
                String[] arr = _date.split(" ");

                if (arr[0].equals("Сегодня")){
                    date = LocalDateTime.of(dateNow.getYear(), dateNow.getMonth(), dateNow.getDayOfMonth(), Integer.parseInt(arr[1].split(":")[0]), Integer.parseInt(arr[1].split(":")[1]));
                } else if (arr[0].equals("Вчера")){
                    date = LocalDateTime.of(dateNow.getYear(), dateNow.getMonth(), dateNow.getDayOfMonth(), Integer.parseInt(arr[1].split(":")[0]), Integer.parseInt(arr[1].split(":")[1])).minusDays(1);
                } else {
                    if (_MOUNT.get(arr[1]) == null){
                        System.out.println("Неизвестная строка: " + _date);
                        continue;
                    }
                    date = LocalDateTime.of(dateNow.getYear(), _MOUNT.get(arr[1]), Integer.parseInt(arr[0]), 0,0,0);
                }
                //Утилита должна создавать структуру директорий (дата добавления устройства - имя папки).
                String filename = element.select("td.title-cell").text().replaceAll("[\\/:*?\\\"<>|]", "") + ".txt";
                File file = new File("./bd/"+date.format(DateTimeFormatter.ofPattern("dd-MM-yyy_HH-mm-ss"))+"/", filename);
                if (!file.getParentFile().exists()){
                    file.getParentFile().mkdirs();
                }
                if (!file.exists()){
                    file.createNewFile();
                } else {
                    continue;
                }
                try(FileWriter writer = new FileWriter(file.getAbsolutePath(), false))
                {
                    //Документ внутри - описание смартфона , цена, ссылка на главное фото и ссылка на сам товар.
                    writer.append("Описание: " + element.select("td.title-cell").text()+"\n");
                    writer.append("Цена: " + element.select("td.wwnormal.tright.td-price").text()+"\n");
                    writer.append("ссылка на фото: "+ element.select("td.photo-cell img.fleft").attr("src")+"\n");
                    writer.append("ссылка на товар: "+ element.select("td.photo-cell a").attr("href")+"\n");
                    writer.flush();
                }catch(IOException ex){
                    System.out.println(ex.getMessage());
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
