from flask import Flask, render_template, request, jsonify, url_for
from keras.preprocessing import image
import numpy as np
from PIL import Image
import os
import tensorflow as tf
import json
app = Flask(__name__)
path_to_file = ""

food_data_json = open('food_data.json',)
food_data_dict = json.load(food_data_json)
food_data_json.close()
@app.route('/', methods = ['GET','POST'])
def index():
    if request.method == 'POST':
        file = request.files['file']
        path_to_file = file.filename
        file.save('image-uploaded/' + path_to_file)
        json_res = predict('image-uploaded/' + path_to_file)
        os.remove('image-uploaded/' + path_to_file)
        return json_res
    else:
        print("Rendered")
        return render_template('index.html')

def predict(image_path):
    path = image_path
    images = preprocess_image(path)
    classes = predict_using_model(images, 10)
    classes_list = classes[0].tolist()
    max_classes = max(classes_list)
    max_index = classes_list.index(max_classes)
    predicted_label = food_data_dict[max_index]["label"]
    predicted_index = food_data_dict[max_index]["index"]
    predicted_sugar = food_data_dict[max_index]["sugar"]
    predicted_calorie = food_data_dict[max_index]["calorie"]
    result = (path + " is a " + predicted_label + " with " + predicted_sugar + "g of Sugar \ 100g and with " + predicted_calorie + " cal of Calorie \ 100 g")
    return jsonify({'index':predicted_index, 'file_name':image_path,'text_result':result, "label":predicted_label, "sugar":predicted_sugar, "calorie":predicted_calorie})

def load_saved_model(model_name):
    loaded_model = tf.keras.models.load_model(model_name)
    return loaded_model

def preprocess_image(image_path):
    img = image.load_img(image_path, target_size=(224, 224))
    x = image.img_to_array(img)
    x = np.expand_dims(x, axis=0)
    images = np.vstack([x])
    return images

def predict_using_model(image, batch_size):
    model = load_saved_model("model.h5")
    classes = model.predict(image, batch_size)
    return classes
		
if __name__ == '__main__':
   app.run(debug = True)