<?php 
		
	//this is our upload folder 
	$upload_path = 'uploads/';
	
	//Getting the server ip 
	$server_ip = gethostbyname(gethostname());
	
	//creating the upload url 
	$upload_url = 'http://'.$server_ip.'/AndroidImageUpload/'.$upload_path; 
	
	//response array 
	$response = array(); 
		
	if($_SERVER['REQUEST_METHOD']=='POST'){
		
		//checking the required parameters from the request 
		if(isset($_POST['name']) and isset($_FILES['image']['name'])){
						
			//getting name from the request 
			$name = $_POST['name'];
			
			//getting file info from the request 
			$fileinfo = pathinfo($_FILES['image']['name']);
			
			//getting the file extension 
			$extension = $fileinfo['extension'];
			
			//file url to store in the database 
			$file_url = $upload_url . getFileName() . '.' . $extension;
			
			//file path to upload in the server 
			$file_path = $upload_path . getFileName() . '.'. $extension; 
			
			//trying to save the file in the directory 
			try{
				//saving the file 
				move_uploaded_file($_FILES['image']['tmp_name'],$file_path);
				//filling response array with values 
				$response['error'] = false; 
				$response['url'] = $file_url; 
				$response['name'] = $name;
				
			//if some error occurred 
			}catch(Exception $e){
				$response['error']=true;
				$response['message']=$e->getMessage();
			}		
			//displaying the response 
			echo json_encode($response);
		}else{
			$response['error']=true;
			$response['message']='Please choose a file';
		}
	}
?>