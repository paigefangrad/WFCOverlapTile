# WFCOverlapTile

A proof-of-concept implementation of a tile-based Wave Function Collapse (WFC) image generation algorithm, also known as [Model Synthesis](https://en.wikipedia.org/wiki/Model_synthesis).

The algorithm examines each pixel in a supplied seed image and builds a ruleset by analyzing its surrounding neighborhood, forming overlapping "tiles". 
These learned tile constraints are then used to generate new images, with valid tiles selected according to probabilities weighted by their frequency in the seed image.

## Examples
Seed image | Generation
:---: | :---:
<img src="https://github.com/paigefangrad/WFCOverlapTile/blob/1eeaff981a94d1ef1a90bbf96c02781ff8ad9bf8/assets/bigislands2upscale.png">|<img src="https://github.com/paigefangrad/WFCOverlapTile/blob/1eeaff981a94d1ef1a90bbf96c02781ff8ad9bf8/assets/WFCExample1.gif" width="350">
<img src="https://github.com/paigefangrad/WFCOverlapTile/blob/1eeaff981a94d1ef1a90bbf96c02781ff8ad9bf8/assets/3Bricksupscale.png">|<img src="https://github.com/paigefangrad/WFCOverlapTile/blob/1eeaff981a94d1ef1a90bbf96c02781ff8ad9bf8/assets/WFCExample2.gif" width="350">
