<?php
    //print_r($_FILES);
    $file_path = "uploads/";
     
    $file_path = $file_path . basename( $_FILES['uploaded_file']['name']) . rand(10,10000);
    if(move_uploaded_file($_FILES['uploaded_file']['tmp_name'], $file_path)) {
        echo "success";
    } else{
        echo "fail";
    }
 ?>


<?php
function fn_api_upload_image($params)
{
    $params['name'] = preg_replace("/[^a-zA-Z0-9.]/i", "", $params['name']);
    $imgname = explode('.',$params['name']);
    $tempImageName = $imgname;
    $imageNameCount = count($imgname);
    unset($imgname[($imageNameCount-1)]);

    $params['name'] = implode(".", $imgname).'_'.time().'.'.$tempImageName[($imageNameCount-1)];

    $fileName = $params["name"];
    $fileTmpLoc = $params["tmp_name"];

    //checking folder
    $date = date('Ymd');
    $destination_folder = DIR_ROOT."/images/upload_images/$date";
    if(!is_dir($destination_folder)) {
        //create folder
        mkdir($destination_folder);
    }

    $fileName = $date."/".$fileName;

    // Path and file name
    $pathAndName = DIR_ROOT."/images/upload_images/" . $fileName;
    // Run the move_uploaded_file() function here
    $moveResult = move_uploaded_file($fileTmpLoc, $pathAndName);
    // Evaluate the value returned from the function if needed
    if ($moveResult == true) {
//        $local = $pathAndName;
        $local = Registry::get('config.loc_prd_img')."upload_images/" . $fileName;
        $remote =  Registry::get('config.remote_img')."images/upload_images/$date/";
//        $remote = Registry::get('config.remote_prd_img');
        $parameter = Registry::get('config.rsync_parameter_upload_image');

        $rsyn = exec("rsync $parameter $local $remote &");

        //deleting image from local folder as it is uploaded on cdn
        unlink($pathAndName);

        $res = array(
            "success" => "success",
            "message" => Registry::get('config.ext_images_host_upload_image_cdn')."/images/upload_images/".$fileName
        );
    } else {
        $res = array(
            "error" => "error",
            "message" => "ERROR: File not moved correctly"
        );
    }

    return $res;
}

?>