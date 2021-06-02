files.onchange = evt => {
  const [file] = files.files
  if (file) {
    imgPrev.src = URL.createObjectURL(file)
  }
}