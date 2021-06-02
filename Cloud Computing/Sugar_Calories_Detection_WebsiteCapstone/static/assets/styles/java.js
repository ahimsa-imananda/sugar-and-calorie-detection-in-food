imgInp.onchange = evt => {
  const [file] = imgInp.files
  if (file) {
    imgPrev.src = URL.createObjectURL(file)
  }
}