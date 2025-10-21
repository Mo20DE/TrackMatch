import {
  Item,
  ItemContent,
  ItemMedia,
  ItemTitle,
} from "@/components/ui/item"
import { Spinner } from "@/components/ui/spinner"


export function DownloadSpinner({processStage}: {processStage: string}) {
  return (
      <Item variant="muted" className="-translate-y-6">
        <ItemMedia>
          <Spinner />
        </ItemMedia>
        <ItemContent>
          <ItemTitle className="line-clamp-1">{processStage}</ItemTitle>
        </ItemContent>
      </Item>
  )
}

export default DownloadSpinner;
