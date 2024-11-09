import os
import json
from urllib.parse import urlparse

from lxml import etree
from bs4 import BeautifulSoup

current_dir = os.path.dirname(__file__)

"""source directories"""
sources_paths = [os.path.join(current_dir, 'Data Cleaning/sources_DC'),
                 os.path.join(current_dir, 'Data Fusion/sources_DF'),
                 os.path.join(current_dir, 'Retrieval Augmented Generation/sources_RAG')]

"""Output directories for json files"""
output_dirs = [os.path.join(current_dir, 'Data Cleaning/extraction_DC'),
               os.path.join(current_dir, 'Data Fusion/extraction_DF'),
               os.path.join(current_dir, 'Retrieval Augmented Generation/extraction_RAG')]


def extract_tables_from_html_file(html_file, arxiv_id, output_dir):
    # Leggi il contenuto del file HTML
    with open(html_file, 'r', encoding='utf-8') as file:
        content = file.read()

    # Parse HTML content with BeautifulSoup
    soup = BeautifulSoup(content, 'html.parser')

    # Use lxml to navigate with XPath
    parser = etree.HTMLParser()
    tree = etree.HTML(str(soup))

    # Extract all tables using XPath
    tables = tree.xpath('//table')


    # Initialize the data dictionary to store table data
    data = {}

    for i, table in enumerate(tables, start=1):
        # Extracting table caption (if available)

        table_id = table.xpath('./@id')[0]

        # Extract the caption of the table
        caption = table.xpath(f"./ancestor::figure/figcaption")

        caption_text = ''
        # Extract the text content of the caption
        if caption:
            caption_text = (etree.tostring
                            (caption[0], method='text', encoding='unicode')
                            .strip()) if caption else ""

        # Extract the HTML of the table
        table_html = etree.tostring(table, pretty_print=True, method='html').decode('utf-8')

        # Extract footnotes related to the table (if available)
        footnotes = table.xpath(f".//cite")
        footnotes_array = []
        if footnotes:
            for index in range(len(footnotes)):
                footnote_urls = footnotes[index].xpath('./a/@href')
                if footnote_urls: # if the footnote contains a link
                    for footnote_url in footnote_urls:
                        footnote_id = urlparse(footnote_url).fragment
                        bibItem = tree.xpath(f"//li[@id='{footnote_id}']")[0]
                        footnotes_array.append(
                            etree.tostring(
                                bibItem, method='text', encoding='unicode')
                            .strip().replace('\u00A0', ' '))
                else: # if the footnote does not contain a link, then extract the span text content
                    footnote_span = footnotes[index].xpath('./span/text()')
                    for span in footnote_span:
                        footnotes_array.append(
                           span.replace('\u00A0', ' '))


        # find paragraphs that contain an href with the fragment that refers to the current table
        references = tree.xpath(f"//p[a[contains(@href, "
                                f"'#{'.'.join(table_id.split('.')[:2])}')]]")

        # Estract the text of the paragraphs found for each reference
        references_text = [etree.tostring(ref, method='text', encoding='unicode')
                           .strip().replace('\u00A0', ' ') for ref
                           in references]

        # Store the extracted information in the dictionary
        data[table_id] = {
            "table": table_html,
            "caption": caption_text,
            "footnotes": footnotes_array,
            "references": references_text
        }

    os.makedirs(output_dir, exist_ok=True)

    # Save the JSON data to a file named after the arxiv_id
    output_file = os.path.join(output_dir, f"{arxiv_id}.json")
    with open(output_file, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False, indent=4)

    print(f"Data extracted and saved to {output_file}")


def process_html_files_in_directory(source_dir, output_dir):
    # Itera su tutti i file nella directory specificata
    for filename in os.listdir(source_dir):
        if filename.endswith(".html"):
            # Extract the arxiv_id from file name (es: "2409.17044.html" -> "2409.17044")
            arxiv_id = filename.split(".html")[0]
            html_file_path = os.path.join(source_dir, filename)
            extract_tables_from_html_file(html_file_path, arxiv_id, output_dir)


def main():
    # Process each HTML file in each source directory
    for sources_path, output_dir in zip(sources_paths, output_dirs):
        process_html_files_in_directory(sources_path, output_dir)


if __name__ == "__main__":
    main()
